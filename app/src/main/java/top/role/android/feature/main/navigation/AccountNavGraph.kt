package top.role.android.feature.main.navigation

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable

import top.role.android.app.navigation.AccountPasswordRoute
import top.role.android.app.navigation.AccountRoute
import top.role.android.app.navigation.CreditsRoute
import top.role.android.app.navigation.SettingsRoute

import top.role.android.feature.account.AccountScreen
import top.role.android.feature.account.AccountViewModel


fun NavGraphBuilder.accountNavGraph(
    navController:
    NavHostController,
    dependencies:
    MainNavigationDependencies,
    onReSignIn:
        () -> Unit,
    onSignOut:
        () -> Unit,
) {

    composable(
        route =
            AccountRoute.route,
    ) {

        val viewModel:
                AccountViewModel =
            viewModel(
                factory =
                    AccountViewModel.factory(
                        repository =
                            dependencies
                                .accountRepository,
                    ),
            )

        AccountScreen(
            viewModel =
                viewModel,

            onOpenCredits = {
                navController.navigate(
                    CreditsRoute.route
                )
            },

            onOpenPassword = {
                navController.navigate(
                    AccountPasswordRoute.route
                )
            },

            onOpenSettings = {
                navController.navigate(
                    SettingsRoute.route
                )
            },

            onReSignIn =
                onReSignIn,

            onSignOut =
                onSignOut,
        )
    }
}