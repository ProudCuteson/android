package top.role.android.feature.entity

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
import top.role.android.core.model.entity.EntityPublishedItem
import top.role.android.feature.entity.components.EntityContentCard
import top.role.android.feature.entity.components.EntityHeader
import top.role.android.feature.entity.components.EntityAnnouncementCard

@Composable
fun EntityDetailScreen(
    state: EntityDetailUiState,
    onContentClick: (EntityPublishedItem) -> Unit = {},
    onMembershipClick: (String) -> Unit = {},
) {
    when {
        state.isLoading -> {
            EntityDetailLoading()
        }

        state.errorMessage != null -> {
            EntityDetailError(
                message = state.errorMessage,
            )
        }

        state.detail?.entity == null -> {
            EntityDetailEmpty()
        }

        else -> {
            val detail = state.detail
            val entity = detail.entity

            val topics =
                detail.contents.filter {
                    it.type == "collection"
                }

            val regularContents =
                detail.contents.filter {
                    it.type == "doc" ||
                            it.type == "slide"
                }

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
                    key = "entity-header",
                ) {
                    EntityHeader(
                        entity = entity,
                        onMembershipClick = {
                            onMembershipClick(
                                entity.id
                            )
                        },
                    )
                }

                detail.announcement
                    ?.let { announcement ->
                        val hasContent =
                            announcement.blocks.isNotEmpty() ||
                                    announcement.md.isNotBlank()

                        if (hasContent) {
                            item(
                                key = "announcement",
                            ) {
                                EntityAnnouncementCard(
                                    announcement = announcement,
                                    modifier =
                                        Modifier.padding(
                                            top = 12.dp,
                                        ),
                                )
                            }
                        }
                    }

                if (topics.isNotEmpty()) {
                    item(
                        key = "topic-header",
                    ) {
                        Text(
                            text = "话题",
                            modifier = Modifier.padding(
                                top = 16.dp,
                                bottom = 2.dp,
                            ),
                            style =
                                MaterialTheme.typography.titleMedium,
                        )
                    }

                    items(
                        items = topics,
                        key = {
                            "topic:${it.publishId}:${it.id}"
                        },
                    ) { item ->
                        EntityContentCard(
                            item = item,
                            onClick = {
                                onContentClick(item)
                            },
                        )
                    }
                }

                if (regularContents.isNotEmpty()) {
                    item(
                        key = "content-header",
                    ) {
                        Text(
                            text = "内容",
                            modifier = Modifier.padding(
                                top = 16.dp,
                                bottom = 2.dp,
                            ),
                            style =
                                MaterialTheme.typography.titleMedium,
                        )
                    }

                    items(
                        items = regularContents,
                        key = {
                            "content:${it.publishId}:${it.id}"
                        },
                    ) { item ->
                        EntityContentCard(
                            item = item,
                            onClick = {
                                onContentClick(item)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EntityDetailLoading() {
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
private fun EntityDetailError(
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
            text = "空间加载失败",
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
private fun EntityDetailEmpty() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement =
            Arrangement.Center,
        horizontalAlignment =
            Alignment.CenterHorizontally,
    ) {
        Text(
            text = "空间不存在或不可访问",
            color =
                MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}