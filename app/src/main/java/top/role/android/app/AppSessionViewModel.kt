package top.role.android.app

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import top.role.android.core.auth.AppSession
import top.role.android.core.auth.AuthRepository
import top.role.android.core.auth.AuthState
import top.role.android.core.convex.ConvexMobileClient
import top.role.android.core.network.auth.SessionValidationResult
import top.role.android.core.storage.SessionStorage

class AppSessionViewModel(
    context: Context,
    private val sessionStorage: SessionStorage,
    private val convexClient: ConvexMobileClient,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val appContext = context.applicationContext
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    private val _localOwnerUserId = MutableStateFlow<String?>(null)
    val localOwnerUserId: StateFlow<String?> = _localOwnerUserId.asStateFlow()

    // 避免前台检查与 Session 监听同时执行验证
    private val validationMutex = Mutex()

    init {
        initializeIdentityAndObserve()
    }

    // MARK: Local Identity

    private fun initializeIdentityAndObserve() {
        viewModelScope.launch {
            try {
                // 必须先完成旧身份迁移或生成访客身份
                sessionStorage.ensureLocalIdentity()

            } catch (error: CancellationException) {
                throw error

            } catch (error: Exception) {
                Log.e(
                    "AppSession",
                    "Local identity initialization failed",
                    error,
                )
                return@launch
            }

            // 本地身份初始化完成后，再启动两个独立监听
            observeLocalOwner()
            observeSession()
        }
    }

    private fun observeLocalOwner() {
        viewModelScope.launch {
            sessionStorage.localOwnerUserId
                .collectLatest { userId ->
                    _localOwnerUserId.value = userId
                }
        }
    }

    // MARK: Remote Session

    private fun observeSession() {
        viewModelScope.launch {
            sessionStorage.session
                .collectLatest { session ->
                    if (session == null) {
                        validationMutex.withLock {
                            if (_authState.value != AuthState.SignedOut) {
                                deactivateConvexAuth()
                            }

                            _authState.value = AuthState.SignedOut
                        }

                        return@collectLatest
                    }

                    validateAndActivate(session)
                }
        }
    }

    /**
     * 验证远端 Session，并在验证成功后激活 Convex。
     *
     * 本地身份不受远端验证结果影响。
     */
    private suspend fun validateAndActivate(
        session: AppSession,
    ) {
        validationMutex.withLock {

            val userId = session.userId

            // 1. 确认当前 Session 仍然是本次待验证的 Session
            val currentSession = sessionStorage.session.first()

            if (currentSession != session) {
                return@withLock
            }

            // 2. 本地 Token 有效期检查
            if (session.isExpired) {

                deactivateConvexAuth()

                _authState.value =
                    AuthState.RequireLogin(userId)

                return@withLock
            }

            // 3. 远端验证
            // 同一用户前台重新验证时保留 SignedIn，避免界面闪烁。
            // 切换用户时则进入 Loading。
            val currentAuthState = _authState.value

            if (
                currentAuthState !is AuthState.SignedIn ||
                currentAuthState.userId != userId
            ) {
                _authState.value = AuthState.Loading
            }

            val validation = try {
                authRepository.validateSession(userId)

            } catch (error: CancellationException) {
                throw error

            } catch (error: Exception) {

                Log.e(
                    "AppSession",
                    "Remote session validation failed",
                    error,
                )

                SessionValidationResult.NeedVerify(
                    userId = userId,
                )
            }

            // 4. 远端验证期间可能发生账户切换
            val latestSession = sessionStorage.session.first()

            if (latestSession != session) {
                return@withLock
            }

            // 5. 统一检查服务端返回的用户身份
            val validationUserId = when (validation) {

                is SessionValidationResult.Valid ->
                    validation.userId

                is SessionValidationResult.NeedVerify ->
                    validation.userId

                is SessionValidationResult.RequireLogin ->
                    validation.userId

                is SessionValidationResult.AccountUnavailable ->
                    validation.userId
            }

            if (validationUserId != userId) {

                deactivateConvexAuth()

                _authState.value =
                    AuthState.RequireLogin(userId)

                return@withLock
            }

            // 6. 根据远端验证结果处理
            when (validation) {

                is SessionValidationResult.Valid -> {

                    // 远端验证成功，激活 Convex SDK
                    val loginResult =
                        convexClient.loginFromStoredSession()

                    // 激活期间再次确认 Session 没有变化
                    val activatedSession =
                        sessionStorage.session.first()

                    if (activatedSession != session) {
                        return@withLock
                    }

                    _authState.value =
                        if (loginResult.isSuccess) {

                            AuthState.SignedIn(userId)

                        } else {

                            Log.e(
                                "AppSession",
                                "Convex authentication activation failed",
                                loginResult.exceptionOrNull(),
                            )

                            deactivateConvexAuth()

                            AuthState.NeedVerify(userId)
                        }
                }

                is SessionValidationResult.NeedVerify -> {

                    // 网络或服务暂时不可用
                    // 不清理 Token，不主动退出 SDK
                    _authState.value =
                        AuthState.NeedVerify(userId)
                }

                is SessionValidationResult.RequireLogin -> {

                    // 服务端明确要求重新登录
                    deactivateConvexAuth()

                    _authState.value =
                        AuthState.RequireLogin(userId)
                }

                is SessionValidationResult.AccountUnavailable -> {

                    deactivateConvexAuth()

                    _authState.value =
                        AuthState.AccountUnavailable(
                            userId = userId,
                            code = validation.code,
                        )
                }
            }
        }
    }

    // MARK: Convex Authentication

    /**
     * 只停用 Convex SDK 的认证状态。
     *
     * 不清理 SessionStorage。
     */
    private suspend fun deactivateConvexAuth() {

        val result = convexClient.logout(
            appContext
        )

        result.exceptionOrNull()?.let { error ->

            Log.e(
                "AppSession",
                "Convex auth deactivation failed",
                error,
            )
        }
    }

    /**
     * 用户主动退出远端账户。
     *
     * 1. 停用 Convex SDK 认证。
     * 2. 清理远端 Session。
     * 3. 更新认证状态。
     *
     * 保留本地身份和本地数据。
     */
    fun signOut(
        onComplete: (() -> Unit)? = null,
    ) {
        viewModelScope.launch {
            validationMutex.withLock {
                // 先停用 Convex SDK
                deactivateConvexAuth()
                // 清理远端凭证
                sessionStorage.clearRemoteSession()
                // 更新远端认证状态
                _authState.value =
                    AuthState.SignedOut
            }
            onComplete?.invoke()
        }
    }

    // MARK: Foreground

    /**
     * App 回到前台时重新检查远端身份。
     *
     * 只有当前 Session 仍然有效且认证成功，
     * 才返回远端 userId。
     *
     * 不影响本地身份。
     */
    suspend fun checkSessionOnForeground(): String? {

        val storedSession =
            sessionStorage.session.first()

        if (storedSession == null) {

            validationMutex.withLock {

                // 再次确认，避免读取期间发生登录
                if (sessionStorage.session.first() == null) {

                    deactivateConvexAuth()

                    _authState.value =
                        AuthState.SignedOut
                }
            }

            return null
        }

        // 执行远端验证及 Convex 认证激活
        validateAndActivate(storedSession)

        // 再次确认 Session 与认证状态
        return validationMutex.withLock {

            val latestSession =
                sessionStorage.session.first()

            val state = _authState.value

            if (
                latestSession == storedSession &&
                state is AuthState.SignedIn &&
                state.userId == storedSession.userId
            ) {
                state.userId
            } else {
                null
            }
        }
    }
}


class AppSessionViewModelFactory(
    private val context: Context,
    private val sessionStorage: SessionStorage,
    private val convexClient: ConvexMobileClient,
    private val authRepository: AuthRepository,
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
                context = context.applicationContext,
                sessionStorage = sessionStorage,
                convexClient = convexClient,
                authRepository = authRepository,
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}
