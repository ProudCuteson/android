package top.role.android.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
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

    init {
        observeSession()
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
                        sessionStorage.clearSession()

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
                            sessionStorage.clearSession()

                            _authState.value =
                                AuthState.SignedOut
                        }
                }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            sessionStorage.clearSession()
        }
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