package top.role.android.core.auth

import top.role.android.core.network.auth.AuthApi
import top.role.android.core.network.auth.ExchangeOtpUser

data class SendOtpResult(
    val maskedPhone: String,
    val expiresIn: Int,
    val resendAfter: Int,
)

data class VerifyOtpResult(
    val ticket: String,
    val expiresAt: Long,
)

data class LoginResult(
    val accessToken: String,
    val expiresAt: Long,
    val user: ExchangeOtpUser,
)

class AuthRepository(
    private val authApi: AuthApi,
) {

    suspend fun sendOtp(
        phone: String,
    ): Result<SendOtpResult> {
        return runCatching {
            val response = authApi.sendOtp(phone)

            if (!response.ok) {
                throw AuthException(
                    code = response.code ?: "OTP_SEND_FAILED",
                    message = response.message ?: "验证码发送失败",
                )
            }

            SendOtpResult(
                maskedPhone = response.maskedPhone
                    ?: throw AuthException(
                        code = "INVALID_RESPONSE",
                        message = "验证码发送响应异常",
                    ),
                expiresIn = response.expiresIn ?: 300,
                resendAfter = response.resendAfter ?: 60,
            )
        }
    }

    suspend fun verifyOtp(
        phone: String,
        code: String,
    ): Result<VerifyOtpResult> {
        return runCatching {
            val response = authApi.verifyOtp(
                phone = phone,
                code = code,
            )

            if (!response.ok) {
                throw AuthException(
                    code = response.code ?: "INVALID_CODE",
                    message = response.message ?: "验证码校验失败",
                )
            }

            VerifyOtpResult(
                ticket = response.ticket
                    ?: throw AuthException(
                        code = "INVALID_RESPONSE",
                        message = "验证码校验响应异常",
                    ),
                expiresAt = response.expiresAt
                    ?: throw AuthException(
                        code = "INVALID_RESPONSE",
                        message = "验证码凭证有效期缺失",
                    ),
            )
        }
    }

    suspend fun exchangeOtp(
        ticket: String,
    ): Result<LoginResult> {
        return runCatching {
            val response = authApi.exchangeOtp(ticket)

            if (!response.ok) {
                throw AuthException(
                    code = response.code ?: "LOGIN_FAILED",
                    message = response.message ?: "登录失败",
                )
            }

            LoginResult(
                accessToken = response.accessToken
                    ?: throw AuthException(
                        code = "INVALID_RESPONSE",
                        message = "登录令牌缺失",
                    ),
                expiresAt = response.expiresAt
                    ?: throw AuthException(
                        code = "INVALID_RESPONSE",
                        message = "登录有效期缺失",
                    ),
                user = response.user
                    ?: throw AuthException(
                        code = "INVALID_RESPONSE",
                        message = "登录用户信息缺失",
                    ),
            )
        }
    }
}

class AuthException(
    val code: String,
    override val message: String,
) : Exception(message)