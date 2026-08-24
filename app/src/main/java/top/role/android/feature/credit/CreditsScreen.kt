package top.role.android.feature.credit

import java.util.Locale

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import top.role.android.core.model.account.MobileCreditPurchaseOption
import top.role.android.core.model.account.MobileCustomCreditPurchaseOption
import top.role.android.core.model.account.MobileRecentCreditPurchase


@Composable
fun CreditsScreen(
    viewModel: CreditsViewModel,
) {
    val uiState by
    viewModel.uiState
        .collectAsStateWithLifecycle()

    CreditsScreenContent(
        state = uiState,

        onCustomAmountChange =
            viewModel::updateCustomAmount,

        onPurchasePack =
            viewModel::purchaseFixedPack,

        onPurchaseCustom =
            viewModel::purchaseCustomPack,

        onContinuePurchase =
            viewModel::continuePurchase,
    )
}


@Composable
private fun CreditsScreenContent(
    state: CreditsUiState,

    onCustomAmountChange:
        (String) -> Unit,

    onPurchasePack:
        (MobileCreditPurchaseOption) -> Unit,

    onPurchaseCustom:
        () -> Unit,

    onContinuePurchase:
        (MobileRecentCreditPurchase) -> Unit,
) {
    when {
        state.loading -> {
            CreditsLoadingState()
        }

        state.errorMessage != null &&
                state.packs.isEmpty() -> {

            CreditsErrorState(
                message =
                    state.errorMessage,
            )
        }

        else -> {
            CreditsContent(
                state =
                    state,

                onCustomAmountChange =
                    onCustomAmountChange,

                onPurchasePack =
                    onPurchasePack,

                onPurchaseCustom =
                    onPurchaseCustom,

                onContinuePurchase =
                    onContinuePurchase,
            )
        }
    }
}


