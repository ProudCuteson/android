package top.role.android.core.network.auth

sealed interface SessionValidationResult {

    /**
     * 服务端确认 Session 有效
     */
    data class Valid(
        val userId: String,
    ) : SessionValidationResult

    /**
     * 暂时无法完成验证
     *
     * 例如网络不可用、认证服务暂时不可用。
     * 不代表用户已经退出登录。
     */
    data class NeedVerify(
        val userId: String,
    ) : SessionValidationResult

    /**
     * 服务端明确要求重新登录
     */
    data class RequireLogin(
        val userId: String,
        val code: String,
    ) : SessionValidationResult

    /**
     * 账户不可用
     */
    data class AccountUnavailable(
        val userId: String,
        val code: String,
    ) : SessionValidationResult
}