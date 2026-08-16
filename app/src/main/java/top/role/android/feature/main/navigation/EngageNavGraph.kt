package top.role.android.feature.main.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable

import top.role.android.app.navigation.MainDestination
import top.role.android.feature.engage.EngageScreen

fun NavGraphBuilder.engageNavGraph(
    navController: NavHostController,
    dependencies: MainNavigationDependencies,
) {
    composable(
        route =
            MainDestination.Engage.route,
    ) {
        EngageScreen()
    }
}