
package top.role.android.core.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import top.role.android.core.storage.SessionStorage

data class AppHttpResponse(
    val statusCode: Int,
    val body: String,
)

class AppHttpClient(
    private val httpClient: OkHttpClient,
    private val sessionStorage: SessionStorage,
    private val baseUrl: String,
) {

    private val jsonMediaType =
        "application/json; charset=utf-8".toMediaType()

    /**
     * GET 请求
     */
    suspend fun get(
        path: String,
    ): AppHttpResponse {
        return execute(path) {
            get()
        }
    }

    /**
     * POST JSON 请求
     */
    suspend fun postJson(
        path: String,
        jsonBody: String,
    ): AppHttpResponse {
        return execute(path) {
            post(
                jsonBody.toRequestBody(jsonMediaType)
            )
        }
    }

    /**
     * 统一 HTTP 请求执行
     */
    private suspend fun execute(
        path: String,
        configure: Request.Builder.() -> Request.Builder,
    ): AppHttpResponse {

        val session = sessionStorage.session.first()

        val accessToken = session
            ?.accessToken
            ?.trim()
            .orEmpty()

        if (accessToken.isBlank()) {
            return AppHttpResponse(
                statusCode = 401,
                body = """
                    {
                      "ok": false,
                      "code": "APP_NOT_AUTHENTICATED"
                    }
                """.trimIndent(),
            )
        }

        val url =
            baseUrl.trimEnd('/') +
                    "/" +
                    path.trimStart('/')

        val request = Request.Builder()
            .url(url)
            .header(
                "Authorization",
                "Bearer $accessToken",
            )
            .header(
                "Accept",
                "application/json",
            )
            .configure()
            .build()

        return withContext(Dispatchers.IO) {
            httpClient
                .newCall(request)
                .execute()
                .use { response ->
                    AppHttpResponse(
                        statusCode = response.code,
                        body = response.body
                            ?.string()
                            .orEmpty(),
                    )
                }
        }
    }
}