package top.role.android.core.network.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.coroutines.CancellationException
import top.role.android.core.storage.SessionStorage
import top.role.android.core.network.ApiJson

class AuthApi(
    private val baseUrl: String,
    private val sessionStorage: SessionStorage,
    private val client: OkHttpClient = OkHttpClient(),
    private val json: Json = ApiJson,
) {

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun sendOtp(
        phone: String,
    ): SendOtpResponse {
        return post(
            path = "/api/auth/otp/send",
            request = SendOtpRequest(
                phone = phone,
            ),
            decode = {
                json.decodeFromString<SendOtpResponse>(it)
            },
        )
    }

    suspend fun verifyOtp(
        phone: String,
        code: String,
    ): VerifyOtpResponse {
        return post(
            path = "/api/auth/otp/verify",
            request = VerifyOtpRequest(
                phone = phone,
                code = code,
            ),
            decode = {
                json.decodeFromString<VerifyOtpResponse>(it)
            },
        )
    }

    suspend fun exchangeOtp(
        ticket: String,
    ): ExchangeOtpResponse {
        return post(
            path = "/api/auth/otp/exchange",
            request = ExchangeOtpRequest(
                ticket = ticket,
            ),
            decode = {
                json.decodeFromString<ExchangeOtpResponse>(it)
            },
        )
    }

    suspend fun signInWithPassword(
        phone: String,
        password: String,
    ): ExchangeOtpResponse {
        return post(
            path = "/api/auth/phone/password-login",
            request = PasswordLoginRequest(
                phone = phone,
                password = password,
            ),
            decode = {
                json.decodeFromString<ExchangeOtpResponse>(it)
            },
        )
    }

    suspend fun validateSession(
        userId: String,
    ): SessionValidationResult {

        try {
            val session = sessionStorage.session.first()
                ?: return SessionValidationResult.RequireLogin(
                    userId = userId,
                    code = "APP_NOT_AUTHENTICATED",
                )

            val accessToken = session.accessToken.trim()

            if (accessToken.isBlank()) {
                return SessionValidationResult.RequireLogin(
                    userId = userId,
                    code = "APP_NOT_AUTHENTICATED",
                )
            }

            val url = baseUrl.trimEnd('/') +
                    "/api/auth/app/session"

            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $accessToken")
                .header("Accept", "application/json")
                .get()
                .build()

            return withContext(Dispatchers.IO) {
                client.newCall(request)
                    .execute()
                    .use { response ->

                        val body = response.body?.string().orEmpty()

                        val data = json.parseToJsonElement(body).jsonObject

                        val ok = data["ok"]
                            ?.jsonPrimitive
                            ?.booleanOrNull == true

                        val code = data["code"]
                            ?.jsonPrimitive
                            ?.contentOrNull

                        when {
                            response.code == 200 && ok -> {
                                val remoteUserId = data["user"]
                                    ?.jsonObject
                                    ?.get("id")
                                    ?.jsonPrimitive
                                    ?.contentOrNull

                                when {
                                    remoteUserId.isNullOrBlank() ->
                                        SessionValidationResult.NeedVerify(userId)

                                    remoteUserId != userId ->
                                        SessionValidationResult.RequireLogin(
                                            userId = userId,
                                            code = "SESSION_USER_MISMATCH",
                                        )

                                    else ->
                                        SessionValidationResult.Valid(userId)
                                }
                            }

                            response.code == 410 &&
                                    code == "ACCOUNT_DELETED" ->
                                SessionValidationResult.AccountUnavailable(
                                    userId,
                                    code,
                                )

                            response.code == 404 &&
                                    code == "USER_NOT_FOUND" ->
                                SessionValidationResult.AccountUnavailable(
                                    userId,
                                    code,
                                )

                            response.code == 401 &&
                                    code in setOf(
                                "UNAUTHORIZED",
                                "APP_NOT_AUTHENTICATED",
                                "INVALID_OR_EXPIRED_TOKEN",
                                "SESSION_USER_MISMATCH",
                                "SESSION_NOT_FOUND",
                                "SESSION_REVOKED",
                                "SESSION_EXPIRED",
                            ) ->
                                SessionValidationResult.RequireLogin(
                                    userId,
                                    code ?: "UNAUTHORIZED",
                                )

                            else ->
                                SessionValidationResult.NeedVerify(userId)
                        }
                    }
            }

        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            return SessionValidationResult.NeedVerify(userId)
        }
    }

    private suspend inline fun <reified RequestType, ResponseType> post(
        path: String,
        request: RequestType,
        crossinline decode: (String) -> ResponseType,
    ): ResponseType {
        return withContext(Dispatchers.IO) {
            val body = json
                .encodeToString(request)
                .toRequestBody(jsonMediaType)

            val requestUrl =
                baseUrl.trimEnd('/') + path

            val httpRequest = Request.Builder()
                .url(requestUrl)
                .post(body)
                .build()

            client.newCall(httpRequest)
                .execute()
                .use { response ->
                    val responseBody = response.body.string()
                    if (responseBody.isBlank()) {
                        throw IllegalStateException("Empty response from $path")
                    }
                    decode(responseBody)
                }
        }
    }
}