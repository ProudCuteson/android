package top.role.android.feature.account.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.runtime.Composable


@Composable
fun AccountAboutSection(
    versionName: String,
    onOpenTerms: () -> Unit,
    onOpenPrivacy: () -> Unit,
) {
    AccountSection(
        title =
            "关于",
    ) {

        AccountNavigationRow(
            icon =
                Icons.Outlined.Description,

            title =
                "用户协议",

            onClick =
                onOpenTerms,
        )


        SectionDivider()


        AccountNavigationRow(
            icon =
                Icons.Outlined.PrivacyTip,

            title =
                "隐私政策",

            onClick =
                onOpenPrivacy,
        )


        SectionDivider()


        AccountInfoRow(
            icon =
                Icons.Outlined.Info,

            label =
                "当前版本",

            value =
                versionName,
        )
    }
}