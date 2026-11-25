package top.role.android.feature.credit

import java.util.Locale
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import top.role.android.core.auth.AuthState
import top.role.android.core.model.account.MobileCreditPurchaseOption
import top.role.android.core.model.account.MobileCustomCreditPurchaseOption
import top.role.android.core.model.account.MobileRecentCreditPurchase
import top.role.android.core.payment.wechatPayDisplayMessage
import top.role.android.ui.auth.AuthStatusContent

// ============================================================
// Screen
// ============================================================

@Composable
fun CreditsScreen(
    viewModel: CreditsViewModel,
    authState: AuthState,
    onLogin: () -> Unit,
    onRetryAuth: () -> Unit,
    onReSignIn: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AuthStatusContent(
        authState = authState,
        onLogin = onLogin,
        onRetry = onRetryAuth,
        onReSignIn = onReSignIn,
        modifier = Modifier.fillMaxSize(),
    ) {
        CreditsScreenContent(
            state = uiState,
            onCustomAmountChange = viewModel::updateCustomAmount,
            onPurchasePack = viewModel::purchaseFixedPack,
            onPurchaseCustom = viewModel::purchaseCustomPack,
            onContinuePurchase = viewModel::continuePurchase,
        )
    }
}

@Composable
private fun CreditsScreenContent(
    state: CreditsUiState,
    onCustomAmountChange: (String) -> Unit,
    onPurchasePack: (MobileCreditPurchaseOption) -> Unit,
    onPurchaseCustom: () -> Unit,
    onContinuePurchase: (MobileRecentCreditPurchase) -> Unit,
) {
    when {
        state.loading -> CreditsLoadingState()

        state.errorMessage != null && state.packs.isEmpty() -> {
            CreditsErrorState(message = state.errorMessage)
        }

        else -> CreditsContent(
            state = state,
            onCustomAmountChange = onCustomAmountChange,
            onPurchasePack = onPurchasePack,
            onPurchaseCustom = onPurchaseCustom,
            onContinuePurchase = onContinuePurchase,
        )
    }
}

// ============================================================
// Loading / Error
// ============================================================

@Composable
private fun CreditsLoadingState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "正在加载 Credits…",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CreditsErrorState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "无法加载 Credits",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ============================================================
// Main Content
// ============================================================

@Composable
private fun CreditsContent(
    state: CreditsUiState,
    onCustomAmountChange: (String) -> Unit,
    onPurchasePack: (MobileCreditPurchaseOption) -> Unit,
    onPurchaseCustom: () -> Unit,
    onContinuePurchase: (MobileRecentCreditPurchase) -> Unit,
) {
    val purchaseInProgress = state.purchasingPackKey != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
    ) {

        // 固定支付提示，不随购买列表滚动。
        state.purchaseErrorMessage
            ?.takeIf { it.isNotBlank() }
            ?.let { message ->
                Box(
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 12.dp,
                    ),
                ) {
                    CreditPurchaseNotice(message = message)
                }
            }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(
                horizontal = 16.dp,
                vertical = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {

            // 固定充值档位
            item(key = "credit_packs") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    CreditSectionTitle("充值档位")

                    state.packs.forEach { pack ->
                        CreditPackItem(
                            pack = pack,
                            isPurchasing = state.purchasingPackKey == pack.packKey,
                            purchaseEnabled = !purchaseInProgress,
                            onPurchase = { onPurchasePack(pack) },
                        )
                    }
                }
            }

            // 自定义充值
            state.custom
                ?.takeIf { it.enabled }
                ?.let { custom ->
                    item(key = "credit_custom") {
                        CustomCreditSection(
                            config = custom,
                            amountText = state.customAmountText,
                            isPurchasing = state.purchasingPackKey == "custom",
                            purchaseEnabled = !purchaseInProgress,
                            onAmountChange = onCustomAmountChange,
                            onPurchase = onPurchaseCustom,
                        )
                    }
                }

            // 最近购买
            item(key = "credit_recent") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    CreditSectionTitle("最近购买")

                    RecentCreditPurchasesSection(
                        loading = state.recentLoading,
                        purchases = state.recentPurchases,
                        purchasingKey = state.purchasingPackKey,
                        onContinuePurchase = onContinuePurchase,
                    )
                }
            }
        }
    }
}

// ============================================================
// Shared UI
// ============================================================

@Composable
private fun CreditSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun CreditPurchaseNotice(message: String) {
    val isConfirming =
        message == "CREDIT_PAYMENT_CONFIRMING" ||
                message == "支付结果正在确认，请稍后查看购买记录。"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = if (isConfirming) {
            MaterialTheme.colorScheme.surfaceVariant
        } else {
            MaterialTheme.colorScheme.errorContainer
        },
    ) {
        Text(
            text = wechatPayDisplayMessage(message),
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isConfirming) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onErrorContainer
            },
        )
    }
}

