package top.role.android.feature.bounty.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import top.role.android.core.model.bounty.MobileBountyDetail
import top.role.android.feature.bounty.bountyKindLabel

@Composable
fun BountyDetailScreen(
    state: BountyDetailUiState,
    onRetry: () -> Unit,
    onTake: () -> Unit,
    onOpenWeb: () -> Unit,
) {
    when {
        state.signedOut -> {
            BountyCenterMessage(
                text = "登录后查看任务",
            )
        }

        state.loading &&
                state.bounty == null -> {
            BountyDetailLoading()
        }

        state.bounty != null -> {
            BountyDetailContent(
                bounty = state.bounty,
                taking = state.taking,
                takeMessage = state.takeMessage,
                errorMessage = state.errorMessage,
                onTake = onTake,
                onOpenWeb = onOpenWeb,
            )
        }

        else -> {
            BountyDetailError(
                message =
                    state.errorMessage
                        ?: "任务不可用",

                onRetry =
                    onRetry,
            )
        }
    }
}

@Composable
private fun BountyDetailContent(
    bounty: MobileBountyDetail,
    taking: Boolean,
    takeMessage: String?,
    errorMessage: String?,
    onTake: () -> Unit,
    onOpenWeb: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(
                rememberScrollState()
            )
            .padding(16.dp),

        verticalArrangement =
            Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = bounty.title,
            style = MaterialTheme
                .typography
                .headlineSmall,
            fontWeight =
                FontWeight.SemiBold,
        )

        Row(
            horizontalArrangement =
                Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = bountyKindLabel(
                    bounty.kind
                ),
                color = MaterialTheme
                    .colorScheme
                    .primary,
                style = MaterialTheme
                    .typography
                    .labelLarge,
            )

            Text(
                text = bountyStatusLabel(
                    bounty.status,
                ),
                style = MaterialTheme
                    .typography
                    .labelLarge,
            )
        }

        if (bounty.reward.isNotBlank()) {
            Text(
                text = bounty.reward,
                style = MaterialTheme
                    .typography
                    .titleMedium,
                fontWeight =
                    FontWeight.Medium,
            )
        }

        if (
            bounty.deadlineLabel
                .isNotBlank()
        ) {
            Text(
                text = "截止：${bounty.deadlineLabel}",
                color = if (
                    bounty.expired
                ) {
                    MaterialTheme
                        .colorScheme
                        .error
                } else {
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
                },
            )
        }

        HorizontalDivider()

        if (bounty.description.isNotBlank()) {
            Text(
                text = bounty.description,
                style = MaterialTheme
                    .typography
                    .bodyLarge,
            )
        }

        if (bounty.content.isNotBlank()) {
            Text(
                text = bounty.content,
                style = MaterialTheme
                    .typography
                    .bodyMedium,
                color = MaterialTheme
                    .colorScheme
                    .onSurfaceVariant,
            )
        }

        if (bounty.references.isNotEmpty()) {
            HorizontalDivider()

            Text(
                text = "相关资料",
                style = MaterialTheme
                    .typography
                    .titleMedium,
                fontWeight =
                    FontWeight.Medium,
            )

            bounty.references.forEach {
                    reference ->

                Text(
                    text =
                        reference.label
                            .ifBlank {
                                reference.url
                            },

                    style = MaterialTheme
                        .typography
                        .bodyMedium,

                    color = MaterialTheme
                        .colorScheme
                        .primary,
                )
            }
        }

        if (
            !takeMessage.isNullOrBlank()
        ) {
            Text(
                text = takeMessage,
                color = MaterialTheme
                    .colorScheme
                    .primary,
            )
        }

        if (
            !errorMessage.isNullOrBlank()
        ) {
            Text(
                text = errorMessage,
                color = MaterialTheme
                    .colorScheme
                    .error,
            )
        }

        val takeEnabled =
            !taking &&
                    !bounty.ownedByMe &&
                    !bounty.taken &&
                    !bounty.expired &&
                    bounty.status == "open"

        Button(
            modifier =
                Modifier.fillMaxWidth(),

            enabled =
                takeEnabled,

            onClick =
                onTake,
        ) {
            Text(
                text =
                    when {
                        taking ->
                            "领取中..."

                        bounty.ownedByMe ->
                            "这是你发布的任务"

                        bounty.taken ->
                            "已领取"

                        bounty.expired ->
                            "已截止"

                        bounty.status != "open" ->
                            bountyStatusLabel(
                                bounty.status
                            )

                        else ->
                            "领取任务"
                    }
            )
        }

        if (
            bounty.taken &&
            !bounty.ownedByMe
        ) {
            Button(
                modifier =
                    Modifier.fillMaxWidth(),

                onClick =
                    onOpenWeb,
            ) {
                Text(
                    text =
                        "前往网页版提交"
                )
            }

            Text(
                text =
                    "成果提交与后续管理请在网页版完成",

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
            bounty.ownedByMe
        ) {
            Button(
                modifier =
                    Modifier.fillMaxWidth(),

                onClick =
                    onOpenWeb,
            ) {
                Text(
                    text =
                        "前往网页版管理"
                )
            }
        }

        if (
            bounty.allowMultipleSubmissions
        ) {
            Text(
                text =
                    "此任务允许多次提交",

                style = MaterialTheme
                    .typography
                    .bodySmall,

                color = MaterialTheme
                    .colorScheme
                    .onSurfaceVariant,
            )
        }
    }
}

private fun bountyStatusLabel(
    status: String,
): String {
    return when (status) {
        "open" -> "进行中"
        "resolved" -> "已解决"
        "closed" -> "已关闭"
        else -> status
    }
}

@Composable
private fun BountyDetailLoading() {
    Column(
        modifier =
            Modifier.fillMaxSize(),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun BountyCenterMessage(
    text: String,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center,
    ) {
        Text(text)
    }
}

@Composable
private fun BountyDetailError(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center,
    ) {
        Text(
            text = message,
            color = MaterialTheme
                .colorScheme
                .error,
        )

        Button(
            modifier =
                Modifier.padding(
                    top = 16.dp,
                ),

            onClick =
                onRetry,
        ) {
            Text("重试")
        }
    }
}