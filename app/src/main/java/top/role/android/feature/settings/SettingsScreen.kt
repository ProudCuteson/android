package top.role.android.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.role.android.core.preferences.AppAppearance
import top.role.android.core.preferences.WeekStart
import top.role.android.feature.settings.components.SettingsAboutSection
import top.role.android.feature.settings.components.SettingsPreferencesSection

@Composable
fun SettingsScreen(
    appearance: AppAppearance,
    weekStart: WeekStart,
    onAppearanceChange: (AppAppearance) -> Unit,
    onWeekStartChange: (WeekStart) -> Unit,
    versionName: String,
    onOpenTerms: () -> Unit,
    onOpenPrivacy: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier,
        contentPadding = PaddingValues(
            horizontal = 20.dp,
            vertical = 20.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item {
            SettingsPreferencesSection(
                appearance = appearance,
                weekStart = weekStart,
                onAppearanceChange = onAppearanceChange,
                onWeekStartChange = onWeekStartChange,
            )
        }

        item {
            SettingsAboutSection(
                versionName = versionName,
                onOpenTerms = onOpenTerms,
                onOpenPrivacy = onOpenPrivacy,
            )
        }

        item {
            Spacer(
                modifier = Modifier.height(16.dp),
            )
        }
    }
}