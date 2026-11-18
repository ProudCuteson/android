package top.role.android.feature.settings.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.runtime.Composable
import top.role.android.feature.components.PageSection
import top.role.android.feature.components.SectionNavigationRow
import top.role.android.feature.components.SectionDivider
import top.role.android.feature.components.SectionInfoRow

@Composable
fun SettingsAboutSection(
    versionName: String,
    onOpenTerms: () -> Unit,
    onOpenPrivacy: () -> Unit,
) {
    PageSection(
        title = "关于",
    ) {
        SectionNavigationRow(
            icon = Icons.Outlined.Description,
            title = "用户协议",
            onClick = onOpenTerms,
        )

        SectionDivider()

        SectionNavigationRow(
            icon = Icons.Outlined.PrivacyTip,
            title = "隐私政策",
            onClick = onOpenPrivacy,
        )

        SectionDivider()

        SectionInfoRow(
            icon = Icons.Outlined.Info,
            label = "当前版本",
            value = versionName,
        )
    }
}