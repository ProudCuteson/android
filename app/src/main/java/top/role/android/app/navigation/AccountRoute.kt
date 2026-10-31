package top.role.android.app.navigation

import android.net.Uri

object AccountRoute {
    const val route = "account"
}

object CreditsRoute {
    const val route = "credits"
}

object AccountPasswordRoute {
    const val route = "account/password"
}

object InAppBrowserRoute {
    const val route = "browser?title={title}&url={url}"

    fun create(
        title: String,
        url: String,
    ): String {
        return "browser" +
                "?title=${Uri.encode(title)}" +
                "&url=${Uri.encode(url)}"
    }
}