package top.role.android.core.auth

sealed interface AuthState {

    data object Loading : AuthState
    data object SignedOut : AuthState
    data class SignedIn( val userId: String ) : AuthState

    data class NeedVerify(
        val userId: String,
    ) : AuthState

    data class RequireLogin(
        val userId: String,
    ) : AuthState

    data class AccountUnavailable(
        val userId: String,
        val code: String,
    ) : AuthState

}