package top.role.android.core.asset

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

/**
 * 已经解析完成、可以直接上传的本地文件。
 */
data class ResolvedAssetUploadFile(
    val file: File,
    val fileName: String,
    val mime: String,
    val bytes: Long,
) {
    fun cleanup() {
        file.delete()
    }
}

class AssetUploadFileResolver(
    context: Context,
) {

    private val appContext =
        context.applicationContext

    private val resolver =
        appContext.contentResolver

    private val cacheDirectory =
        File(
            appContext.cacheDir,
            "asset-uploads",
        )

    companion object {

        /**
         * Android 默认文件大小限制。
         *
         * 后端仍负责最终大小校验。
         */
        const val DEFAULT_MAX_BYTES =
            100L * 1024L * 1024L
    }

    /**
     * 将系统文件选择器返回的 Uri
     * 解析为可上传的临时文件。
     */
    suspend fun resolve(
        uri: Uri,
        options: AssetUploadOptions,
    ): ResolvedAssetUploadFile =
        withContext(Dispatchers.IO) {

            val metadata = readMetadata(uri)

            val fileName = metadata.fileName

            val mime = resolveMime(
                uri = uri,
                fileName = fileName,
            )

            val maxBytes =
                options.maxBytes
                    ?: DEFAULT_MAX_BYTES

            require(maxBytes > 0) {
                "Invalid file size limit"
            }

            validateMime(
                mime = mime,
                options = options,
            )

            metadata.bytes?.let { bytes ->
                if (bytes > maxBytes) {
                    error("文件大小超过限制")
                }
            }

            if (
                !cacheDirectory.exists() &&
                !cacheDirectory.mkdirs()
            ) {
                error("无法创建上传缓存目录")
            }

            val tempFile = File.createTempFile(
                "asset-",
                ".upload",
                cacheDirectory,
            )

            try {

                val input = resolver
                    .openInputStream(uri)
                    ?: error("无法读取文件")

                var totalBytes = 0L

                input.use { source ->

                    tempFile.outputStream().use { target ->

                        val buffer =
                            ByteArray(64 * 1024)

                        while (true) {

                            currentCoroutineContext()
                                .ensureActive()

                            val count =
                                source.read(buffer)

                            if (count < 0) {
                                break
                            }

                            totalBytes += count

                            if (totalBytes > maxBytes) {
                                error("文件大小超过限制")
                            }

                            target.write(
                                buffer,
                                0,
                                count,
                            )
                        }
                    }
                }

                if (totalBytes <= 0) {
                    error("文件内容为空")
                }

                ResolvedAssetUploadFile(
                    file = tempFile,
                    fileName = fileName,
                    mime = mime,
                    bytes = totalBytes,
                )

            } catch (error: Throwable) {

                tempFile.delete()

                throw error
            }
        }

    /**
     * 文件提供者返回的元数据。
     *
     * bytes 可能为空，
     * 最终以实际读取的文件大小为准。
     */
    private data class FileMetadata(
        val fileName: String,
        val bytes: Long?,
    )

    private fun readMetadata(
        uri: Uri,
    ): FileMetadata {

        var fileName: String? = null
        var bytes: Long? = null

        resolver.query(
            uri,
            arrayOf(
                OpenableColumns.DISPLAY_NAME,
                OpenableColumns.SIZE,
            ),
            null,
            null,
            null,
        )?.use { cursor ->

            if (cursor.moveToFirst()) {

                val nameIndex =
                    cursor.getColumnIndex(
                        OpenableColumns.DISPLAY_NAME,
                    )

                if (
                    nameIndex >= 0 &&
                    !cursor.isNull(nameIndex)
                ) {
                    fileName =
                        cursor.getString(nameIndex)
                }

                val sizeIndex =
                    cursor.getColumnIndex(
                        OpenableColumns.SIZE,
                    )

                if (
                    sizeIndex >= 0 &&
                    !cursor.isNull(sizeIndex)
                ) {
                    bytes = cursor
                        .getLong(sizeIndex)
                        .takeIf { it >= 0 }
                }
            }
        }

        return FileMetadata(
            fileName = fileName
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?: "未命名文件",

            bytes = bytes,
        )
    }

    /**
     * 优先使用 ContentResolver 提供的 MIME。
     *
     * PDF 保留扩展名 fallback。
     */
    private fun resolveMime(
        uri: Uri,
        fileName: String,
    ): String {

        val mime = resolver.getType(uri)
            ?.substringBefore(";")
            ?.trim()
            ?.lowercase(Locale.ROOT)
            ?.takeIf { it.isNotEmpty() }

        if (
            mime != null &&
            mime != "application/octet-stream"
        ) {
            return mime
        }

        if (
            fileName.endsWith(
                ".pdf",
                ignoreCase = true,
            )
        ) {
            return "application/pdf"
        }

        return mime
            ?: "application/octet-stream"
    }

    private fun validateMime(
        mime: String,
        options: AssetUploadOptions,
    ) {

        if (
            options.allowedMimes.isNotEmpty() &&
            mime !in options.allowedMimes
        ) {
            error("不支持该文件格式")
        }
    }
}