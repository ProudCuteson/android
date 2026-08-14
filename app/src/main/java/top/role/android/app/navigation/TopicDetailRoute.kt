package top.role.android.app.navigation

object TopicDetailRoute {

    const val base = "topic-detail"

    const val idArg = "id"
    const val publishIdArg = "publishId"

    const val route =
        "$base/{$idArg}?publishId={$publishIdArg}"

    fun create(
        id: String,
        publishId: String? = null,
    ): String {
        val baseRoute = "$base/$id"

        return if (publishId.isNullOrBlank()) {
            baseRoute
        } else {
            "$baseRoute?publishId=$publishId"
        }
    }
}