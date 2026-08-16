package top.role.android.feature.main.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

import top.role.android.app.navigation.ContentListRoute
import top.role.android.app.navigation.DocDetailRoute
import top.role.android.app.navigation.EntityDetailRoute
import top.role.android.app.navigation.MainDestination
import top.role.android.app.navigation.SlideDetailRoute
import top.role.android.app.navigation.SpaceListRoute
import top.role.android.app.navigation.TopicDetailRoute
import top.role.android.app.navigation.TopicListRoute

import top.role.android.core.model.space.SpaceListMode

import top.role.android.feature.discover.DiscoverScreen
import top.role.android.feature.discover.DiscoverViewModel
import top.role.android.feature.discover.DiscoverViewModelFactory

import top.role.android.feature.discover.content.ContentListScreen
import top.role.android.feature.discover.content.ContentListViewModel
import top.role.android.feature.discover.content.ContentListViewModelFactory

import top.role.android.feature.discover.space.SpaceListScreen
import top.role.android.feature.discover.space.SpaceListViewModel
import top.role.android.feature.discover.space.SpaceListViewModelFactory

import top.role.android.feature.discover.topic.TopicListScreen
import top.role.android.feature.discover.topic.TopicListViewModel
import top.role.android.feature.discover.topic.TopicListViewModelFactory

import top.role.android.feature.entity.EntityDetailScreen
import top.role.android.feature.entity.EntityDetailViewModel
import top.role.android.feature.entity.EntityDetailViewModelFactory

import top.role.android.feature.reading.DocDetailScreen
import top.role.android.feature.reading.DocDetailViewModel
import top.role.android.feature.reading.DocDetailViewModelFactory

import top.role.android.feature.slide.SlideDetailScreen
import top.role.android.feature.slide.SlideDetailViewModel
import top.role.android.feature.slide.SlideDetailViewModelFactory

import top.role.android.feature.topic.TopicDetailScreen
import top.role.android.feature.topic.TopicDetailViewModel
import top.role.android.feature.topic.TopicDetailViewModelFactory

