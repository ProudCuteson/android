package top.role.android.app.navigation

object SlideDetailRoute {

    const val slideIdArg =
        "slideId"

    const val route =
        "slide/{$slideIdArg}"

    fun create(
        slideId: String,
    ): String {
        return "slide/$slideId"
    }
}