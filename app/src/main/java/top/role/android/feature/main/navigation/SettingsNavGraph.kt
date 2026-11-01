package top.role.android.feature.main.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import top.role.android.app.navigation.SettingsRoute
import top.role.android.core.preferences.AppAppearance
import top.role.android.core.preferences.WeekStart
import top.role.android.feature.settings.SettingsScreen

fun NavGraphBuilder.settingsNavGraph(
    appearance: AppAppearance,
    weekStart: WeekStart,
    onAppearanceChange: (AppAppearance) -> Unit,
    onWeekStartChange: (WeekStart) -> Unit,
    versionName: String,
    onOpenTerms: () -> Unit,
    onOpenPrivacy: () -> Unit,
) {
    composable(route = SettingsRoute.route) {
        SettingsScreen(
            appearance = appearance,
            weekStart = weekStart,
            onAppearanceChange = onAppearanceChange,
            onWeekStartChange = onWeekStartChange,
            versionName = versionName,
            onOpenTerms = onOpenTerms,
            onOpenPrivacy = onOpenPrivacy,
        )
    }
}