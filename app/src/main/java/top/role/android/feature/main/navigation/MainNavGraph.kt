package top.role.android.feature.main.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

import top.role.android.BuildConfig
import top.role.android.app.navigation.MainDestination
import top.role.android.core.preferences.AppAppearance
import top.role.android.core.preferences.WeekStart
import top.role.android.app.navigation.InAppBrowserRoute
import top.role.android.feature.browse.pdf.PdfDocumentLoader

@Composable
fun MainNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    dependencies: MainNavigationDependencies,
    pdfDocumentLoader: PdfDocumentLoader,
    appearance: AppAppearance,
    weekStart: WeekStart,
    onAppearanceChange: (AppAppearance) -> Unit,
    onWeekStartChange: (WeekStart) -> Unit,
    onLogin: () -> Unit,
    onRetryAuth: () -> Unit,
    onRestoreAuth: suspend () -> Boolean,
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
            onLogin = onLogin,
            onRetryAuth = onRetryAuth,
            onRestoreAuth = onRestoreAuth,
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
            versionName = BuildConfig.VERSION_NAME,
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

        inAppBrowserNavGraph(
            navController = navController,
        )

        pdfReaderNavGraph(
            navController = navController,
            pdfDocumentLoader = pdfDocumentLoader,
        )

    }
}