@Composable
private fun CreditsLoadingState() {
    Column(
        modifier =
            Modifier.fillMaxSize(),

        verticalArrangement =
            Arrangement.Center,

        horizontalAlignment =
            Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()

        Spacer(
            modifier =
                Modifier.height(16.dp)
        )

        Text(
            text =
                "正在加载 Credits…",

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


@Composable
private fun CreditsErrorState(
    message: String,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),

        verticalArrangement =
            Arrangement.Center,

        horizontalAlignment =
            Alignment.CenterHorizontally,
    ) {
        Text(
            text =
                "无法加载 Credits",

            style =
                MaterialTheme
                    .typography
                    .titleMedium,

            fontWeight =
                FontWeight.SemiBold,
        )

        Spacer(
            modifier =
                Modifier.height(8.dp)
        )

        Text(
            text =
                message,

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


@Composable
private fun CreditsContent(
    state: CreditsUiState,

    onCustomAmountChange:
        (String) -> Unit,

    onPurchasePack:
        (MobileCreditPurchaseOption) -> Unit,

    onPurchaseCustom:
        () -> Unit,

    onContinuePurchase:
        (MobileRecentCreditPurchase) -> Unit,
) {
    val purchaseInProgress =
        state.purchasingPackKey != null

    LazyColumn(
        modifier =
            Modifier.fillMaxSize(),

        contentPadding =
            PaddingValues(
                horizontal = 20.dp,
                vertical = 20.dp,
            ),

        verticalArrangement =
            Arrangement.spacedBy(
                24.dp
            ),
    ) {

        /*
         * 固定 Credits 档位
         */
        item {
            Column(
                verticalArrangement =
                    Arrangement.spacedBy(
                        12.dp
                    ),
            ) {
                Text(
                    text =
                        "充值档位",

                    style =
                        MaterialTheme
                            .typography
                            .labelLarge,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,
                )

                state.packs
                    .forEach { pack ->

                        CreditPackItem(
                            pack =
                                pack,

                            isPurchasing =
                                state.purchasingPackKey ==
                                        pack.packKey,

                            purchaseEnabled =
                                !purchaseInProgress,

                            onPurchase = {
                                onPurchasePack(
                                    pack
                                )
                            },
                        )
                    }
            }
        }


        /*
         * 自定义 Credits
         */
        state.custom
            ?.takeIf {
                it.enabled
            }
            ?.let { custom ->

                item {
                    CustomCreditSection(
                        config =
                            custom,

                        amountText =
                            state.customAmountText,

                        isPurchasing =
                            state.purchasingPackKey ==
                                    "custom",

                        purchaseEnabled =
                            !purchaseInProgress,

                        onAmountChange =
                            onCustomAmountChange,

                        onPurchase =
                            onPurchaseCustom,
                    )
                }
            }


        /*
         * 创建订单时的错误。
         *
         * 不影响整个 Credits 页面。
         */
        state.purchaseErrorMessage
            ?.takeIf {
                it.isNotBlank()
            }
            ?.let { message ->

                item {
                    Text(
                        text =
                            message,

                        style =
                            MaterialTheme
                                .typography
                                .bodyMedium,

                        color =
                            MaterialTheme
                                .colorScheme
                                .error,
                    )
                }
            }


        /*
         * 最近 Credits 购买
         */
        item {
            Column {
                Text(
                    text =
                        "最近购买",

                    style =
                        MaterialTheme
                            .typography
                            .labelLarge,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,
                )

                Spacer(
                    modifier =
                        Modifier.height(8.dp)
                )

                RecentCreditPurchasesSection(
                    loading =
                        state.recentLoading,

                    purchases =
                        state.recentPurchases,

                    purchasingKey =
                        state.purchasingPackKey,

                    onContinuePurchase =
                        onContinuePurchase,
                )

            }
        }
    }
}


@Composable
private fun CreditPackItem(
    pack:
    MobileCreditPurchaseOption,

    isPurchasing:
    Boolean,

    purchaseEnabled:
    Boolean,

    onPurchase:
        () -> Unit,
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
        Column(
            modifier =
                Modifier.padding(
                    16.dp
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

                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically,
                    ) {

                        Text(
                            text =
                                pack.title,

                            style =
                                MaterialTheme
                                    .typography
                                    .titleMedium,

                            fontWeight =
                                FontWeight.SemiBold,
                        )

                        if (
                            pack.recommended
                        ) {
                            Spacer(
                                modifier =
                                    Modifier.width(
                                        8.dp
                                    )
                            )

                            Surface(
                                shape =
                                    MaterialTheme
                                        .shapes
                                        .small,

                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .secondaryContainer,
                            ) {
                                Text(
                                    text =
                                        "推荐",

                                    modifier =
                                        Modifier.padding(
                                            horizontal =
                                                8.dp,

                                            vertical =
                                                3.dp,
                                        ),

                                    style =
                                        MaterialTheme
                                            .typography
                                            .labelSmall,

                                    color =
                                        MaterialTheme
                                            .colorScheme
                                            .onSecondaryContainer,
                                )
                            }
                        }
                    }

                    Spacer(
                        modifier =
                            Modifier.height(
                                4.dp
                            )
                    )

                    Text(
                        text =
                            "${formatCredits(pack.credits)} Credits",

                        style =
                            MaterialTheme
                                .typography
                                .bodyLarge,

                        fontWeight =
                            FontWeight.Medium,
                    )
                }


                Text(
                    text =
                        pack.amountText,

                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,

                    fontWeight =
                        FontWeight.SemiBold,
                )
            }


            if (
                !pack.desc
                    .isNullOrBlank()
            ) {
                Spacer(
                    modifier =
                        Modifier.height(
                            8.dp
                        )
                )

                Text(
                    text =
                        pack.desc,

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


            Spacer(
                modifier =
                    Modifier.height(
                        14.dp
                    )
            )


            Button(
                modifier =
                    Modifier.fillMaxWidth(),

                enabled =
                    purchaseEnabled,

                onClick =
                    onPurchase,
            ) {

                if (
                    isPurchasing
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
                            Modifier.width(8.dp)
                    )

                    Text(
                        text =
                            "正在创建订单…"
                    )

                } else {

                    Text(
                        text =
                            "购买 ${pack.amountText}",
                    )
                }
            }
        }
    }
}


@Composable
private fun CustomCreditSection(
    config:
    MobileCustomCreditPurchaseOption,

    amountText:
    String,

    isPurchasing:
    Boolean,

    purchaseEnabled:
    Boolean,

    onAmountChange:
        (String) -> Unit,

    onPurchase:
        () -> Unit,
) {
    val amountMinor =
        parseAmountMinor(
            amountText
        )

    /*
     * 与后端 getMobileCreditPurchaseOptions
     * 返回的 min/max 完全对齐。
     */
    val validAmountMinor =
        amountMinor
            ?.takeIf {
                it >=
                        config.minAmountMinor &&
                        it <=
                        config.maxAmountMinor
            }

    val estimatedCredits =
        validAmountMinor
            ?.let {
                estimateCustomCredits(
                    amountMinor =
                        it
                )
            }

    Column(
        verticalArrangement =
            Arrangement.spacedBy(
                12.dp
            ),
    ) {

        /*
         * 与固定档位的 section 标题层级一致。
         */
        Text(
            text =
                config.title,

            style =
                MaterialTheme
                    .typography
                    .labelLarge,

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant,
        )


        /*
         * 与 fixed pack 使用相同 Surface。
         */
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
            Column(
                modifier =
                    Modifier.padding(
                        16.dp
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
                                "自定义金额",

                            style =
                                MaterialTheme
                                    .typography
                                    .titleMedium,

                            fontWeight =
                                FontWeight.SemiBold,
                        )

                        Spacer(
                            modifier =
                                Modifier.height(4.dp)
                        )

                        Text(
                            text =
                                config.baseRateText,

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


                Spacer(
                    modifier =
                        Modifier.height(
                            14.dp
                        )
                )


                OutlinedTextField(
                    modifier =
                        Modifier.fillMaxWidth(),

                    value =
                        amountText,

                    onValueChange = { value ->

                        onAmountChange(
                            sanitizeAmountInput(
                                value
                            )
                        )
                    },

                    singleLine =
                        true,

                    enabled =
                        !isPurchasing,

                    label = {
                        Text(
                            text =
                                "充值金额（元）",
                        )
                    },

                    prefix = {
                        Text(
                            text =
                                "¥",
                        )
                    },

                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType =
                                KeyboardType.Decimal,
                        ),
                )


                Spacer(
                    modifier =
                        Modifier.height(
                            10.dp
                        )
                )


                Text(
                    text =
                        "支持 ¥${formatYuan(config.minAmountMinor)} ～ " +
                                "¥${formatYuan(config.maxAmountMinor)}",

                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,
                )


                if (
                    !config.desc
                        .isNullOrBlank()
                ) {
                    Spacer(
                        modifier =
                            Modifier.height(
                                4.dp
                            )
                    )

                    Text(
                        text =
                            config.desc,

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


                if (
                    estimatedCredits != null
                ) {
                    Spacer(
                        modifier =
                            Modifier.height(
                                14.dp
                            )
                    )

                    HorizontalDivider()

                    Spacer(
                        modifier =
                            Modifier.height(
                                14.dp
                            )
                    )

                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),

                        verticalAlignment =
                            Alignment.CenterVertically,
                    ) {

                        Text(
                            text =
                                "预计获得",

                            style =
                                MaterialTheme
                                    .typography
                                    .bodyMedium,
                        )

                        Spacer(
                            modifier =
                                Modifier.weight(1f)
                        )

                        Text(
                            text =
                                "${formatCredits(estimatedCredits)} Credits",

                            style =
                                MaterialTheme
                                    .typography
                                    .bodyLarge,

                            fontWeight =
                                FontWeight.SemiBold,
                        )
                    }
                }


                Spacer(
                    modifier =
                        Modifier.height(
                            14.dp
                        )
                )


                Button(
                    modifier =
                        Modifier.fillMaxWidth(),

                    enabled =
                        validAmountMinor != null &&
                                purchaseEnabled,

                    onClick =
                        onPurchase,
                ) {

                    if (
                        isPurchasing
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
                                Modifier.width(8.dp)
                        )

                        Text(
                            text =
                                "正在创建订单…"
                        )

                    } else {

                        Text(
                            text =
                                if (
                                    amountText
                                        .isNotBlank()
                                ) {
                                    "充值 ¥$amountText"
                                } else {
                                    "自定义充值"
                                },
                        )
                    }
                }
            }
        }
    }
}


