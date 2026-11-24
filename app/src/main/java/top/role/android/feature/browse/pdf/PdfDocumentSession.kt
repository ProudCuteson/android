package top.role.android.feature.browse.pdf

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import java.io.Closeable
import java.io.File

/**
 * 一个 PDF 文档对应一个 Renderer。
 *
 * PdfRenderer 不支持并发操作，因此所有渲染和关闭
 * 操作统一同步保护。
 */
class PdfDocumentSession private constructor(
    private val descriptor: ParcelFileDescriptor,
    private val renderer: PdfRenderer,
) : Closeable {

    private var closed = false

    val pageCount: Int
        @Synchronized
        get() {
            check(!closed) {
                "PDF 文档已经关闭"
            }
            return renderer.pageCount
        }

    @Synchronized
    fun renderPage(
        index: Int,
        targetWidth: Int,
    ): Bitmap {
        check(!closed) {
            "PDF 文档已经关闭"
        }

        require(index in 0 until renderer.pageCount) {
            "PDF 页面索引无效：$index"
        }

        require(targetWidth > 0) {
            "PDF 渲染宽度无效"
        }

        val page = renderer.openPage(index)

        try {
            val width = targetWidth

            val height = (
                    page.height.toFloat() *
                            width / page.width
                    ).toInt().coerceAtLeast(1)

            val bitmap = Bitmap.createBitmap(
                width,
                height,
                Bitmap.Config.ARGB_8888,
            )

            page.render(
                bitmap,
                null,
                null,
                PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY,
            )

            return bitmap
        } finally {
            page.close()
        }
    }

    @Synchronized
    override fun close() {
        if (closed) return

        closed = true

        try {
            renderer.close()
        } finally {
            descriptor.close()
        }
    }

    companion object {
        fun open(file: File): PdfDocumentSession {
            require(file.exists() && file.isFile) {
                "PDF 文件不存在"
            }

            val descriptor = ParcelFileDescriptor.open(
                file,
                ParcelFileDescriptor.MODE_READ_ONLY,
            )

            return try {
                val renderer = PdfRenderer(descriptor)

                PdfDocumentSession(
                    descriptor = descriptor,
                    renderer = renderer,
                )
            } catch (error: Exception) {
                descriptor.close()
                throw error
            }
        }
    }
}
