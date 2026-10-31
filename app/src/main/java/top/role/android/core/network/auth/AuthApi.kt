package top.role.android.core.network.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import top.role.android.core.network.ApiJson

class AuthApi(
    private val baseUrl: String,
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