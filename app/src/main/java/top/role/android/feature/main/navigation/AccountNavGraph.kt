package top.role.android.feature.main.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable

import top.role.android.feature.account.AccountScreen
import top.role.android.feature.main.ACCOUNT_ROUTE

fun NavGraphBuilder.accountNavGraph(
    navController: NavHostController,
    dependencies: MainNavigationDependencies,
    onSignOut: () -> Unit,
) {
    composable(
        route =
            ACCOUNT_ROUTE,
    ) {
        AccountScreen(
            onSignOut =
                onSignOut,
        )
    }
}