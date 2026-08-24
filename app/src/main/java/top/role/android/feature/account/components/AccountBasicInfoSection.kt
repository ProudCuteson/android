package top.role.android.feature.account.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.runtime.Composable

import top.role.android.core.model.account.MobileAccountProfile


@Composable
fun AccountBasicInfoSection(
    profile: MobileAccountProfile,
) {
    AccountSection(
        title =
            "基础信息",
    ) {

        AccountInfoRow(
            icon =
                Icons.Outlined.Phone,

            label =
                "手机号",

            value =
                displayAccountValue(
                    profile.phone
                ),
        )


        SectionDivider()


        AccountInfoRow(
            icon =
                Icons.Outlined.Email,

            label =
                "邮箱",

            value =
                displayAccountValue(
                    profile.email
                ),
        )
    }
}


private fun displayAccountValue(
    value: String,
): String {

    return value
        .trim()
        .ifEmpty {
            "—"
        }
}