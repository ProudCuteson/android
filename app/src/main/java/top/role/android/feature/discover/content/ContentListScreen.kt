package top.role.android.feature.discover.content

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
import top.role.android.core.model.content.MobileContentCard
import top.role.android.core.model.content.MobileContentType
import top.role.android.feature.discover.components.ContentCard

@Composable
fun ContentListScreen(
    state: ContentListUiState,
    onContentClick: (MobileContentCard) -> Unit,
    onLoadMore: (MobileContentType) -> Unit,
    onRetry: () -> Unit,
) {
    val hasAnyItems =
        state.slides.isNotEmpty() ||
                state.docs.isNotEmpty()

    when {
        state.isLoading &&
                !hasAnyItems -> {
            ContentListLoading()
        }

        !state.isLoading &&
                !hasAnyItems &&
                hasInitialError(state) -> {
            ContentListError(
                onRetry = onRetry,
            )
        }

        else -> {
            ContentListContent(
                state = state,
                onContentClick =
                    onContentClick,
                onLoadMore =
                    onLoadMore,
            )
        }
    }
}

@Composable
private fun ContentListContent(
    state: ContentListUiState,
    onContentClick:
        (MobileContentCard) -> Unit,
    onLoadMore:
        (MobileContentType) -> Unit,
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
                "content-slide-title"
        ) {
            ContentSectionHeader(
                title =
                    "演示文稿",
            )
        }

        contentSectionItems(
            contentType =
                MobileContentType.Slide,

            items =
                state.slides,

            isLoading =
                state.isLoadingSlides,

            isLoadingMore =
                state.isLoadingMoreSlides,

            isDone =
                state.slidesDone,

            errorMessage =
                slideErrorMessage(
                    state
                ),

            emptyText =
                "暂无演示文稿",

            onContentClick =
                onContentClick,

            onLoadMore =
                onLoadMore,
        )

        item(
            key =
                "content-doc-spacer"
        ) {
            Spacer(
                modifier =
                    Modifier.height(
                        12.dp
                    ),
            )
        }

        item(
            key =
                "content-doc-title"
        ) {
            ContentSectionHeader(
                title =
                    "文档",
            )
        }

        contentSectionItems(
            contentType =
                MobileContentType.Doc,

            items =
                state.docs,

            isLoading =
                state.isLoadingDocs,

            isLoadingMore =
                state.isLoadingMoreDocs,

            isDone =
                state.docsDone,

            errorMessage =
                docErrorMessage(
                    state
                ),

            emptyText =
                "暂无文档",

            onContentClick =
                onContentClick,

            onLoadMore =
                onLoadMore,
        )

        item(
            key =
                "content-bottom-space"
        ) {
            Spacer(
                modifier =
                    Modifier.height(
                        16.dp
                    ),
            )
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope
        .contentSectionItems(
    contentType: MobileContentType,
    items: List<MobileContentCard>,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    isDone: Boolean,
    errorMessage: String?,
    emptyText: String,
    onContentClick:
        (MobileContentCard) -> Unit,
    onLoadMore:
        (MobileContentType) -> Unit,
) {
    items(
        items = items,
        key = {
            "${contentType.value}:${it.id}"
        },
    ) { item ->
        ContentCard(
            item = item,
            onClick = {
                onContentClick(item)
            },
        )
    }

    if (
        isLoading &&
        items.isEmpty()
    ) {
        item(
            key =
                "content-${contentType.value}-loading"
        ) {
            ContentSectionLoading()
        }

        return
    }

    if (
        errorMessage != null &&
        items.isEmpty()
    ) {
        item(
            key =
                "content-${contentType.value}-error"
        ) {
            ContentSectionError(
                message =
                    errorMessage,
            )
        }

        return
    }

    if (items.isEmpty()) {
        item(
            key =
                "content-${contentType.value}-empty"
        ) {
            ContentSectionEmpty(
                message =
                    emptyText,
            )
        }

        return
    }

    if (isLoadingMore) {
        item(
            key =
                "content-${contentType.value}-loading-more"
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
        errorMessage != null &&
        items.isNotEmpty()
    ) {
        item(
            key =
                "content-${contentType.value}-error-more"
        ) {
            Text(
                text =
                    errorMessage,
                modifier =
                    Modifier.padding(
                        vertical = 12.dp,
                    ),
                style =
                    MaterialTheme.typography
                        .bodyMedium,
                color =
                    MaterialTheme.colorScheme
                        .error,
            )
        }
    }

    if (
        !isDone &&
        !isLoadingMore
    ) {
        item(
            key =
                "content-${contentType.value}-load-more"
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
                    text =
                        "加载更多",
                    modifier = Modifier
                        .clickable {
                            onLoadMore(
                                contentType
                            )
                        }
                        .padding(
                            horizontal =
                                24.dp,
                            vertical =
                                10.dp,
                        ),
                    textAlign =
                        TextAlign.Center,
                    style =
                        MaterialTheme.typography
                            .labelLarge,
                    color =
                        MaterialTheme.colorScheme
                            .primary,
                )
            }
        }
    }
}

@Composable
private fun ContentSectionHeader(
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
            MaterialTheme.typography
                .titleMedium,
        fontWeight =
            FontWeight.SemiBold,
    )
}

@Composable
private fun ContentSectionLoading() {
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
private fun ContentSectionError(
    message: String,
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
        Text(
            text = message,
            style =
                MaterialTheme.typography
                    .bodyMedium,
            color =
                MaterialTheme.colorScheme
                    .error,
        )
    }
}

@Composable
private fun ContentSectionEmpty(
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
                MaterialTheme.typography
                    .bodyMedium,
            color =
                MaterialTheme.colorScheme
                    .onSurfaceVariant,
        )
    }
}

@Composable
private fun ContentListLoading() {
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
private fun ContentListError(
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
                "内容加载失败",
            style =
                MaterialTheme.typography
                    .titleMedium,
        )

        Text(
            text =
                "暂时无法加载内容",
            modifier =
                Modifier.padding(
                    top = 8.dp,
                ),
            style =
                MaterialTheme.typography
                    .bodyMedium,
            color =
                MaterialTheme.colorScheme
                    .onSurfaceVariant,
        )

        Text(
            text =
                "点击重试",
            modifier = Modifier
                .padding(
                    top = 16.dp,
                )
                .clickable(
                    onClick =
                        onRetry,
                ),
            style =
                MaterialTheme.typography
                    .labelLarge,
            color =
                MaterialTheme.colorScheme
                    .primary,
        )
    }
}

private fun hasInitialError(
    state: ContentListUiState,
): Boolean {
    return state.slideFeatured.errorMessage != null ||
            state.slideLatest.errorMessage != null ||
            state.docFeatured.errorMessage != null ||
            state.docLatest.errorMessage != null
}

private fun slideErrorMessage(
    state: ContentListUiState,
): String? {
    return state.slideFeatured.errorMessage
        ?: state.slideLatest.errorMessage
}

private fun docErrorMessage(
    state: ContentListUiState,
): String? {
    return state.docFeatured.errorMessage
        ?: state.docLatest.errorMessage
}