package top.role.android.feature.membership

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import top.role.android.core.model.membership.MobileMembershipPlan
import top.role.android.core.payment.wechatPayDisplayMessage

import java.time.Instant
import java.time.ZoneId

@Composable
fun MembershipScreen(
    uiState: MembershipUiState,
    onRetry: () -> Unit,
    onSelectPlan: (String) -> Unit,
    onPurchase: () -> Unit,
) {
    when {
        uiState.loading -> MembershipLoadingState()

        uiState.loadError != null -> {
            MembershipLoadErrorState(onRetry = onRetry)
        }

        else -> MembershipContent(
            uiState = uiState,
            onSelectPlan = onSelectPlan,
            onPurchase = onPurchase,
        )
    }
}

// Loading / Error

@Composable
private fun MembershipLoadingState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "正在加载会员信息…",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MembershipLoadErrorState(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "暂时无法加载会员信息",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(onClick = onRetry) {
            Text("重新加载")
        }
    }
}

// Main Content

@Composable
private fun MembershipContent(
    uiState: MembershipUiState,
    onSelectPlan: (String) -> Unit,
    onPurchase: () -> Unit,
) {
    val page = uiState.page ?: return
    val space = page.space ?: return
    val membership = page.membership ?: return
    val viewer = page.viewer ?: return

    val purchaseInProgress = uiState.paying || uiState.confirming

    val purchaseEnabled =
        !purchaseInProgress &&
                !viewer.isManager &&
                !viewer.isPermanentMember &&
                !uiState.selectedPlanKey.isNullOrBlank()

    Column(modifier = Modifier.fillMaxSize()) {

        // 可滚动的会员介绍及购买方案。
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {

            // 空间信息
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "空间会员",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )

                Text(
                    text = space.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )

                space.description
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { description ->
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                space.number?.let { number ->
                    Text(
                        text = "空间号 ${formatSpaceNumber(number)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // 当前会员身份
            if (viewer.isManager || viewer.isPermanentMember || viewer.isMember) {
                MembershipViewerNotice(
                    isManager = viewer.isManager,
                    isPermanentMember = viewer.isPermanentMember,
                    isMember = viewer.isMember,
                    activeUntil = viewer.activeUntil,
                )
            }

            HorizontalDivider()

            // 会员方案
            if (!membership.enabled) {
                MembershipDisabledNotice()
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "选择会员方案",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )

                    membership.description
                        ?.trim()
                        ?.takeIf { it.isNotEmpty() }
                        ?.let { description ->
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                    membership.plans.forEach { plan ->
                        MembershipPlanCard(
                            plan = plan,
                            selected = uiState.selectedPlanKey == plan.key,
                            enabled = !purchaseInProgress,
                            onClick = { onSelectPlan(plan.key) },
                        )
                    }
                }
            }
        }

        // 固定底部购买区域。
        if (membership.enabled) {
            MembershipPurchaseBar(
                uiState = uiState,
                purchaseEnabled = purchaseEnabled,
                buttonText = when {
                    uiState.confirming -> "正在确认…"
                    uiState.paying -> "正在支付…"
                    viewer.isManager -> "管理员无需开通"
                    viewer.isPermanentMember -> "已是永久会员"
                    viewer.isMember -> "继续续费"
                    else -> "立即开通"
                },
                onPurchase = onPurchase,
            )
        }
    }
}

// Membership Status

@Composable
private fun MembershipDisabledNotice() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "暂未开放会员",
                style = MaterialTheme.typography.titleMedium,
            )

            Text(
                text = "该空间目前没有开放付费会员。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MembershipViewerNotice(
    isManager: Boolean,
    isPermanentMember: Boolean,
    isMember: Boolean,
    activeUntil: Double?,
) {
    val title: String
    val description: String

    when {
        isManager -> {
            title = "空间管理员"
            description = "您是该空间的管理员，无需购买会员。"
        }

        isPermanentMember -> {
            title = "永久会员"
            description = "您已经拥有该空间的永久会员权限。"
        }

        isMember -> {
            title = "当前会员"
            description = activeUntil?.let {
                "当前会员有效期至 ${formatMembershipDate(it)}，续费将在现有有效期基础上顺延。"
            } ?: "当前会员有效，续费将在现有会员期限基础上顺延。"
        }

        else -> return
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// Membership Plan

@Composable
private fun MembershipPlanCard(
    plan: MobileMembershipPlan,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = enabled,
                onClick = onClick,
            ),
        shape = MaterialTheme.shapes.large,
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            },
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(
                selected = selected,
                onClick = null,
                enabled = enabled,
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = plan.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )

                    if (plan.key == "year") {
                        MembershipRecommendedLabel()
                    }
                }

                Text(
                    text = "有效期 ${formatMonthCount(plan.months)} 个月",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = formatMembershipPrice(
                        amountMinor = plan.totalPriceCents,
                        currency = plan.currency,
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )

                Text(
                    text = "一次支付",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun MembershipRecommendedLabel() {
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

// Purchase Bar

@Composable
private fun MembershipPurchaseBar(
    uiState: MembershipUiState,
    purchaseEnabled: Boolean,
    buttonText: String,
    onPurchase: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            uiState.paymentError
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?.let { error ->
                    MembershipPaymentNotice(error)
                }

            if (uiState.confirming) {
                Text(
                    text = "正在确认会员状态，请稍候。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (uiState.paymentSuccess) {
                Text(
                    text = "会员已开通",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = purchaseEnabled,
                onClick = onPurchase,
            ) {
                if (uiState.paying || uiState.confirming) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(buttonText)
            }
        }
    }
}

@Composable
private fun MembershipPaymentNotice(error: String) {
    val isConfirming = error == "MEMBERSHIP_PAYMENT_CONFIRMING"

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
            text = displayMembershipError(error),
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

// Format Helpers

private fun formatMembershipPrice(
    amountMinor: Double,
    currency: String,
): String {
    val amount = amountMinor / 100.0

    return when (currency) {
        "USD" -> "$%.2f".format(amount)
        else -> "¥%.2f".format(amount)
    }
}

private fun formatMonthCount(months: Double): String {
    return if (months % 1.0 == 0.0) {
        months.toInt().toString()
    } else {
        months.toString()
    }
}

private fun formatSpaceNumber(number: Double): String {
    return if (number % 1.0 == 0.0) {
        number.toLong().toString()
    } else {
        number.toString()
    }
}

private fun formatMembershipDate(timestamp: Double): String {
    return Instant.ofEpochMilli(timestamp.toLong())
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .toString()
}

// Error Messages

private fun displayMembershipError(code: String): String {
    return when (code.trim()) {
        "SPACE_NOT_FOUND" ->
            "空间不存在或已删除。"

        "SPACE_NOT_ACTIVE" ->
            "该空间当前不可用。"

        "MEMBERSHIP_NOT_ENABLED" ->
            "该空间暂未开放会员。"

        "INVALID_MEMBERSHIP_PRICE" ->
            "会员价格配置异常。"

        "ALREADY_SPACE_MANAGER" ->
            "空间管理员无需购买会员。"

        "ALREADY_PERMANENT_MEMBER" ->
            "您已经是永久会员。"

        "PLAN_NOT_SELECTED" ->
            "请选择会员方案。"

        "PLAN_NOT_AVAILABLE" ->
            "该会员方案当前不可用。"

        "PLAN_NOT_ENABLED_FOR_SPACE" ->
            "该空间未开放此会员方案。"

        "MEMBERSHIP_PAYMENT_CONFIRMING" ->
            "支付结果正在确认，请稍后查看会员状态。"

        "MEMBERSHIP_PAYMENT_NOT_SETTLED" ->
            "暂时无法确认支付结果，请查看会员状态。"

        else -> wechatPayDisplayMessage(code)
    }
}