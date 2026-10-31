package top.role.android.app.navigation

object DocDetailRoute {

    const val base = "doc-detail"

    const val docIdArg = "docId"
    const val topicIdArg = "topicId"

    const val route = "$base/{$docIdArg}?$topicIdArg={$topicIdArg}"

    fun create(
        docId: String,
        topicId: String? = null,
    ): String {
        val baseRoute = "$base/$docId"

        return if (topicId.isNullOrBlank()) {
            baseRoute
        } else {
            "$baseRoute?$topicIdArg=$topicId"
        }
    }
}