@Composable
private fun CreditPurchaseButton(
    text: String,
    isPurchasing: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Button(
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        onClick = onClick,
    ) {
        if (isPurchasing) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text("正在处理…")
        } else {
            Text(text)
        }
    }
}

@Composable
private fun RecommendedLabel() {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Text(
            text = "推荐",
            modifier = Modifier.padding(
                horizontal = 8.dp,
                vertical = 3.dp,
            ),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

// ============================================================
// Fixed Credit Pack
// ============================================================

@Composable
private fun CreditPackItem(
    pack: MobileCreditPurchaseOption,
    isPurchasing: Boolean,
    purchaseEnabled: Boolean,
    onPurchase: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = pack.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )

                        if (pack.recommended) {
                            RecommendedLabel()
                        }
                    }

                    Text(
                        text = "${formatCredits(pack.credits)} Credits",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                }

                Text(
                    text = pack.amountText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            pack.desc
                ?.takeIf { it.isNotBlank() }
                ?.let { description ->
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

            CreditPurchaseButton(
                text = "购买 ${pack.amountText}",
                isPurchasing = isPurchasing,
                enabled = purchaseEnabled,
                onClick = onPurchase,
            )
        }
    }
}

// ============================================================
// Custom Credit Purchase
// ============================================================

@Composable
private fun CustomCreditSection(
    config: MobileCustomCreditPurchaseOption,
    amountText: String,
    isPurchasing: Boolean,
    purchaseEnabled: Boolean,
    onAmountChange: (String) -> Unit,
    onPurchase: () -> Unit,
) {
    val amountMinor = parseAmountMinor(amountText)

    val validAmountMinor = amountMinor?.takeIf {
        it >= config.minAmountMinor &&
                it <= config.maxAmountMinor
    }

    val estimatedCredits = validAmountMinor?.let {
        estimateCustomCredits(it)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        CreditSectionTitle(config.title)

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = 1.dp,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "自定义金额",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Text(
                        text = config.baseRateText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = amountText,
                    onValueChange = {
                        onAmountChange(sanitizeAmountInput(it))
                    },
                    singleLine = true,
                    enabled = !isPurchasing,
                    label = { Text("充值金额（元）") },
                    prefix = { Text("¥") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                    ),
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "支持 ¥${formatYuan(config.minAmountMinor)} ～ " +
                                "¥${formatYuan(config.maxAmountMinor)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    config.desc
                        ?.takeIf { it.isNotBlank() }
                        ?.let { description ->
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                }

                if (estimatedCredits != null) {
                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "预计获得",
                            style = MaterialTheme.typography.bodyMedium,
                        )

                        Text(
                            text = "${formatCredits(estimatedCredits)} Credits",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                CreditPurchaseButton(
                    text = if (amountText.isNotBlank()) {
                        "充值 ¥$amountText"
                    } else {
                        "自定义充值"
                    },
                    isPurchasing = isPurchasing,
                    enabled = validAmountMinor != null && purchaseEnabled,
                    onClick = onPurchase,
                )
            }
        }
    }
}

// ============================================================
// Amount Helpers
// ============================================================

private fun sanitizeAmountInput(value: String): String {
    val filtered = value.filter { it.isDigit() || it == '.' }
    val firstDot = filtered.indexOf('.')

    if (firstDot < 0) return filtered

    val integerPart = filtered.substring(0, firstDot)

    val decimalPart = filtered
        .substring(firstDot + 1)
        .filter { it.isDigit() }
        .take(2)

    return "$integerPart.$decimalPart"
}

private fun parseAmountMinor(amountText: String): Double? {
    val yuan = amountText.trim().toDoubleOrNull() ?: return null

    if (!yuan.isFinite() || yuan <= 0.0) return null

    return kotlin.math.round(yuan * 100.0)
}

private fun estimateCustomCredits(amountMinor: Double): Double {
    return kotlin.math.floor(amountMinor * 618.0 / 10_000.0)
}

private fun formatCredits(value: Double): String {
    return if (value % 1.0 == 0.0) {
        value.toLong().toString()
    } else {
        value.toString()
    }
}

private fun formatYuan(amountMinor: Double): String {
    val yuan = amountMinor / 100.0

    return if (yuan % 1.0 == 0.0) {
        yuan.toLong().toString()
    } else {
        String.format(Locale.US, "%.2f", yuan)
    }
}