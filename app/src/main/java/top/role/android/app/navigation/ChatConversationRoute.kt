package top.role.android.app.navigation

object ChatConversationRoute {
    const val conversationIdArg = "conversationId"

    const val route =
        "chat/conversation/{$conversationIdArg}"

    fun create(
        conversationId: String,
    ): String {
        return "chat/conversation/$conversationId"
    }
}

object ChatGroupCreateRoute {
    const val route =
        "chat/group-create"
}

object ChatGroupSettingsRoute {

    const val conversationIdArg =
        "conversationId"

    const val route =
        "chat/group/{$conversationIdArg}"

    fun create(
        conversationId: String,
    ): String {
        return "chat/group/$conversationId"
    }
}