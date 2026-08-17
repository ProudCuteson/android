package top.role.android.core.data.image

import android.content.ContentResolver
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.BufferedSink
import top.role.android.core.model.image.MobileImageUploadAuth
import java.io.File
import java.io.InputStream

class AliyunOssImageUploader(
    private val contentResolver: ContentResolver,
    private val httpClient: OkHttpClient,
) : ImageObjectUploader {

    override suspend fun upload(
        sourceUri: Uri,
        mime: String,
        bytes: Long,
        auth: MobileImageUploadAuth,
    ) {
        withContext(
            Dispatchers.IO,
        ) {
            require(
                auth.uploadUrl.isNotBlank(),
            ) {
                "IMAGE_UPLOAD_URL_REQUIRED"
            }

            require(
                bytes > 0L,
            ) {
                "IMAGE_UPLOAD_BYTES_INVALID"
            }

            val requestBody =
                buildMultipartBody(
                    sourceUri = sourceUri,
                    mime = mime,
                    bytes = bytes,
                    auth = auth,
                )

            val request =
                Request.Builder()
                    .url(
                        auth.uploadUrl,
                    )
                    .post(
                        requestBody,
                    )
                    .build()

            httpClient
                .newCall(
                    request,
                )
                .execute()
                .use { response ->
                    if (
                        !response.isSuccessful
                    ) {
                        val responseText =
                            runCatching {
                                response.body
                                    ?.string()
                            }
                                .getOrNull()
                                .orEmpty()

                        error(
                            buildString {
                                append(
                                    "IMAGE_OSS_UPLOAD_FAILED"
                                )

                                append(
                                    ": HTTP "
                                )

                                append(
                                    response.code
                                )

                                if (
                                    responseText.isNotBlank()
                                ) {
                                    append(
                                        " - "
                                    )

                                    append(
                                        responseText.take(
                                            500,
                                        )
                                    )
                                }
                            }
                        )
                    }
                }
        }
    }

    private fun buildMultipartBody(
        sourceUri: Uri,
        mime: String,
        bytes: Long,
        auth: MobileImageUploadAuth,
    ): MultipartBody {
        val builder =
            MultipartBody
                .Builder()
                .setType(
                    MultipartBody.FORM,
                )

        /*
         * OSS POST policy 字段由后端生成，
         * Android 原样提交。
         */
        auth.formData
            .forEach { (
                           name,
                           value,
                       ) ->
                builder.addFormDataPart(
                    name,
                    value,
                )
            }

        /*
         * OSS PostObject 每次只允许一个 file 字段。
         */
        builder.addFormDataPart(
            "file",
            auth.fileName,
            UriRequestBody(
                contentResolver =
                    contentResolver,

                sourceUri =
                    sourceUri,

                contentType =
                    mime,

                bytes =
                    bytes,
            ),
        )

        return builder.build()
    }
}

private class UriRequestBody(
    private val contentResolver: ContentResolver,
    private val sourceUri: Uri,
    private val contentType: String,
    private val bytes: Long,
) : RequestBody() {

    override fun contentType() =
        contentType
            .toMediaTypeOrNull()

    override fun contentLength(): Long {
        return bytes
    }

    override fun writeTo(
        sink: BufferedSink,
    ) {
        val inputStream =
            openInputStream()
                ?: error(
                    "IMAGE_SOURCE_OPEN_FAILED"
                )

        inputStream.use { input ->
            val buffer =
                ByteArray(
                    DEFAULT_BUFFER_SIZE,
                )

            while (true) {
                val read =
                    input.read(
                        buffer,
                    )

                if (
                    read < 0
                ) {
                    break
                }

                sink.write(
                    buffer,
                    0,
                    read,
                )
            }
        }
    }

    private fun openInputStream(): InputStream? {
        return when (
            sourceUri.scheme
        ) {
            ContentResolver.SCHEME_FILE ->
                sourceUri.path
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?.let { path ->
                        File(
                            path,
                        ).inputStream()
                    }

            else ->
                contentResolver
                    .openInputStream(
                        sourceUri,
                    )
        }
    }
}