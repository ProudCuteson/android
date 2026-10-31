package top.role.android.app.navigation

object EntityDetailRoute {
    const val base = "entity-detail"
    const val typeArg = "type"
    const val idArg = "id"

    const val route = "$base/{$typeArg}/{$idArg}"

    fun create(
        type: String,
        id: String,
    ): String {
        return "$base/$type/$id"
    }
}