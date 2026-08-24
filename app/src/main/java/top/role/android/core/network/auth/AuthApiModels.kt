package top.role.android.core.network.auth

import kotlinx.serialization.Serializable

@Serializable
data class SendOtpRequest(
    val phone: String,
)

@Serializable
data class SendOtpResponse(
    val ok: Boolean,
    val maskedPhone: String? = null,
    val expiresIn: Int? = null,
    val resendAfter: Int? = null,
    val code: String? = null,
    val message: String? = null,
)

@Serializable
data class VerifyOtpRequest(
    val phone: String,
    val code: String,
)

@Serializable
data class VerifyOtpResponse(
    val ok: Boolean,
    val ticket: String? = null,
    val expiresAt: Long? = null,
    val code: String? = null,
    val message: String? = null,
)

@Serializable
data class ExchangeOtpRequest(
    val ticket: String,
)

@Serializable
data class ExchangeOtpUser(
    val id: String,
    val phone: String,
    val isNewUser: Boolean,
)

@Serializable
data class ExchangeOtpResponse(
    val ok: Boolean,
    val accessToken: String? = null,
    val expiresAt: Long? = null,
    val user: ExchangeOtpUser? = null,
    val code: String? = null,
    val message: String? = null,
)

@Serializable
data class PasswordLoginRequest(
    val phone: String,
    val password: String,
)