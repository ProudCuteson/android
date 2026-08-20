package top.role.android.feature.bounty

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import top.role.android.core.model.bounty.MobileBountyListMode
import top.role.android.core.model.engage.MobileBountyCard

@Composable
fun BountyListScreen(
    state: BountyListUiState,
    onModeChange: (MobileBountyListMode) -> Unit,
    onBountyClick: (String) -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
) {
    when {
        state.signedOut -> {
            CenterMessage(
                text = "登录后查看赏金任务",
            )
        }

        state.loading &&
                state.items.isEmpty() -> {
            LoadingContent()
        }

        state.errorMessage != null &&
                state.items.isEmpty() -> {
            ErrorContent(
                message = state.errorMessage,
                onRetry = onRetry,
            )
        }

        else -> {
            BountyListContent(
                state = state,
                onModeChange = onModeChange,
                onBountyClick = onBountyClick,
                onLoadMore = onLoadMore,
                onRetry = onRetry,
            )
        }
    }
}

@Composable
private fun BountyListContent(
    state: BountyListUiState,
    onModeChange: (MobileBountyListMode) -> Unit,
    onBountyClick: (String) -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = 16.dp,
            vertical = 12.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            BountyModeTabs(
                selectedMode = state.mode,
                onModeChange = onModeChange,
            )
        }

        if (state.items.isEmpty()) {
            item {
                Text(
                    text = when (state.mode) {
                        MobileBountyListMode.Featured ->
                            "暂无推荐任务"

                        MobileBountyListMode.Latest ->
                            "暂无最新任务"
                    },
                    modifier = Modifier.padding(
                        vertical = 24.dp,
                    ),
                    color = MaterialTheme
                        .colorScheme
                        .onSurfaceVariant,
                )
            }
        } else {
            items(
                items = state.items,
                key = { it.id },
            ) { item ->
                BountyListCard(
                    item = item,
                    onClick = {
                        onBountyClick(
                            item.id
                        )
                    },
                )
            }
        }

        if (
            !state.errorMessage.isNullOrBlank() &&
            state.items.isNotEmpty()
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment =
                        Alignment.CenterVertically,
                    horizontalArrangement =
                        Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = state.errorMessage,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme
                            .typography
                            .bodySmall,
                        color = MaterialTheme
                            .colorScheme
                            .error,
                    )

                    Text(
                        text = "重试",
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .clickable {
                                onRetry()
                            },
                        color = MaterialTheme
                            .colorScheme
                            .primary,
                    )
                }
            }
        }

        if (state.loadingMore) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        } else if (
            !state.isDone &&
            state.continueCursor.isNotBlank()
        ) {
            item {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onLoadMore,
                ) {
                    Text("加载更多")
                }
            }
        }
    }
}

@Composable
private fun BountyModeTabs(
    selectedMode: MobileBountyListMode,
    onModeChange: (MobileBountyListMode) -> Unit,
) {
    Row(
        horizontalArrangement =
            Arrangement.spacedBy(20.dp),
    ) {
        BountyModeTab(
            text = "推荐",
            selected =
                selectedMode ==
                        MobileBountyListMode.Featured,
            onClick = {
                onModeChange(
                    MobileBountyListMode.Featured
                )
            },
        )

        BountyModeTab(
            text = "最新",
            selected =
                selectedMode ==
                        MobileBountyListMode.Latest,
            onClick = {
                onModeChange(
                    MobileBountyListMode.Latest
                )
            },
        )
    }
}

@Composable
private fun BountyModeTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Text(
        text = text,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        style = MaterialTheme
            .typography
            .titleSmall,
        fontWeight = if (selected) {
            FontWeight.SemiBold
        } else {
            FontWeight.Normal
        },
        color = if (selected) {
            MaterialTheme
                .colorScheme
                .primary
        } else {
            MaterialTheme
                .colorScheme
                .onSurfaceVariant
        },
    )
}

@Composable
private fun BountyListCard(
    item: MobileBountyCard,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalArrangement =
            Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceBetween,
        ) {
            Text(
                text = item.title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme
                    .typography
                    .titleMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            if (item.featured) {
                Text(
                    text = "推荐",
                    modifier = Modifier.padding(
                        start = 12.dp,
                    ),
                    style = MaterialTheme
                        .typography
                        .labelMedium,
                    color = MaterialTheme
                        .colorScheme
                        .primary,
                )
            }
        }

        if (item.description.isNotBlank()) {
            Text(
                text = item.description,
                style = MaterialTheme
                    .typography
                    .bodyMedium,
                color = MaterialTheme
                    .colorScheme
                    .onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Row(
            horizontalArrangement =
                Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = bountyKindLabel(
                    item.kind
                ),
                style = MaterialTheme
                    .typography
                    .labelMedium,
                color = MaterialTheme
                    .colorScheme
                    .primary,
            )

            if (item.reward.isNotBlank()) {
                Text(
                    text = item.reward,
                    style = MaterialTheme
                        .typography
                        .labelMedium,
                )
            }

            if (item.deadlineLabel.isNotBlank()) {
                Text(
                    text = item.deadlineLabel,
                    style = MaterialTheme
                        .typography
                        .labelMedium,
                    color = MaterialTheme
                        .colorScheme
                        .onSurfaceVariant,
                )
            }
        }

        if (item.expired) {
            Text(
                text = "已截止",
                style = MaterialTheme
                    .typography
                    .labelSmall,
                color = MaterialTheme
                    .colorScheme
                    .error,
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(
                top = 6.dp,
            )
        )
    }
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun CenterMessage(
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
private fun ErrorContent(
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
            modifier = Modifier.padding(
                top = 16.dp,
            ),
            onClick = onRetry,
        ) {
            Text("重试")
        }
    }
}