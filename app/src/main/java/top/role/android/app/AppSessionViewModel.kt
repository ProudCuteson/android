package top.role.android.app

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import top.role.android.core.auth.AuthState
import top.role.android.core.convex.ConvexMobileClient
import top.role.android.core.storage.SessionStorage

class AppSessionViewModel(
    private val sessionStorage: SessionStorage,
    private val convexClient: ConvexMobileClient,
) : ViewModel() {

    private val _authState =
        MutableStateFlow<AuthState>(
            AuthState.Loading
        )

    val authState: StateFlow<AuthState> =
        _authState.asStateFlow()

    private val _localOwnerUserId =
        MutableStateFlow<String?>(
            null
        )

    val localOwnerUserId:
            StateFlow<String?> =
        _localOwnerUserId.asStateFlow()

    init {
        observeLocalOwner()
        observeSession()
    }

    private fun observeLocalOwner() {
        viewModelScope.launch {
            sessionStorage
                .localOwnerUserId
                .collectLatest { userId ->
                    _localOwnerUserId.value =
                        userId
                }
        }
    }

    private fun observeSession() {
        viewModelScope.launch {
            sessionStorage.session
                .collectLatest { session ->

                    if (session == null) {
                        _authState.value =
                            AuthState.SignedOut

                        return@collectLatest
                    }

                    if (session.isExpired) {
                        sessionStorage
                            .clearRemoteSession()

                        _authState.value =
                            AuthState.SignedOut

                        return@collectLatest
                    }

                    _authState.value =
                        AuthState.Loading

                    val loginResult =
                        convexClient
                            .loginFromStoredSession()

                    loginResult
                        .onSuccess {
                            _authState.value =
                                AuthState.SignedIn(
                                    session = session,
                                )
                        }
                        .onFailure {
                            _authState.value =
                                AuthState.SignedOut
                        }
                }
        }
    }

    // ConvexMobileClient.logout() 调用 SDK 的退出流程，
    // 由 SDK 调用 BridgeJwtAuthProvider.logout() 清理远端凭证。
    // 既清理 DataStore 中的远端 Session，也让 Convex SDK 更新自身认证状态，而不只是删除 Token。
    fun signOut(
        context: Context,
        onComplete: (() -> Unit)? = null,
    ) {
        viewModelScope.launch {
            convexClient
                .logout(context.applicationContext)
                .onSuccess {
                    onComplete?.invoke()
                }
                .onFailure { error ->
                    Log.e(
                        "AppSession",
                        "Sign out failed",
                        error,
                    )
                }
        }
    }

    suspend fun checkSessionOnForeground(
        context: Context,
    ): String? {

        val storedSession =
            sessionStorage.session.first()
                ?: return null

        if (storedSession.isExpired) {

            // 立即停止将远端视为已登录
            _authState.value =
                AuthState.SignedOut

            // 同步清理 Convex SDK 的认证状态
            val result =
                convexClient.logout(
                    context.applicationContext
                )

            result.exceptionOrNull()?.let { error ->
                Log.e(
                    "AppSession",
                    "Expired session logout failed",
                    error,
                )

                // SDK 退出失败时，至少清除已过期凭证
                sessionStorage.clearRemoteSession()
            }

            return null
        }

        val signedIn =
            _authState.value as? AuthState.SignedIn
                ?: return null

        // 必须是当前存储的同一用户
        if (
            signedIn.session.userId !=
            storedSession.userId
        ) {
            return null
        }

        return storedSession.userId
    }

}

class AppSessionViewModelFactory(
    private val sessionStorage: SessionStorage,
    private val convexClient: ConvexMobileClient,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
    ): T {

        if (
            modelClass.isAssignableFrom(
                AppSessionViewModel::class.java
            )
        ) {
            return AppSessionViewModel(
                sessionStorage = sessionStorage,
                convexClient = convexClient,
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}