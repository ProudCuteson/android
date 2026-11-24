package top.role.android.app.navigation

import android.net.Uri

object PdfReaderRoute {
    const val route =
        "pdf_reader?title={title}&url={url}&assetId={assetId}"

    fun create(
        title: String,
        url: String,
        assetId: String,
    ): String {
        return "pdf_reader" +
                "?title=${Uri.encode(title)}" +
                "&url=${Uri.encode(url)}" +
                "&assetId=${Uri.encode(assetId)}"
    }
}