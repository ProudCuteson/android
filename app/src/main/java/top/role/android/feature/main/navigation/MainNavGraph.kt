package top.role.android.feature.main.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import top.role.android.feature.note.editor.NoteRichTextPlayground

import top.role.android.app.navigation.MainDestination
import top.role.android.core.preferences.AppAppearance
import top.role.android.core.preferences.WeekStart

@Composable
fun MainNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    dependencies: MainNavigationDependencies,
    appearance: AppAppearance,
    weekStart: WeekStart,
    onAppearanceChange: (AppAppearance) -> Unit,
    onWeekStartChange: (WeekStart) -> Unit,
    onReSignIn: () -> Unit,
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
            weekStart = weekStart,
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
            appearance = appearance,
            weekStart = weekStart,
            onAppearanceChange = onAppearanceChange,
            onWeekStartChange = onWeekStartChange,
            onReSignIn = onReSignIn,
            onSignOut = onSignOut,
        )

        browserNavGraph(
            navController =
                navController,
        )

        composable("note/rich-text-playground") {
            NoteRichTextPlayground()
        }
    }
}