package top.role.android.core.data.note.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import top.role.android.core.network.ApiConfig

class NoteAiRemoteDataSource(
    private val httpClient:
    OkHttpClient,

    private val baseUrl:
    String =
        ApiConfig.BASE_URL,
) {

    private val json =
        Json {
            ignoreUnknownKeys = true
        }

    suspend fun generate(
        input: String,
        mode: NoteAiMode,
    ): NoteAiGenerateResponse {
        val normalizedInput =
            input.trim()

        require(
            normalizedInput.isNotEmpty(),
        ) {
            "NOTE_AI_INPUT_REQUIRED"
        }

        val payload =
            NoteAiGenerateRequest(
                input =
                    normalizedInput,

                mode =
                    mode.rawValue,
            )

        val requestBody =
            json
                .encodeToString(
                    payload,
                )
                .toRequestBody(
                    JSON_MEDIA_TYPE,
                )

        val request =
            Request.Builder()
                .url(
                    buildGenerateUrl(),
                )
                .post(
                    requestBody,
                )
                .header(
                    "Content-Type",
                    "application/json",
                )
                .build()

        return withContext(
            Dispatchers.IO,
        ) {
            httpClient
                .newCall(
                    request,
                )
                .execute()
                .use { response ->
                    val rawBody =
                        response.body
                            ?.string()
                            .orEmpty()

                    val decoded =
                        runCatching {
                            json.decodeFromString<
                                    NoteAiGenerateResponse
                                    >(
                                rawBody,
                            )
                        }
                            .getOrElse {
                                throw IllegalStateException(
                                    "NOTE_AI_INVALID_RESPONSE: " +
                                            rawBody.take(
                                                300,
                                            ),
                                    it,
                                )
                            }

                    if (
                        !response.isSuccessful ||
                        !decoded.ok
                    ) {
                        throw IllegalStateException(
                            decoded.error
                                ?.takeIf {
                                    it.isNotBlank()
                                }
                                ?: "NOTE_AI_REQUEST_FAILED"
                        )
                    }

                    decoded
                }
        }
    }

    private fun buildGenerateUrl():
            String {
        return baseUrl
            .trimEnd(
                '/',
            ) +
                "/api/ai/note/mobile/generate"
    }

    private companion object {
        val JSON_MEDIA_TYPE =
            "application/json; charset=utf-8"
                .toMediaType()
    }
}