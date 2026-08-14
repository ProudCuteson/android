package top.role.android.core.auth

sealed interface AuthState {

    data object Loading : AuthState

    data object SignedOut : AuthState

    data class SignedIn(
        val session: AppSession,
    ) : AuthState
}