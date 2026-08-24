package top.role.android.feature.credit

import java.util.Locale

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import top.role.android.core.model.account.MobileRecentCreditPurchase


@Composable
fun RecentCreditPurchasesSection(
    loading:
    Boolean,

    purchases:
    List<MobileRecentCreditPurchase>,

    purchasingKey:
    String?,

    onContinuePurchase:
        (MobileRecentCreditPurchase) -> Unit,
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            MaterialTheme
                .shapes
                .large,

        tonalElevation =
            1.dp,
    ) {

        when {

            loading -> {

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                16.dp
                            ),

                    verticalAlignment =
                        Alignment.CenterVertically,
                ) {

                    CircularProgressIndicator(
                        modifier =
                            Modifier
                                .width(20.dp)
                                .height(20.dp),

                        strokeWidth =
                            2.dp,
                    )

                    Spacer(
                        modifier =
                            Modifier.width(
                                12.dp
                            )
                    )

                    Text(
                        text =
                            "正在加载最近购买…",

                        style =
                            MaterialTheme
                                .typography
                                .bodyMedium,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant,
                    )
                }
            }


            purchases.isEmpty() -> {

                Text(
                    text =
                        "暂无购买记录",

                    modifier =
                        Modifier.padding(
                            16.dp
                        ),

                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,
                )
            }


            else -> {

                Column {

                    purchases
                        .forEachIndexed {
                                index,
                                purchase ->

                            RecentCreditPurchaseRow(
                                purchase =
                                    purchase,

                                isContinuing =
                                    purchasingKey ==
                                            continuePurchaseKey(
                                                purchase
                                            ),

                                purchaseInProgress =
                                    purchasingKey != null,

                                onContinuePurchase =
                                    onContinuePurchase,
                            )


                            if (
                                index <
                                purchases.lastIndex
                            ) {
                                HorizontalDivider(
                                    modifier =
                                        Modifier.padding(
                                            start =
                                                16.dp,
                                        )
                                )
                            }
                        }
                }
            }
        }
    }
}


@Composable
private fun RecentCreditPurchaseRow(
    purchase:
    MobileRecentCreditPurchase,

    isContinuing:
    Boolean,

    purchaseInProgress:
    Boolean,

    onContinuePurchase:
        (MobileRecentCreditPurchase) -> Unit,
) {
    val canContinue =
        canContinuePurchase(
            purchase
        )


    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal =
                        16.dp,

                    vertical =
                        14.dp,
                ),
    ) {

        Row(
            modifier =
                Modifier.fillMaxWidth(),

            verticalAlignment =
                Alignment.CenterVertically,
        ) {

            Column(
                modifier =
                    Modifier.weight(1f),
            ) {

                Text(
                    text =
                        purchase.packTitle
                            ?.takeIf {
                                it.isNotBlank()
                            }
                            ?: if (
                                purchase.isCustom
                            ) {
                                "自定义充值"
                            } else {
                                "购买 Credits"
                            },

                    style =
                        MaterialTheme
                            .typography
                            .bodyLarge,

                    fontWeight =
                        FontWeight.Medium,
                )


                Spacer(
                    modifier =
                        Modifier.height(
                            3.dp
                        )
                )


                Text(
                    text =
                        creditPurchaseStatusLabel(
                            purchase
                        ),

                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,
                )
            }


            Column(
                horizontalAlignment =
                    Alignment.End,
            ) {

                Text(
                    text =
                        purchase.amountText
                            ?.takeIf {
                                it.isNotBlank()
                            }
                            ?: formatAmountMinor(
                                purchase.amountMinor
                            ),

                    style =
                        MaterialTheme
                            .typography
                            .bodyLarge,

                    fontWeight =
                        FontWeight.SemiBold,
                )


                Spacer(
                    modifier =
                        Modifier.height(
                            3.dp
                        )
                )


                Text(
                    text =
                        "${formatCredits(purchase.credits)} C",

                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,
                )
            }
        }


        if (
            canContinue
        ) {

            Spacer(
                modifier =
                    Modifier.height(
                        12.dp
                    )
            )


            Button(
                modifier =
                    Modifier.fillMaxWidth(),

                enabled =
                    !purchaseInProgress,

                onClick = {
                    onContinuePurchase(
                        purchase
                    )
                },
            ) {

                if (
                    isContinuing
                ) {

                    CircularProgressIndicator(
                        modifier =
                            Modifier
                                .width(18.dp)
                                .height(18.dp),

                        strokeWidth =
                            2.dp,
                    )

                    Spacer(
                        modifier =
                            Modifier.width(
                                8.dp
                            )
                    )

                    Text(
                        text =
                            "正在继续支付…"
                    )

                } else {

                    Text(
                        text =
                            "继续支付"
                    )
                }
            }
        }
    }
}


private fun canContinuePurchase(
    purchase:
    MobileRecentCreditPurchase,
): Boolean {

    if (
        purchase.status !=
        "pending"
    ) {
        return false
    }


    val dueAt =
        purchase.dueAt
            ?: return true


    return System.currentTimeMillis() <
            dueAt.toLong()
}


private fun creditPurchaseStatusLabel(
    purchase:
    MobileRecentCreditPurchase,
): String {

    if (
        purchase.status ==
        "pending"
    ) {

        val dueAt =
            purchase.dueAt

        if (
            dueAt != null &&
            System.currentTimeMillis() >=
            dueAt.toLong()
        ) {
            return "已过期"
        }
    }


    return when (
        purchase.status
    ) {

        "pending" ->
            "待支付"

        "paid" ->
            "已支付"

        "settled" ->
            "已入账"

        "failed" ->
            "支付失败"

        "canceled" ->
            "已取消"

        else ->
            purchase.status
                .ifBlank {
                    "处理中"
                }
    }
}


private fun continuePurchaseKey(
    purchase:
    MobileRecentCreditPurchase,
): String {

    return "continue:${purchase.outTradeNo}"
}


private fun formatCredits(
    value:
    Double,
): String {

    return if (
        value % 1.0 ==
        0.0
    ) {
        value
            .toLong()
            .toString()
    } else {
        value.toString()
    }
}


private fun formatAmountMinor(
    amountMinor:
    Double,
): String {

    val yuan =
        amountMinor /
                100.0


    return if (
        yuan % 1.0 ==
        0.0
    ) {
        "¥${yuan.toLong()}"
    } else {
        "¥${
            String.format(
                Locale.US,
                "%.2f",
                yuan,
            )
        }"
    }
}