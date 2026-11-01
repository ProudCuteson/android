package top.role.android.feature.main.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

import top.role.android.app.navigation.MainDestination
import top.role.android.core.preferences.AppAppearance
import top.role.android.core.preferences.WeekStart
import top.role.android.app.navigation.InAppBrowserRoute

@Composable
fun MainNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    dependencies: MainNavigationDependencies,
    appearance: AppAppearance,
    weekStart: WeekStart,
    onAppearanceChange: (AppAppearance) -> Unit,
    onWeekStartChange: (WeekStart) -> Unit,
    onLogin: () -> Unit,
    onRetryAuth: () -> Unit,
    onReSignIn: () -> Unit,
    onSignOut: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = MainDestination.Day.route,
        modifier = modifier,
    ) {
        dayNavGraph(
            navController = navController,
            dependencies = dependencies,
            weekStart = weekStart,
        )

        discoverNavGraph(
            navController = navController,
            dependencies =  dependencies,
            onLogin = onLogin,
            onRetryAuth = onRetryAuth,
            onReSignIn = onReSignIn,
        )

        chatNavGraph(
            navController = navController,
            dependencies = dependencies,
            onLogin = onLogin,
            onRetryAuth = onRetryAuth,
            onReSignIn = onReSignIn,
        )

        engageNavGraph(
            navController = navController,
            dependencies = dependencies,
            onLogin = onLogin,
            onRetryAuth = onRetryAuth,
            onReSignIn = onReSignIn,
        )

        accountNavGraph(
            navController = navController,
            dependencies = dependencies,
            onReSignIn = onReSignIn,
            onSignOut = onSignOut,
        )

        quickStartNavGraph(
            navController = navController,
            bannerRepository = dependencies.bannerRepository,
        )

        settingsNavGraph(
            appearance = appearance,
            weekStart = weekStart,
            onAppearanceChange = onAppearanceChange,
            onWeekStartChange = onWeekStartChange,
            versionName = "1.0",
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
        )

        browserNavGraph(
            navController = navController,
        )

    }
}