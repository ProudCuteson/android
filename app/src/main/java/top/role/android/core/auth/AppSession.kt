// 保留 AppSession 作为远端会话凭据模型

package top.role.android.core.auth

data class AppSession(
    val accessToken: String,
    val expiresAt: Long,
    val userId: String,
    val phone: String,
) {
    val isExpired: Boolean
        get() = System.currentTimeMillis() >= expiresAt
}