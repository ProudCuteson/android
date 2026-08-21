package top.role.android.app.navigation

object MembershipRoute {

    const val spaceIdArg =
        "spaceId"

    const val route =
        "membership/{$spaceIdArg}"

    fun create(
        spaceId: String,
    ): String {
        return "membership/$spaceId"
    }
}