private fun sanitizeAmountInput(
    value:
    String,
): String {

    val filtered =
        value.filter {
            it.isDigit() ||
                    it == '.'
        }

    val firstDot =
        filtered.indexOf('.')

    if (
        firstDot < 0
    ) {
        return filtered
    }

    val integerPart =
        filtered.substring(
            0,
            firstDot,
        )

    val decimalPart =
        filtered
            .substring(
                firstDot + 1
            )
            .filter {
                it.isDigit()
            }
            .take(2)

    return buildString {
        append(
            integerPart
        )

        append('.')

        append(
            decimalPart
        )
    }
}


private fun parseAmountMinor(
    amountText:
    String,
): Double? {

    val yuan =
        amountText
            .trim()
            .toDoubleOrNull()
            ?: return null

    if (
        !yuan.isFinite() ||
        yuan <= 0.0
    ) {
        return null
    }

    return kotlin.math.round(
        yuan * 100.0
    )
}


private fun estimateCustomCredits(
    amountMinor:
    Double,
): Double {

    return kotlin.math.floor(
        amountMinor *
                618.0 /
                10_000.0
    )
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


private fun formatYuan(
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
        yuan
            .toLong()
            .toString()
    } else {
        String.format(
            Locale.US,
            "%.2f",
            yuan,
        )
    }
}