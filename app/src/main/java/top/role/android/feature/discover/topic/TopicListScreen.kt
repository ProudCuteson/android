package top.role.android.feature.discover.topic

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import top.role.android.core.model.topic.MobileTopicCard
import top.role.android.core.model.topic.TopicListMode
import top.role.android.feature.discover.components.TopicCard

@Composable
fun TopicListScreen(
    state: TopicListUiState,
    onTopicClick: (MobileTopicCard) -> Unit,
    onLoadMore: (TopicListMode) -> Unit,
    onRetry: () -> Unit,
    onRetrySection: (TopicListMode) -> Unit,
) {
    val hasAnyItems =
        state.featured.items.isNotEmpty() ||
                state.following.items.isNotEmpty() ||
                state.latest.items.isNotEmpty()

    val allFailed =
        !state.isLoading &&
                !hasAnyItems &&
                state.featured.errorMessage != null &&
                state.following.errorMessage != null &&
                state.latest.errorMessage != null

    when {
        state.isLoading &&
                !hasAnyItems -> {
            TopicListLoading()
        }

        allFailed -> {
            TopicListError(
                onRetry = onRetry,
            )
        }

        else -> {
            TopicListContent(
                state = state,
                onTopicClick = onTopicClick,
                onLoadMore = onLoadMore,
                onRetrySection = onRetrySection,
            )
        }
    }
}

@Composable
private fun TopicListContent(
    state: TopicListUiState,
    onTopicClick: (MobileTopicCard) -> Unit,
    onLoadMore: (TopicListMode) -> Unit,
    onRetrySection: (TopicListMode) -> Unit,
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
        item(
            key =
                "topic-section-featured-title"
        ) {
            TopicSectionHeader(
                title = "推荐话题",
            )
        }

        topicSectionItems(
            mode =
                TopicListMode.Featured,

            section =
                state.featured,

            emptyText =
                "暂无推荐话题",

            onTopicClick =
                onTopicClick,

            onLoadMore =
                onLoadMore,

            onRetrySection =
                onRetrySection,
        )

        item(
            key =
                "topic-section-following-spacer"
        ) {
            Spacer(
                modifier =
                    Modifier.height(12.dp),
            )
        }

        item(
            key =
                "topic-section-following-title"
        ) {
            TopicSectionHeader(
                title = "我关注的话题",
            )
        }

        topicSectionItems(
            mode =
                TopicListMode.Following,

            section =
                state.following,

            emptyText =
                "暂无关注话题",

            onTopicClick =
                onTopicClick,

            onLoadMore =
                onLoadMore,

            onRetrySection =
                onRetrySection,
        )

        item(
            key =
                "topic-section-latest-spacer"
        ) {
            Spacer(
                modifier =
                    Modifier.height(12.dp),
            )
        }

        item(
            key =
                "topic-section-latest-title"
        ) {
            TopicSectionHeader(
                title = "最新话题",
            )
        }

        topicSectionItems(
            mode =
                TopicListMode.Latest,

            section =
                state.latest,

            emptyText =
                "暂无最新话题",

            onTopicClick =
                onTopicClick,

            onLoadMore =
                onLoadMore,

            onRetrySection =
                onRetrySection,
        )

        item(
            key =
                "topic-list-bottom-space"
        ) {
            Spacer(
                modifier =
                    Modifier.height(16.dp),
            )
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope
        .topicSectionItems(
    mode: TopicListMode,
    section: TopicListSectionState,
    emptyText: String,
    onTopicClick: (MobileTopicCard) -> Unit,
    onLoadMore: (TopicListMode) -> Unit,
    onRetrySection: (TopicListMode) -> Unit,
) {
    items(
        items = section.items,

        key = {
            "${mode.value}:${it.id}"
        },
    ) { item ->
        TopicCard(
            item = item,

            onClick = {
                onTopicClick(item)
            },
        )
    }

    if (
        section.isLoading &&
        section.items.isEmpty()
    ) {
        item(
            key =
                "topic-${mode.value}-loading"
        ) {
            TopicSectionLoading()
        }

        return
    }

    if (
        section.errorMessage != null &&
        section.items.isEmpty()
    ) {
        item(
            key =
                "topic-${mode.value}-error"
        ) {
            TopicSectionError(
                message =
                    section.errorMessage,

                onRetry = {
                    onRetrySection(mode)
                },
            )
        }

        return
    }

    if (
        section.items.isEmpty()
    ) {
        item(
            key =
                "topic-${mode.value}-empty"
        ) {
            TopicSectionEmpty(
                message =
                    emptyText,
            )
        }

        return
    }

    if (section.isLoadingMore) {
        item(
            key =
                "topic-${mode.value}-loading-more"
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = 18.dp,
                    ),

                contentAlignment =
                    Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
    }

    if (
        section.errorMessage != null &&
        section.items.isNotEmpty()
    ) {
        item(
            key =
                "topic-${mode.value}-error-more"
        ) {
            Text(
                text =
                    section.errorMessage,

                modifier =
                    Modifier.padding(
                        vertical = 12.dp,
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
        !section.isDone &&
        !section.isLoadingMore &&
        section.continueCursor != null
    ) {
        item(
            key =
                "topic-${mode.value}-load-more"
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = 8.dp,
                    ),

                contentAlignment =
                    Alignment.Center,
            ) {
                Text(
                    text = "加载更多",

                    modifier = Modifier
                        .clickable {
                            onLoadMore(mode)
                        }
                        .padding(
                            horizontal = 24.dp,
                            vertical = 10.dp,
                        ),

                    textAlign =
                        TextAlign.Center,

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

@Composable
private fun TopicSectionHeader(
    title: String,
) {
    Text(
        text = title,

        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 14.dp,
                bottom = 2.dp,
            ),

        style =
            MaterialTheme
                .typography
                .titleMedium,

        fontWeight =
            FontWeight.SemiBold,
    )
}

@Composable
private fun TopicSectionLoading() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = 24.dp,
            ),

        contentAlignment =
            Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun TopicSectionError(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = 18.dp,
            ),

        horizontalAlignment =
            Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,

            style =
                MaterialTheme
                    .typography
                    .bodyMedium,

            color =
                MaterialTheme
                    .colorScheme
                    .error,
        )

        Text(
            text = "点击重试",

            modifier = Modifier
                .padding(
                    top = 8.dp,
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
private fun TopicSectionEmpty(
    message: String,
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
        Text(
            text = message,

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
private fun TopicListLoading() {
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
private fun TopicListError(
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
                "话题加载失败",

            style =
                MaterialTheme
                    .typography
                    .titleMedium,
        )

        Text(
            text =
                "暂时无法加载话题内容",

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