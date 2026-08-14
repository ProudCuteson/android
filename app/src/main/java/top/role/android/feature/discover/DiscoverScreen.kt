package top.role.android.feature.discover

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.unit.dp

import top.role.android.core.model.content.MobileContentCard
import top.role.android.core.model.discover.MobileDiscoverPage
import top.role.android.core.model.space.MobileSpaceCard
import top.role.android.core.model.topic.MobileTopicCard
import top.role.android.core.model.space.SpaceListMode

import top.role.android.feature.discover.components.DiscoverSectionHeader
import top.role.android.feature.discover.components.EntityCard
import top.role.android.feature.discover.components.PublishedCard
import top.role.android.feature.discover.components.TopicCard

@Composable
fun DiscoverScreen(
    state: DiscoverUiState,

    onEntityClick: (MobileSpaceCard) -> Unit = {},
    onTopicClick: (MobileTopicCard) -> Unit = {},
    onDocClick: (MobileContentCard) -> Unit = {},

    onMoreEntities: (SpaceListMode) -> Unit = {},
    onMoreTopics: () -> Unit = {},
    onMoreDocs: () -> Unit = {},
) {
    when {
        state.isLoading -> {
            DiscoverLoading()
        }

        state.errorMessage != null -> {
            DiscoverError(
                message = state.errorMessage,
            )
        }

        state.page != null -> {
            DiscoverContent(
                page = state.page,

                onEntityClick =
                    onEntityClick,

                onTopicClick =
                    onTopicClick,

                onDocClick =
                    onDocClick,

                onMoreEntities =
                    onMoreEntities,

                onMoreTopics =
                    onMoreTopics,

                onMoreDocs =
                    onMoreDocs,
            )
        }

        else -> {
            DiscoverEmpty()
        }
    }
}

@Composable
private fun DiscoverContent(
    page: MobileDiscoverPage,

    onEntityClick:
        (MobileSpaceCard) -> Unit,

    onTopicClick:
        (MobileTopicCard) -> Unit,

    onDocClick:
        (MobileContentCard) -> Unit,

    onMoreEntities:
        (SpaceListMode) -> Unit,

    onMoreTopics:
        () -> Unit,

    onMoreDocs:
        () -> Unit,
) {

    val featuredSpaces =
        page.featuredSpaces

    val featuredIds =
        featuredSpaces
            .asSequence()
            .map { it.id }
            .toSet()

    val followingSpaces =
        page.followingSpaces
            .filterNot {
                it.id in featuredIds
            }

    val followingIds =
        followingSpaces
            .asSequence()
            .map { it.id }
            .toSet()

    val latestSpaces =
        page.latestSpaces
            .filterNot {
                it.id in featuredIds ||
                        it.id in followingIds
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

        /*
         * 推荐空间
         */
        if (
            featuredSpaces.isNotEmpty()
        ) {
            item(
                key = "featured-header"
            ) {
                DiscoverSectionHeader(
                    title = "推荐",
                    onMoreClick = {
                        onMoreEntities(
                            SpaceListMode.Featured
                        )
                    },
                )
            }

            items(
                items = featuredSpaces,
                key = {
                    "featured:${it.id}"
                },
            ) { item ->
                EntityCard(
                    item = item,
                    onClick = {
                        onEntityClick(item)
                    },
                )
            }
        }

        /*
         * 我关注的空间
         */
        if (
            followingSpaces.isNotEmpty()
        ) {
            item(
                key = "following-header"
            ) {
                DiscoverSectionHeader(
                    title = "我关注的",
                    onMoreClick = {
                        onMoreEntities(
                            SpaceListMode.Following
                        )
                    },
                )
            }

            items(
                items = followingSpaces,
                key = {
                    "following:${it.id}"
                },
            ) { item ->
                EntityCard(
                    item = item,
                    onClick = {
                        onEntityClick(item)
                    },
                )
            }
        }

        /*
         * 最新空间
         */
        if (
            latestSpaces.isNotEmpty()
        ) {
            item(
                key = "latest-header"
            ) {
                DiscoverSectionHeader(
                    title = "最新空间",
                    onMoreClick = {
                        onMoreEntities(
                            SpaceListMode.Latest
                        )
                    },
                )
            }

            items(
                items = latestSpaces,
                key = {
                    "latest:${it.id}"
                },
            ) { item ->
                EntityCard(
                    item = item,
                    onClick = {
                        onEntityClick(item)
                    },
                )
            }
        }

        /*
         * 话题
         */
        if (
            page.topics
                .isNotEmpty()
        ) {
            item(
                key = "topics-header"
            ) {
                DiscoverSectionHeader(
                    title = "话题",
                    onMoreClick =
                        onMoreTopics,
                )
            }

            items(
                items =
                    page.topics,

                key = {
                    "topic:${it.publishId}:${it.id}"
                },
            ) { item ->
                TopicCard(
                    item = item,
                    onClick = {
                        onTopicClick(item)
                    },
                )
            }
        }

        /*
         * 最新内容
         */
        if (
            page.contents
                .isNotEmpty()
        ) {
            item(
                key = "docs-header"
            ) {
                DiscoverSectionHeader(
                    title = "最新内容",
                    onMoreClick =
                        onMoreDocs,
                )
            }

            items(
                items =
                    page.contents,

                key = {
                    "content:${it.type}:${it.publishId}:${it.id}"
                },
            ) { item ->
                PublishedCard(
                    item = item,
                    onClick = {
                        onDocClick(item)
                    },
                )
            }
        }

        item(
            key = "bottom-space"
        ) {
            Spacer(
                modifier =
                    Modifier.height(24.dp),
            )
        }
    }
}

@Composable
private fun DiscoverLoading() {
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
private fun DiscoverError(
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
            text =
                "发现内容加载失败",

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
    }
}

@Composable
private fun DiscoverEmpty() {
    Column(
        modifier =
            Modifier.fillMaxSize(),
        verticalArrangement =
            Arrangement.Center,
        horizontalAlignment =
            Alignment.CenterHorizontally,
    ) {
        Text(
            text = "暂无发现内容",

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant,
        )
    }
}