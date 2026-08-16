package top.role.android.feature.main.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable

import top.role.android.app.navigation.MainDestination
import top.role.android.feature.chat.ChatScreen

fun NavGraphBuilder.chatNavGraph(
    navController: NavHostController,
    dependencies: MainNavigationDependencies,
) {
    composable(
        route =
            MainDestination.Chat.route,
    ) {
        ChatScreen()
    }
}