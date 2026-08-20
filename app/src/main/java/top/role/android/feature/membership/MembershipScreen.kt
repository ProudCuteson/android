package top.role.android.feature.membership

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.role.android.core.model.membership.MobileMembershipPlan

@Composable
fun MembershipScreen(
    uiState: MembershipUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onSelectPlan: (String) -> Unit,
    onPurchase: () -> Unit,
) {
    when {
        uiState.loading -> {
            MembershipLoadingState()
        }

        uiState.loadError != null -> {
            MembershipLoadErrorState(
                message =
                    uiState.loadError,
                onBack =
                    onBack,
                onRetry =
                    onRetry,
            )
        }

        else -> {
            MembershipContent(
                uiState =
                    uiState,
                onBack =
                    onBack,
                onSelectPlan =
                    onSelectPlan,
                onPurchase =
                    onPurchase,
            )
        }
    }
}

@Composable
private fun MembershipLoadingState() {
    Box(
        modifier =
            Modifier.fillMaxSize(),
        contentAlignment =
            Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun MembershipLoadErrorState(
    message: String,
    onBack: () -> Unit,
    onRetry: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(20.dp),
    ) {
        MembershipTopBar(
            onBack =
                onBack,
        )

        Spacer(
            modifier =
                Modifier.height(32.dp),
        )

        Text(
            text =
                "暂时无法加载",
            style =
                MaterialTheme
                    .typography
                    .titleLarge,
        )

        Spacer(
            modifier =
                Modifier.height(8.dp),
        )

        Text(
            text =
                displayMembershipError(
                    message
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

        Spacer(
            modifier =
                Modifier.height(20.dp),
        )

        Button(
            onClick =
                onRetry,
        ) {
            Text(
                text =
                    "重新加载",
            )
        }
    }
}

@Composable
private fun MembershipContent(
    uiState: MembershipUiState,
    onBack: () -> Unit,
    onSelectPlan: (String) -> Unit,
    onPurchase: () -> Unit,
) {
    val page =
        uiState.page
            ?: return

    val space =
        page.space
            ?: return

    val membership =
        page.membership
            ?: return

    val viewer =
        page.viewer
            ?: return

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(
                    horizontal = 20.dp,
                    vertical = 12.dp,
                ),
    ) {
        MembershipTopBar(
            onBack =
                onBack,
        )

        Spacer(
            modifier =
                Modifier.height(20.dp),
        )

        /*
         * Space
         */
        Text(
            text =
                "空间会员",
            style =
                MaterialTheme
                    .typography
                    .labelLarge,
            color =
                MaterialTheme
                    .colorScheme
                    .primary,
        )

        Spacer(
            modifier =
                Modifier.height(6.dp),
        )

        Text(
            text =
                space.name,
            style =
                MaterialTheme
                    .typography
                    .headlineSmall,
        )

        space.description
            ?.trim()
            ?.takeIf {
                it.isNotEmpty()
            }
            ?.let { description ->
                Spacer(
                    modifier =
                        Modifier.height(6.dp),
                )

                Text(
                    text =
                        description,
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

        space.number
            ?.let { number ->
                Spacer(
                    modifier =
                        Modifier.height(6.dp),
                )

                Text(
                    text =
                        "空间号 ${formatSpaceNumber(number)}",
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
                Modifier.height(24.dp),
        )

        /*
         * Viewer current state
         */
        MembershipViewerNotice(
            isManager =
                viewer.isManager,
            isPermanentMember =
                viewer.isPermanentMember,
            isMember =
                viewer.isMember,
            activeUntil =
                viewer.activeUntil,
        )

        if (
            viewer.isManager ||
            viewer.isPermanentMember ||
            viewer.isMember
        ) {
            Spacer(
                modifier =
                    Modifier.height(20.dp),
            )
        }

        HorizontalDivider()

        Spacer(
            modifier =
                Modifier.height(24.dp),
        )

        /*
         * Membership disabled
         */
        if (!membership.enabled) {
            Text(
                text =
                    "暂未开放会员",
                style =
                    MaterialTheme
                        .typography
                        .titleMedium,
            )

            Spacer(
                modifier =
                    Modifier.height(6.dp),
            )

            Text(
                text =
                    "该空间目前没有开放付费会员。",
                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant,
            )

            Spacer(
                modifier =
                    Modifier.height(40.dp),
            )

            return@Column
        }

        /*
         * Plans
         */
        Text(
            text =
                "选择会员方案",
            style =
                MaterialTheme
                    .typography
                    .titleLarge,
        )

        membership.description
            ?.trim()
            ?.takeIf {
                it.isNotEmpty()
            }
            ?.let { description ->
                Spacer(
                    modifier =
                        Modifier.height(6.dp),
                )

                Text(
                    text =
                        description,
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

        Spacer(
            modifier =
                Modifier.height(16.dp),
        )

        membership.plans
            .forEach { plan ->
                MembershipPlanCard(
                    plan =
                        plan,
                    selected =
                        uiState
                            .selectedPlanKey ==
                                plan.key,
                    enabled =
                        !uiState.paying &&
                                !uiState.confirming,
                    onClick = {
                        onSelectPlan(
                            plan.key
                        )
                    },
                )

                Spacer(
                    modifier =
                        Modifier.height(10.dp),
                )
            }

        /*
         * Payment status
         */
        uiState.paymentError
            ?.trim()
            ?.takeIf {
                it.isNotEmpty()
            }
            ?.let { error ->
                Spacer(
                    modifier =
                        Modifier.height(8.dp),
                )

                Text(
                    text =
                        displayMembershipError(
                            error
                        ),
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

        if (uiState.confirming) {
            Spacer(
                modifier =
                    Modifier.height(12.dp),
            )

            Text(
                text =
                    "正在确认会员状态…",
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

        if (uiState.paymentSuccess) {
            Spacer(
                modifier =
                    Modifier.height(12.dp),
            )

            Text(
                text =
                    "会员已开通",
                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,
                color =
                    MaterialTheme
                        .colorScheme
                        .primary,
            )
        }

        Spacer(
            modifier =
                Modifier.height(24.dp),
        )

        /*
         * Action
         */
        Button(
            onClick =
                onPurchase,
            modifier =
                Modifier.fillMaxWidth(),
            enabled =
                !uiState.paying &&
                        !uiState.confirming &&
                        !viewer.isManager &&
                        !viewer.isPermanentMember &&
                        !uiState
                            .selectedPlanKey
                            .isNullOrBlank(),
        ) {
            Text(
                text =
                    when {
                        uiState.confirming ->
                            "确认中"

                        uiState.paying ->
                            "正在支付"

                        viewer.isManager ->
                            "管理员无需开通"

                        viewer.isPermanentMember ->
                            "已是永久会员"

                        viewer.isMember ->
                            "继续续费"

                        else ->
                            "立即开通"
                    },
            )
        }

        Spacer(
            modifier =
                Modifier.height(40.dp),
        )
    }
}

@Composable
private fun MembershipTopBar(
    onBack: () -> Unit,
) {
    Row(
        modifier =
            Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.SpaceBetween,
        verticalAlignment =
            Alignment.CenterVertically,
    ) {
        TextButton(
            onClick =
                onBack,
        ) {
            Text(
                text =
                    "返回",
            )
        }

        Text(
            text =
                "空间会员",
            style =
                MaterialTheme
                    .typography
                    .titleMedium,
        )

        /*
         * 保持标题居中时的占位。
         */
        Spacer(
            modifier =
                Modifier
                    .padding(
                        horizontal = 24.dp
                    ),
        )
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
            title =
                "空间管理员"

            description =
                "您是该空间的管理员，无需购买会员。"
        }

        isPermanentMember -> {
            title =
                "永久会员"

            description =
                "您已经拥有该空间的永久会员权限。"
        }

        isMember -> {
            title =
                "当前会员"

            description =
                activeUntil
                    ?.let {
                        "当前会员有效期至 ${formatMembershipDate(it)}，继续购买会在当前有效期基础上顺延。"
                    }
                    ?: "当前会员有效，继续购买会在现有会员期限基础上顺延。"
        }

        else -> {
            return
        }
    }

    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(16.dp),
        tonalElevation =
            1.dp,
    ) {
        Column(
            modifier =
                Modifier.padding(16.dp),
        ) {
            Text(
                text =
                    title,
                style =
                    MaterialTheme
                        .typography
                        .titleMedium,
            )

            Spacer(
                modifier =
                    Modifier.height(6.dp),
            )

            Text(
                text =
                    description,
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
}

@Composable
private fun MembershipPlanCard(
    plan: MobileMembershipPlan,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    enabled =
                        enabled,
                    onClick =
                        onClick,
                ),
        shape =
            RoundedCornerShape(16.dp),
        border =
            BorderStroke(
                width =
                    if (selected) {
                        2.dp
                    } else {
                        1.dp
                    },
                color =
                    if (selected) {
                        MaterialTheme
                            .colorScheme
                            .primary
                    } else {
                        MaterialTheme
                            .colorScheme
                            .outlineVariant
                    },
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalArrangement =
                Arrangement.SpaceBetween,
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
                            plan.label,
                        style =
                            MaterialTheme
                                .typography
                                .titleMedium,
                    )

                    if (
                        plan.key ==
                        "year"
                    ) {
                        Spacer(
                            modifier =
                                Modifier.padding(
                                    horizontal = 4.dp
                                ),
                        )

                        Text(
                            text =
                                "推荐",
                            style =
                                MaterialTheme
                                    .typography
                                    .labelSmall,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .primary,
                        )
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(4.dp),
                )

                Text(
                    text =
                        "有效期 ${formatMonthCount(plan.months)} 个月",
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
                        formatMembershipPrice(
                            amountMinor =
                                plan.totalPriceCents,
                            currency =
                                plan.currency,
                        ),
                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,
                )

                Spacer(
                    modifier =
                        Modifier.height(2.dp),
                )

                Text(
                    text =
                        "一次支付",
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
    }
}

private fun formatMembershipPrice(
    amountMinor: Double,
    currency: String,
): String {
    val amount =
        amountMinor / 100.0

    return when (currency) {
        "USD" ->
            "$%.2f".format(
                amount
            )

        else ->
            "¥%.2f".format(
                amount
            )
    }
}

private fun formatMonthCount(
    months: Double,
): String {
    return if (
        months % 1.0 == 0.0
    ) {
        months
            .toInt()
            .toString()
    } else {
        months.toString()
    }
}

private fun formatSpaceNumber(
    number: Double,
): String {
    return if (
        number % 1.0 == 0.0
    ) {
        number
            .toLong()
            .toString()
    } else {
        number.toString()
    }
}

private fun formatMembershipDate(
    timestamp: Double,
): String {
    val date =
        java.time.Instant
            .ofEpochMilli(
                timestamp.toLong()
            )
            .atZone(
                java.time.ZoneId
                    .systemDefault()
            )
            .toLocalDate()

    return date.toString()
}

private fun displayMembershipError(
    code: String,
): String {
    return when (code) {
        "SPACE_NOT_FOUND" ->
            "空间不存在或已删除"

        "SPACE_NOT_ACTIVE" ->
            "该空间当前不可用"

        "MEMBERSHIP_NOT_ENABLED" ->
            "该空间暂未开放会员"

        "INVALID_MEMBERSHIP_PRICE" ->
            "会员价格配置异常"

        "ALREADY_SPACE_MANAGER" ->
            "空间管理员无需购买会员"

        "ALREADY_PERMANENT_MEMBER" ->
            "您已经是永久会员"

        "PLAN_NOT_SELECTED" ->
            "请选择会员方案"

        "PLAN_NOT_AVAILABLE" ->
            "该会员方案当前不可用"

        "PLAN_NOT_ENABLED_FOR_SPACE" ->
            "该空间未开放此会员方案"

        "APP_NOT_AUTHENTICATED",
        "APP_SESSION_INVALID",
        "APP_SESSION_NOT_FOUND",
        "APP_SESSION_REVOKED",
        "APP_SESSION_EXPIRED" ->
            "登录状态已失效，请重新登录"

        "WECHAT_NOT_INSTALLED" ->
            "未检测到微信"

        "WECHAT_PAY_REQUEST_REJECTED" ->
            "无法调起微信支付"

        "MEMBERSHIP_PAYMENT_CONFIRMING" ->
            "支付结果正在确认中，请稍后查看会员状态"

        "MEMBERSHIP_PAYMENT_NOT_SETTLED" ->
            "支付未完成"

        else ->
            code
    }
}