fun NavGraphBuilder.discoverNavGraph(
    navController: NavHostController,
    dependencies: MainNavigationDependencies,
) {

    /*
     * Discover Home
     */

    composable(
        route =
            MainDestination.Discover.route,
    ) {
        val discoverViewModel:
                DiscoverViewModel =
            viewModel(
                factory =
                    DiscoverViewModelFactory(
                        repository =
                            dependencies
                                .discoverRepository,
                    )
            )

        val state by
        discoverViewModel
            .uiState
            .collectAsStateWithLifecycle()

        DiscoverScreen(
            state =
                state,

            onEntityClick = { item ->
                navController.navigate(
                    EntityDetailRoute.create(
                        type =
                            "space",

                        id =
                            item.id,
                    )
                )
            },

            onTopicClick = { item ->
                navController.navigate(
                    TopicDetailRoute.create(
                        id =
                            item.id,

                        publishId =
                            item.publishId,
                    )
                )
            },

            onDocClick = { item ->
                when (
                    item.type
                ) {
                    "doc" -> {
                        navController.navigate(
                            DocDetailRoute.create(
                                docId =
                                    item.id,
                            )
                        )
                    }

                    "slide" -> {
                        navController.navigate(
                            SlideDetailRoute.create(
                                slideId =
                                    item.id,
                            )
                        )
                    }
                }
            },

            onMoreEntities = { mode ->
                navController.navigate(
                    SpaceListRoute.create(
                        mode =
                            mode,
                    )
                )
            },

            onMoreTopics = {
                navController.navigate(
                    TopicListRoute.route
                )
            },

            onMoreContent = {
                navController.navigate(
                    ContentListRoute.route
                )
            },
        )
    }

    /*
     * Space list
     */

    composable(
        route =
            SpaceListRoute.route,

        arguments =
            listOf(
                navArgument(
                    SpaceListRoute.modeArg
                ) {
                    type =
                        NavType.StringType
                },
            ),
    ) { backStackEntry ->

        val mode =
            SpaceListMode.fromValue(
                backStackEntry
                    .arguments
                    ?.getString(
                        SpaceListRoute.modeArg
                    )
            )

        val factory =
            remember(
                mode,
                dependencies.spaceListRepository,
            ) {
                SpaceListViewModelFactory(
                    mode =
                        mode,

                    repository =
                        dependencies
                            .spaceListRepository,
                )
            }

        val spaceListViewModel:
                SpaceListViewModel =
            viewModel(
                key =
                    "space-list:${mode.value}",

                factory =
                    factory,
            )

        val state by
        spaceListViewModel
            .uiState
            .collectAsStateWithLifecycle()

        SpaceListScreen(
            state =
                state,

            onSpaceClick = { item ->
                navController.navigate(
                    EntityDetailRoute.create(
                        type =
                            "space",

                        id =
                            item.id,
                    )
                )
            },

            onLoadMore =
                spaceListViewModel::loadMore,

            onRetry =
                spaceListViewModel::retry,
        )
    }

    /*
     * Topic list
     */

    composable(
        route =
            TopicListRoute.route,
    ) {
        val factory =
            remember(
                dependencies
                    .topicListRepository,
            ) {
                TopicListViewModelFactory(
                    repository =
                        dependencies
                            .topicListRepository,
                )
            }

        val topicListViewModel:
                TopicListViewModel =
            viewModel(
                key =
                    "topic-list",

                factory =
                    factory,
            )

        val state by
        topicListViewModel
            .uiState
            .collectAsStateWithLifecycle()

        TopicListScreen(
            state =
                state,

            onTopicClick = { item ->
                navController.navigate(
                    TopicDetailRoute.create(
                        id =
                            item.id,

                        publishId =
                            item.publishId,
                    )
                )
            },

            onLoadMore = { mode ->
                topicListViewModel.loadMore(
                    mode =
                        mode,
                )
            },

            onRetry =
                topicListViewModel::retry,

            onRetrySection = { mode ->
                topicListViewModel.retrySection(
                    mode =
                        mode,
                )
            },
        )
    }

    /*
     * Content list
     */

    composable(
        route =
            ContentListRoute.route,
    ) {
        val factory =
            remember(
                dependencies
                    .contentListRepository,
            ) {
                ContentListViewModelFactory(
                    repository =
                        dependencies
                            .contentListRepository,
                )
            }

        val contentListViewModel:
                ContentListViewModel =
            viewModel(
                key =
                    "content-list",

                factory =
                    factory,
            )

        val state by
        contentListViewModel
            .uiState
            .collectAsStateWithLifecycle()

        ContentListScreen(
            state =
                state,

            onContentClick = { item ->
                when (
                    item.type
                ) {
                    "doc" -> {
                        navController.navigate(
                            DocDetailRoute.create(
                                docId =
                                    item.id,
                            )
                        )
                    }

                    "slide" -> {
                        navController.navigate(
                            SlideDetailRoute.create(
                                slideId =
                                    item.id,
                            )
                        )
                    }
                }
            },

            onLoadMore = { type ->
                contentListViewModel.loadMore(
                    contentType =
                        type,
                )
            },

            onRetry =
                contentListViewModel::retry,
        )
    }

    /*
     * Space Detail
     */

    composable(
        route =
            EntityDetailRoute.route,

        arguments =
            listOf(
                navArgument(
                    EntityDetailRoute.typeArg
                ) {
                    type =
                        NavType.StringType
                },

                navArgument(
                    EntityDetailRoute.idArg
                ) {
                    type =
                        NavType.StringType
                },
            ),
    ) { backStackEntry ->

        val type =
            backStackEntry
                .arguments
                ?.getString(
                    EntityDetailRoute.typeArg
                )
                .orEmpty()

        val id =
            backStackEntry
                .arguments
                ?.getString(
                    EntityDetailRoute.idArg
                )
                .orEmpty()

        if (
            type != "space"
        ) {
            return@composable
        }

        val entityViewModel:
                EntityDetailViewModel =
            viewModel(
                key =
                    "entity:$id",

                factory =
                    EntityDetailViewModelFactory(
                        id =
                            id,

                        repository =
                            dependencies
                                .entityDetailRepository,
                    ),
            )

        val state by
        entityViewModel
            .uiState
            .collectAsStateWithLifecycle()

        EntityDetailScreen(
            state =
                state,

            onContentClick = { item ->

                if (
                    !item.canOpen
                ) {
                    if (
                        item.lockReason ==
                        "member_required"
                    ) {
                        println(
                            "Membership purchase pending"
                        )
                    }

                    return@EntityDetailScreen
                }

                when (
                    item.type
                ) {
                    "collection" -> {
                        navController.navigate(
                            TopicDetailRoute.create(
                                id =
                                    item.id,

                                publishId =
                                    item.publishId,
                            )
                        )
                    }

                    "doc" -> {
                        navController.navigate(
                            DocDetailRoute.create(
                                docId =
                                    item.id,
                            )
                        )
                    }

                    "slide" -> {
                        navController.navigate(
                            SlideDetailRoute.create(
                                slideId =
                                    item.id,
                            )
                        )
                    }
                }
            },
        )
    }

    /*
     * Topic Detail
     */

    composable(
        route =
            TopicDetailRoute.route,

        arguments =
            listOf(
                navArgument(
                    TopicDetailRoute.idArg
                ) {
                    type =
                        NavType.StringType
                },

                navArgument(
                    TopicDetailRoute.publishIdArg
                ) {
                    type =
                        NavType.StringType

                    nullable =
                        true

                    defaultValue =
                        null
                },
            ),
    ) { backStackEntry ->

        val id =
            backStackEntry
                .arguments
                ?.getString(
                    TopicDetailRoute.idArg
                )
                .orEmpty()

        val topicViewModel:
                TopicDetailViewModel =
            viewModel(
                key =
                    "topic:$id",

                factory =
                    TopicDetailViewModelFactory(
                        topicId =
                            id,

                        repository =
                            dependencies
                                .topicDetailRepository,
                    ),
            )

        val state by
        topicViewModel
            .uiState
            .collectAsStateWithLifecycle()

        TopicDetailScreen(
            state =
                state,

            onSpaceClick = {
                state
                    .detail
                    ?.space
                    ?.let { space ->

                        navController.navigate(
                            EntityDetailRoute.create(
                                type =
                                    "space",

                                id =
                                    space.id,
                            )
                        )
                    }
            },

            onDocClick = { doc ->

                if (
                    !doc.canOpen
                ) {
                    println(
                        "Membership pending: ${state.detail?.space?.id}"
                    )
                } else {
                    val topicId =
                        state
                            .detail
                            ?.topic
                            ?.id

                    navController.navigate(
                        DocDetailRoute.create(
                            docId =
                                doc.id,

                            topicId =
                                topicId,
                        )
                    )
                }
            },

            onMembershipClick = {
                println(
                    "Membership pending: ${state.detail?.space?.id}"
                )
            },
        )
    }

    /*
     * Doc Detail
     */

    composable(
        route =
            DocDetailRoute.route,

        arguments =
            listOf(
                navArgument(
                    DocDetailRoute.docIdArg
                ) {
                    type =
                        NavType.StringType
                },

                navArgument(
                    DocDetailRoute.topicIdArg
                ) {
                    type =
                        NavType.StringType

                    nullable =
                        true

                    defaultValue =
                        null
                },
            ),
    ) { backStackEntry ->

        val docId =
            backStackEntry
                .arguments
                ?.getString(
                    DocDetailRoute.docIdArg
                )
                .orEmpty()

        val topicId =
            backStackEntry
                .arguments
                ?.getString(
                    DocDetailRoute.topicIdArg
                )

        val docViewModel:
                DocDetailViewModel =
            viewModel(
                key =
                    "doc:$docId:${topicId.orEmpty()}",

                factory =
                    DocDetailViewModelFactory(
                        docId =
                            docId,

                        topicId =
                            topicId,

                        repository =
                            dependencies
                                .docDetailRepository,
                    ),
            )

        val state by
        docViewModel
            .uiState
            .collectAsStateWithLifecycle()

        DocDetailScreen(
            state =
                state,

            onSpaceClick = {
                state
                    .detail
                    ?.space
                    ?.let { space ->

                        navController.navigate(
                            EntityDetailRoute.create(
                                type =
                                    "space",

                                id =
                                    space.id,
                            )
                        )
                    }
            },
        )
    }

    /*
     * Slide Detail
     */

    composable(
        route =
            SlideDetailRoute.route,

        arguments =
            listOf(
                navArgument(
                    SlideDetailRoute.slideIdArg
                ) {
                    type =
                        NavType.StringType
                },
            ),
    ) { backStackEntry ->

        val slideId =
            backStackEntry
                .arguments
                ?.getString(
                    SlideDetailRoute.slideIdArg
                )
                .orEmpty()

        val slideViewModel:
                SlideDetailViewModel =
            viewModel(
                key =
                    "slide:$slideId",

                factory =
                    SlideDetailViewModelFactory(
                        slideId =
                            slideId,

                        repository =
                            dependencies
                                .slideDetailRepository,
                    ),
            )

        val state by
        slideViewModel
            .uiState
            .collectAsStateWithLifecycle()

        SlideDetailScreen(
            state =
                state,

            onPreviousPage =
                slideViewModel::goToPreviousPage,

            onNextPage =
                slideViewModel::goToNextPage,

            onLoadQuestions =
                slideViewModel::loadQuestions,

            onSubmitQuestion = {
                    text,
                    onSuccess ->

                slideViewModel.submitQuestion(
                    text =
                        text,

                    onSuccess =
                        onSuccess,
                )
            },

            onMarkQuestionAnswered =
                slideViewModel::markQuestionAnswered,

            onHideQuestion =
                slideViewModel::hideQuestion,

            onClearQuestionSubmitError =
                slideViewModel::clearQuestionSubmitError,

            onClearQuestionActionError =
                slideViewModel::clearQuestionActionError,

            onRetry =
                slideViewModel::retry,
        )
    }
}