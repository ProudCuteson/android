package top.role.android.feature.main.navigation

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.compose.runtime.DisposableEffect

import top.role.android.app.navigation.AccountPasswordRoute
import top.role.android.app.navigation.AccountRoute
import top.role.android.app.navigation.CreditsRoute
import top.role.android.app.navigation.InAppBrowserRoute

import top.role.android.core.preferences.AppAppearance
import top.role.android.core.preferences.WeekStart

import top.role.android.feature.account.AccountScreen
import top.role.android.feature.account.AccountViewModel
import top.role.android.feature.account.password.AccountPasswordScreen
import top.role.android.feature.account.password.AccountPasswordViewModel

import top.role.android.feature.credit.CreditsScreen
import top.role.android.feature.credit.CreditsViewModel


fun NavGraphBuilder.accountNavGraph(
    navController: NavHostController,
    dependencies: MainNavigationDependencies,

    appearance: AppAppearance,
    weekStart: WeekStart,

    onAppearanceChange: (AppAppearance) -> Unit,
    onWeekStartChange: (WeekStart) -> Unit,

    onReSignIn: () -> Unit,
    onSignOut: () -> Unit,
) {


    composable(route = AccountRoute.route) {

        val authenticated = dependencies.remoteAuthenticated

        val accountViewModel: AccountViewModel =
            viewModel(
                key = "account:${dependencies.localOwnerUserId}",
                factory = AccountViewModel.factory(
                    repository =
                        dependencies.accountRepository,
                ),
            )

        DisposableEffect(
            accountViewModel,
            authenticated,
        ) {
            accountViewModel.setActive(
                authenticated
            )

            onDispose {
                accountViewModel.setActive(false)
            }
        }

        AccountScreen(
            viewModel = if (authenticated) {
                    accountViewModel
                } else {
                    null
                },

            remoteAuthenticated = authenticated,

            appearance = appearance,
            weekStart = weekStart,

            onAppearanceChange =
                onAppearanceChange,

            onWeekStartChange =
                onWeekStartChange,

            versionName = "1.0",

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

            onOpenTerms = {
                navController.navigate(
                    InAppBrowserRoute.create(
                        title = "用户协议",
                        url = "https://role.top/home/term",
                    )
                )
            },

            onOpenPrivacy = {
                navController.navigate(
                    InAppBrowserRoute.create(
                        title = "隐私政策",
                        url = "https://role.top/home/privacy",
                    )
                )
            },

            onReSignIn = onReSignIn,
            onSignOut = onSignOut,
        )
    }

    composable(route = CreditsRoute.route) {
        val viewModel: CreditsViewModel = viewModel(
            factory = CreditsViewModel.factory(
                repository = dependencies.creditsRepository,
                wechatPayLauncher = dependencies.wechatPayLauncher,
                ),
            )
        CreditsScreen(viewModel = viewModel)
    }

    composable(route = AccountPasswordRoute.route) {

        val viewModel: AccountPasswordViewModel = viewModel(
            factory = AccountPasswordViewModel.factory(
                accountRepository = dependencies.accountRepository,
                passwordRepository = dependencies.accountPasswordRepository,
                ),
            )

        AccountPasswordScreen(
            viewModel = viewModel,
            onReSignIn = onReSignIn,
        )
    }
}