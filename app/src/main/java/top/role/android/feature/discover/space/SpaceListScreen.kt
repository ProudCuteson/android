package top.role.android.feature.discover.space

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import top.role.android.core.model.space.MobileSpaceCard
import top.role.android.core.model.space.SpaceListMode
import top.role.android.feature.discover.components.EntityCard

@Composable
fun SpaceListScreen(
    state: SpaceListUiState,
    onSpaceClick: (MobileSpaceCard) -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
) {
    when {
        state.isLoading &&
                state.items.isEmpty() -> {
            SpaceListLoading()
        }

        state.errorMessage != null &&
                state.items.isEmpty() -> {
            SpaceListError(
                message = state.errorMessage,
                onRetry = onRetry,
            )
        }

        state.items.isEmpty() -> {
            SpaceListEmpty(
                mode = state.mode,
            )
        }

        else -> {
            SpaceListContent(
                state = state,
                onSpaceClick = onSpaceClick,
                onLoadMore = onLoadMore,
            )
        }
    }
}

@Composable
private fun SpaceListContent(
    state: SpaceListUiState,
    onSpaceClick: (MobileSpaceCard) -> Unit,
    onLoadMore: () -> Unit,
) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = 16.dp,
            ),

        verticalArrangement =
            Arrangement.spacedBy(10.dp),
    ) {
        items(
            items = state.items,

            key = {
                it.id
            },
        ) { item ->
            EntityCard(
                item = item,
                onClick = {
                    onSpaceClick(item)
                },
            )
        }

        if (state.isLoadingMore) {
            item(
                key =
                    "space-list-loading-more"
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            vertical = 20.dp,
                        ),
                    contentAlignment =
                        Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        if (
            state.errorMessage != null &&
            state.items.isNotEmpty()
        ) {
            item(
                key =
                    "space-list-error-more"
            ) {
                Text(
                    text =
                        state.errorMessage,

                    modifier =
                        Modifier.padding(
                            vertical = 16.dp,
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
        }

        if (
            state.isDone &&
            state.items.isNotEmpty()
        ) {
            item(
                key =
                    "space-list-done"
            ) {
                Text(
                    text = "没有更多了",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            vertical = 16.dp,
                        ),
                    textAlign = TextAlign.Center,
                    style =
                        MaterialTheme
                            .typography
                            .labelSmall,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,
                )
            }
        }

        if (
            !state.isDone &&
            !state.isLoadingMore &&
            state.continueCursor != null
        ) {
            item(
                key = "space-list-load-more"
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            vertical = 12.dp,
                        ),
                    contentAlignment =
                        Alignment.Center,
                ) {
                    Text(
                        text = "加载更多",
                        modifier = Modifier
                            .clickable(
                                onClick = onLoadMore,
                            )
                            .padding(
                                horizontal = 24.dp,
                                vertical = 10.dp,
                            ),
                        textAlign = TextAlign.Center,
                        style =
                            MaterialTheme
                                .typography
                                .labelLarge,
                        color =
                            MaterialTheme
                                .colorScheme
                                .primary,
                    )
                }
            }
        }

    }
}

@Composable
private fun SpaceListLoading() {
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
private fun SpaceListError(
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
            text =
                "空间列表加载失败",

            style =
                MaterialTheme
                    .typography
                    .titleMedium,
        )

        Text(
            text = message,

            modifier =
                Modifier.padding(
                    top = 8.dp,
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

        Text(
            text = "点击重试",

            modifier = Modifier
                .padding(
                    top = 16.dp,
                )
                .clickable(
                    onClick = onRetry,
                ),

            style =
                MaterialTheme
                    .typography
                    .labelLarge,

            color =
                MaterialTheme
                    .colorScheme
                    .primary,
        )
    }
}

@Composable
private fun SpaceListEmpty(
    mode: SpaceListMode,
) {
    val message =
        when (mode) {
            SpaceListMode.Featured ->
                "暂无推荐空间"

            SpaceListMode.Following ->
                "暂无关注空间"

            SpaceListMode.Latest ->
                "暂无空间"
        }

    Box(
        modifier =
            Modifier.fillMaxSize(),

        contentAlignment =
            Alignment.Center,
    ) {
        Text(
            text = message,

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant,
        )
    }
}