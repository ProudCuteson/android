package top.role.android.feature.main.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable

import top.role.android.app.navigation.MainDestination
import top.role.android.app.navigation.SurveyListRoute
import top.role.android.app.navigation.BountyListRoute
import top.role.android.app.navigation.CreditsRoute
import top.role.android.app.navigation.MarketListRoute
import top.role.android.feature.banner.BannerRepository
import top.role.android.app.navigation.InAppBrowserRoute
import top.role.android.feature.main.QuickStartScreen

fun NavGraphBuilder.quickStartNavGraph(
    navController: NavHostController,
    bannerRepository: BannerRepository,
) {
    composable(
        route = MainDestination.QuickStart.route,
    ) {
        QuickStartScreen(
            bannerRepository = bannerRepository,
            onBannerClick = { href ->
                when {
                    href.startsWith("https://") ||
                            href.startsWith("http://") -> {
                        navController.navigate(
                            InAppBrowserRoute.create(
                                title = "了解更多",
                                url = href,
                            )
                        )
                    }

                    else -> {
                        navController.navigate(href) {
                            launchSingleTop = true
                        }
                    }
                }
            },
            onOpenSurvey = {
                navController.navigate(SurveyListRoute.route)
            },
            onOpenBounty = {
                navController.navigate(BountyListRoute.route)
            },
            onOpenCredits = {
                navController.navigate(CreditsRoute.route)
            },
            onOpenMarket = {
                navController.navigate(MarketListRoute.route)
            },
        )
    }
}