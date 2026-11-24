package top.role.android.feature.main.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

import top.role.android.app.navigation.InAppBrowserRoute
import top.role.android.app.navigation.PdfReaderRoute

import top.role.android.feature.browse.InAppBrowserScreen
import top.role.android.feature.browse.pdf.PdfDocumentLoader
import top.role.android.feature.browse.pdf.PdfReaderRouteScreen


fun NavGraphBuilder.inAppBrowserNavGraph(
    navController: NavHostController,
) {
    composable(
        route = InAppBrowserRoute.route,
        arguments = listOf(
            navArgument("title") {
                type = NavType.StringType
                defaultValue = ""
            },
            navArgument("url") {
                type = NavType.StringType
                defaultValue = ""
            },
        ),
    ) { backStackEntry ->

        val title = backStackEntry.arguments
            ?.getString("title")
            .orEmpty()

        val url = backStackEntry.arguments
            ?.getString("url")
            .orEmpty()

        InAppBrowserScreen(
            title = title,
            url = url,
            onBack = {
                navController.popBackStack()
            },
        )
    }
}