package top.role.android.feature.browse.pdf

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import java.io.Closeable
import java.io.File
import kotlin.math.roundToInt

/**
 * 一个 PDF 文档对应一个 Renderer。
 *
 * - PdfRenderer 接管文件描述符的所有权
 * - 页面渲染与文档关闭统一同步保护
 * - 限制 Bitmap 尺寸，避免异常内存占用
 */
class PdfDocumentSession private constructor(
    private val renderer: PdfRenderer,
) : Closeable {

    private var closed = false

    /**
     * 文档页数在 Session 创建时确定。
     */
    val pageCount: Int = renderer.pageCount

    @Synchronized
    fun renderPage(
        index: Int,
        targetWidth: Int,
    ): Bitmap {

        check(!closed) {
            "PDF 文档已经关闭"
        }

        require(index in 0 until pageCount) {
            "PDF 页面索引无效：$index"
        }

        require(targetWidth > 0) {
            "PDF 渲染宽度无效"
        }

        val page = renderer.openPage(index)

        try {
            val pageWidth = page.width
            val pageHeight = page.height

            require(pageWidth > 0 && pageHeight > 0) {
                "PDF 页面尺寸无效"
            }

            /**
             * 保持原始页面比例，
             * 同时限制最大宽度与高度。
             */
            val scale = minOf(
                targetWidth.toDouble() / pageWidth,
                MAX_RENDER_WIDTH.toDouble() / pageWidth,
                MAX_RENDER_HEIGHT.toDouble() / pageHeight,
            )

            val width = (pageWidth * scale)
                .roundToInt()
                .coerceAtLeast(1)

            val height = (pageHeight * scale)
                .roundToInt()
                .coerceAtLeast(1)

            val bitmap = Bitmap.createBitmap(
                width,
                height,
                Bitmap.Config.ARGB_8888,
            )

            try {
                bitmap.eraseColor(Color.WHITE)

                page.render(
                    bitmap,
                    null,
                    null,
                    PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY,
                )

                return bitmap

            } catch (error: Throwable) {
                bitmap.recycle()
                throw error
            }

        } finally {
            page.close()
        }
    }

    /**
     * PdfRenderer 负责关闭其持有的文件描述符。
     */
    @Synchronized
    override fun close() {
        if (closed) return

        renderer.close()
        closed = true
    }

    companion object {

        private const val MAX_RENDER_WIDTH = 2048
        private const val MAX_RENDER_HEIGHT = 3072

        fun open(file: File): PdfDocumentSession {

            require(file.exists() && file.isFile) {
                "PDF 文件不存在"
            }

            val descriptor = ParcelFileDescriptor.open(
                file,
                ParcelFileDescriptor.MODE_READ_ONLY,
            )

            val renderer = try {
                PdfRenderer(descriptor)
            } catch (error: Throwable) {
                runCatching {
                    descriptor.close()
                }
                throw error
            }

            return try {
                PdfDocumentSession(renderer)
            } catch (error: Throwable) {
                runCatching {
                    renderer.close()
                }
                throw error
            }
        }
    }
}