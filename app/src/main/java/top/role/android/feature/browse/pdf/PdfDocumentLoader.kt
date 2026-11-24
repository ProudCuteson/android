package top.role.android.feature.browse.pdf

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

/**
 * PDF 下载与缓存。
 *
 * - 使用后端提供的临时签名 URL
 * - 缓存保存在应用私有 Cache 目录
 * - 不持久化签名 URL
 * - 下载完成后验证 PDF 文件头
 */
class PdfDocumentLoader(
    context: Context,
    private val client: OkHttpClient,
) {
    private val cacheDir =
        File(context.cacheDir, "pdf-documents").apply {
            mkdirs()
        }

    suspend fun download(
        url: String,
        cacheKey: String,
    ): File = withContext(Dispatchers.IO) {

        require(url.startsWith("https://")) {
            "PDF URL 必须使用 HTTPS"
        }

        require(cacheKey.isNotBlank()) {
            "PDF cacheKey 不能为空"
        }

        val hash = MessageDigest
            .getInstance("SHA-256")
            .digest(cacheKey.toByteArray())
            .joinToString("") {
                "%02x".format(it)
            }

        val target = File(cacheDir, "$hash.pdf")

        // 已有有效缓存，直接使用。
        if (isValidPdf(target)) {
            return@withContext target
        }

        target.delete()

        val tempFile = File(
            cacheDir,
            "$hash-${System.nanoTime()}.download",
        )

        try {
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            client.newCall(request).execute().use { response ->

                check(response.isSuccessful) {
                    "PDF 下载失败：HTTP ${response.code}"
                }

                val body = requireNotNull(response.body) {
                    "PDF 响应内容为空"
                }

                tempFile.outputStream().buffered().use { output ->
                    body.byteStream().use { input ->
                        input.copyTo(output)
                    }
                }
            }

            check(isValidPdf(tempFile)) {
                "下载的文件不是有效的 PDF"
            }

            check(tempFile.renameTo(target)) {
                "PDF 缓存文件保存失败"
            }

            target
        } catch (error: Exception) {
            tempFile.delete()
            throw error
        }
    }

    private fun isValidPdf(file: File): Boolean {
        if (!file.exists() || file.length() < 5) {
            return false
        }

        return runCatching {
            FileInputStream(file).use { input ->
                val header = ByteArray(5)
                input.read(header) == 5 &&
                        String(header, Charsets.US_ASCII) == "%PDF-"
            }
        }.getOrDefault(false)
    }
}
