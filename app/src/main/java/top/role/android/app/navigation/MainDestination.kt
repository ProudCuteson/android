package top.role.android.app.navigation

enum class MainDestination(
    val route: String,
    val label: String,
) {
    Day(
        route = "day",
        label = "记录",
    ),
    Discover(
        route = "discover",
        label = "发现",
    ),
    Chat(
        route = "chat",
        label = "聊天",
    ),
    Engage(
        route = "engage",
        label = "参与",
    ),
}