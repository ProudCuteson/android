package top.role.android.feature.main.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

import top.role.android.app.navigation.InAppBrowserRoute
import top.role.android.feature.browser.InAppBrowserScreen


fun NavGraphBuilder.browserNavGraph(
    navController: NavHostController,
) {

    composable(
        route =
            InAppBrowserRoute.route,

        arguments =
            listOf(
                navArgument(
                    "title"
                ) {
                    type =
                        NavType.StringType

                    defaultValue =
                        ""
                },

                navArgument(
                    "url"
                ) {
                    type =
                        NavType.StringType

                    defaultValue =
                        ""
                },
            ),
    ) { backStackEntry ->

        val title =
            backStackEntry
                .arguments
                ?.getString(
                    "title"
                )
                .orEmpty()


        val url =
            backStackEntry
                .arguments
                ?.getString(
                    "url"
                )
                .orEmpty()


        InAppBrowserScreen(
            title =
                title,

            url =
                url,

            onBack = {
                navController
                    .popBackStack()
            },
        )
    }
}