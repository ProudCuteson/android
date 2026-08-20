package top.role.android.feature.main.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

import top.role.android.app.navigation.MainDestination

@Composable
fun MainNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    dependencies: MainNavigationDependencies,
    onSignOut: () -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = MainDestination.Day.route,
        modifier = modifier,
    ) {
        dayNavGraph(
            navController = navController,
            dependencies = dependencies,
        )

        discoverNavGraph(
            navController = navController,
            dependencies =  dependencies,
        )

        chatNavGraph(
            navController = navController,
            dependencies = dependencies,
        )

        engageNavGraph(
            navController = navController,
            dependencies = dependencies,
        )

        accountNavGraph(
            navController = navController,
            dependencies = dependencies,
            onSignOut = onSignOut,
        )
    }
}