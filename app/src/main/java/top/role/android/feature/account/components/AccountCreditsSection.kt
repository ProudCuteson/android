package top.role.android.feature.account.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.runtime.Composable


@Composable
fun AccountCreditsSection(
    balance: Double,
    onOpenCredits: () -> Unit,
) {
    AccountSection(
        title =
            "Credits",
    ) {

        AccountInfoRow(
            icon =
                Icons.Outlined.AccountBalanceWallet,

            label =
                "余额",

            value =
                formatCredits(
                    balance
                ),
        )


        SectionDivider()


        AccountNavigationRow(
            icon =
                Icons.Outlined.AccountBalanceWallet,

            title =
                "购买 Credits",

            onClick =
                onOpenCredits,
        )
    }
}


private fun formatCredits(
    value: Double,
): String {

    return if (
        value % 1.0 == 0.0
    ) {
        "${value.toLong()} C"
    } else {
        "$value C"
    }
}