package top.role.android.feature.topic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.role.android.core.model.topic.TopicDocItem
import top.role.android.feature.topic.components.TopicDocCard
import top.role.android.feature.topic.components.TopicHeader

@Composable
fun TopicDetailScreen(
    state: TopicDetailUiState,

    onSpaceClick: () -> Unit = {},
    onDocClick: (TopicDocItem) -> Unit = {},
    onMembershipClick: () -> Unit = {},
) {
    when {
        state.isLoading -> {
            TopicDetailLoading()
        }

        state.errorMessage != null -> {
            TopicDetailError(
                message = state.errorMessage,
            )
        }

        state.detail?.topic == null -> {
            TopicDetailEmpty()
        }

        else -> {
            val detail = state.detail
            val topic = detail.topic!!

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
                    key = "topic-header",
                ) {
                    TopicHeader(
                        topic = topic,
                        space = detail.space,

                        onSpaceClick =
                            if (detail.space != null) {
                                onSpaceClick
                            } else {
                                null
                            },

                        onMembershipClick =
                            if (
                                topic.lockReason ==
                                "member_required"
                            ) {
                                onMembershipClick
                            } else {
                                null
                            },
                    )
                }

                if (detail.docs.isNotEmpty()) {
                    item(
                        key = "docs-header",
                    ) {
                        Text(
                            text = "内容",
                            modifier = Modifier.padding(
                                top = 14.dp,
                                bottom = 2.dp,
                            ),
                            style =
                                MaterialTheme.typography.titleMedium,
                        )
                    }

                    items(
                        items = detail.docs,
                        key = {
                            "${it.publishId}:${it.id}"
                        },
                    ) { doc ->
                        TopicDocCard(
                            item = doc,
                            onClick = {
                                onDocClick(doc)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopicDetailLoading() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement =
            Arrangement.Center,
        horizontalAlignment =
            Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun TopicDetailError(
    message: String,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement =
            Arrangement.Center,
        horizontalAlignment =
            Alignment.CenterHorizontally,
    ) {
        Text(
            text = "话题加载失败",
            style =
                MaterialTheme.typography.titleMedium,
        )

        Text(
            text = message,
            modifier = Modifier.padding(
                top = 8.dp,
            ),
            color =
                MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TopicDetailEmpty() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement =
            Arrangement.Center,
        horizontalAlignment =
            Alignment.CenterHorizontally,
    ) {
        Text(
            text = "话题不存在或不可访问",
            color =
                MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}