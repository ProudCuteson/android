package top.role.android.feature.main.navigation

import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.compose.runtime.LaunchedEffect
import top.role.android.core.auth.AuthState
import top.role.android.app.navigation.AccountPasswordRoute
import top.role.android.app.navigation.AccountRoute
import top.role.android.app.navigation.CreditsRoute
import top.role.android.feature.account.AccountScreen
import top.role.android.feature.account.AccountViewModel
import top.role.android.feature.account.password.AccountPasswordScreen
import top.role.android.feature.account.password.AccountPasswordViewModel
import top.role.android.feature.credit.CreditsScreen
import top.role.android.feature.credit.CreditsViewModel

fun NavGraphBuilder.accountNavGraph(
    navController: NavHostController,
    dependencies: MainNavigationDependencies,
    onRestoreAuth: suspend () -> Boolean,
    onLogin: () -> Unit,
    onRetryAuth: () -> Unit,
    onReSignIn: () -> Unit,
    onSignOut: () -> Unit,
) {

    // MARK: Account

    composable(route = AccountRoute.route) {

        val signedInUserId =
            (dependencies.authState as? AuthState.SignedIn)
                ?.userId

        val authenticated = signedInUserId != null

        val accountViewModel: AccountViewModel =
            viewModel(
                key = "account:${signedInUserId ?: "signed-out"}",
                factory = AccountViewModel.factory(
                    repository = dependencies.accountRepository,
                ),
            )

        DisposableEffect(
            accountViewModel,
            authenticated,
        ) {
            accountViewModel.setActive(authenticated)

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
            onRestoreAuth = onRestoreAuth,
            onOpenCredits = {
                navController.navigate(CreditsRoute.route)
            },
            onOpenPassword = {
                navController.navigate(AccountPasswordRoute.route)
            },
            onReSignIn = onReSignIn,
            onSignOut = onSignOut,
        )
    }

    // MARK: Credits

    composable(route = CreditsRoute.route) {

        val viewModel: CreditsViewModel =
            viewModel(
                factory = CreditsViewModel.factory(
                    repository =
                        dependencies.creditsRepository,

                    wechatPayLauncher =
                        dependencies.wechatPayLauncher,
                ),
            )

        LaunchedEffect(dependencies.authState) {
            viewModel.setAuthState(dependencies.authState)
        }

        CreditsScreen(
            viewModel = viewModel,
            authState = dependencies.authState,
            onLogin = onReSignIn,
            onRetryAuth = onReSignIn,
            onReSignIn = onReSignIn,
        )

    }

    // MARK: Account Password

    composable(route = AccountPasswordRoute.route) {

        val viewModel: AccountPasswordViewModel =
            viewModel(
                factory = AccountPasswordViewModel.factory(
                    accountRepository =
                        dependencies.accountRepository,

                    passwordRepository =
                        dependencies.accountPasswordRepository,
                ),
            )

        AccountPasswordScreen(
            viewModel = viewModel,
            onReSignIn = onReSignIn,
        )
    }
}
