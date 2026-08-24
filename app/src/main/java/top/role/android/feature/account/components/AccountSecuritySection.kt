package top.role.android.feature.account.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import top.role.android.core.model.account.MobileAccountProfile


@Composable
fun AccountSecuritySection(
    profile: MobileAccountProfile,
    onOpenPassword: () -> Unit,
) {
    AccountSection(
        title =
            "安全",
    ) {

        AccountInfoRow(
            icon =
                Icons.Outlined.Shield,

            label =
                "密码",

            value =
                if (
                    profile.hasPassword
                ) {
                    "已设置"
                } else {
                    "未设置"
                },
        )


        SectionDivider()


        AccountNavigationRow(
            icon =
                Icons.Outlined.Lock,

            title =
                if (
                    profile.hasPassword
                ) {
                    "修改密码"
                } else {
                    "设置密码"
                },

            onClick =
                onOpenPassword,
        )


        if (
            profile.passwordTip
                .isNotBlank()
        ) {

            Text(
                text =
                    profile.passwordTip,

                modifier =
                    Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 14.dp,
                    ),

                style =
                    MaterialTheme.typography.bodySmall,

                color =
                    MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}