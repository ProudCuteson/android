package top.role.android.feature.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.remember

import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.navigation.NavType
import androidx.navigation.navArgument

import top.role.android.app.navigation.MainDestination
import top.role.android.app.navigation.SpaceListRoute
import top.role.android.app.navigation.EntityDetailRoute
import top.role.android.app.navigation.TopicDetailRoute
import top.role.android.app.navigation.DocDetailRoute

import top.role.android.feature.account.AccountScreen
import top.role.android.feature.chat.ChatScreen
import top.role.android.feature.day.DayScreen
import top.role.android.feature.discover.DiscoverScreen
import top.role.android.feature.engage.EngageScreen
import top.role.android.feature.discover.DiscoverRepository
import top.role.android.feature.discover.DiscoverViewModel
import top.role.android.feature.discover.DiscoverViewModelFactory
import top.role.android.feature.entity.EntityDetailScreen
import top.role.android.feature.entity.EntityDetailRepository
import top.role.android.feature.entity.EntityDetailViewModel
import top.role.android.feature.entity.EntityDetailViewModelFactory
import top.role.android.feature.topic.TopicDetailScreen
import top.role.android.feature.topic.TopicDetailRepository
import top.role.android.feature.topic.TopicDetailViewModel
import top.role.android.feature.topic.TopicDetailViewModelFactory
import top.role.android.feature.reading.DocDetailRepository
import top.role.android.feature.reading.DocDetailScreen
import top.role.android.feature.reading.DocDetailViewModel
import top.role.android.feature.reading.DocDetailViewModelFactory

import top.role.android.core.model.space.SpaceListMode
import top.role.android.feature.discover.space.SpaceListRepository
import top.role.android.feature.discover.space.SpaceListScreen
import top.role.android.feature.discover.space.SpaceListViewModel
import top.role.android.feature.discover.space.SpaceListViewModelFactory

private const val ACCOUNT_ROUTE = "account"

private data class MainNavigationItem(
    val destination: MainDestination,
    val icon: ImageVector,
)

private val mainNavigationItems = listOf(
    MainNavigationItem(
        destination = MainDestination.Day,
        icon = Icons.Outlined.Today,
    ),
    MainNavigationItem(
        destination = MainDestination.Discover,
        icon = Icons.Outlined.Explore,
    ),
    MainNavigationItem(
        destination = MainDestination.Chat,
        icon = Icons.Outlined.ChatBubbleOutline,
    ),
    MainNavigationItem(
        destination = MainDestination.Engage,
        icon = Icons.Outlined.TaskAlt,
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    discoverRepository: DiscoverRepository,
    spaceListRepository: SpaceListRepository,
    entityDetailRepository: EntityDetailRepository,
    topicDetailRepository: TopicDetailRepository,
    docDetailRepository: DocDetailRepository,
    onSignOut: () -> Unit,
) {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val currentSpaceListMode =
        if (
            currentRoute ==
            SpaceListRoute.route
        ) {
            SpaceListMode.fromValue(
                navBackStackEntry
                    ?.arguments
                    ?.getString(
                        SpaceListRoute.modeArg
                    )
            )
        } else {
            null
        }

    val isMainDestination = mainNavigationItems.any {
        it.destination.route == currentRoute
    }

    val discoverViewModel: DiscoverViewModel =
        viewModel(
            factory =
                DiscoverViewModelFactory(
                    repository =
                        discoverRepository,
                )
        )

    val discoverState by
    discoverViewModel.uiState
        .collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text =
                            when (currentRoute) {
                                MainDestination.Day.route ->
                                    "今日"

                                MainDestination.Discover.route ->
                                    "发现"

                                MainDestination.Chat.route ->
                                    "聊天"

                                MainDestination.Engage.route ->
                                    "参与"

                                ACCOUNT_ROUTE ->
                                    "我的"

                                SpaceListRoute.route ->
                                    when (currentSpaceListMode) {
                                        SpaceListMode.Featured ->
                                            "推荐空间"

                                        SpaceListMode.Following ->
                                            "我关注的"

                                        SpaceListMode.Latest ->
                                            "最新空间"

                                        null ->
                                            "空间"
                                    }

                                EntityDetailRoute.route ->
                                    "空间"

                                TopicDetailRoute.route ->
                                    "话题"

                                DocDetailRoute.route ->
                                    "阅读"

                                else ->
                                    "TopRole"
                            }
                    )
                },

                actions = {
                    if (
                        currentRoute ==
                        MainDestination.Day.route
                    ) {
                        IconButton(
                            onClick = {
                                navController.navigate(
                                    ACCOUNT_ROUTE
                                ) {
                                    launchSingleTop = true
                                }
                            }
                        ) {
                            Icon(
                                imageVector =
                                    Icons.Outlined.AccountCircle,
                                contentDescription =
                                    "我的",
                            )
                        }
                    }
                },

                navigationIcon = {
                    if (!isMainDestination) {
                        IconButton(
                            onClick = {
                                navController.popBackStack()
                            },
                        ) {
                            Icon(
                                imageVector =
                                    Icons.AutoMirrored.Filled
                                        .ArrowBack,
                                contentDescription =
                                    "返回",
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            if (isMainDestination) {
                NavigationBar {
                    mainNavigationItems.forEach { item ->
                        val destination = item.destination
                        val selected = currentRoute == destination.route

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(
                                        navController.graph.findStartDestination().id
                                    ) {
                                        saveState = true
                                    }

                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = destination.label,
                                )
                            },
                            label = {
                                Text(destination.label)
                            },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MainDestination.Day.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(MainDestination.Day.route) {
                DayScreen()
            }

            composable(
                route = SpaceListRoute.route,

                arguments = listOf(
                    navArgument(
                        SpaceListRoute.modeArg
                    ) {
                        type = NavType.StringType
                    },
                ),
            ) { backStackEntry ->

                val mode =
                    SpaceListMode.fromValue(
                        backStackEntry.arguments
                            ?.getString(
                                SpaceListRoute.modeArg
                            )
                    )

                val factory =
                    remember(
                        mode,
                        spaceListRepository,
                    ) {
                        SpaceListViewModelFactory(
                            mode = mode,
                            repository =
                                spaceListRepository,
                        )
                    }

                val spaceListViewModel:
                        SpaceListViewModel =
                    viewModel(
                        key =
                            "space-list:${mode.value}",

                        factory = factory,
                    )

                val state by
                spaceListViewModel.uiState
                    .collectAsStateWithLifecycle()

                SpaceListScreen(
                    state = state,

                    onSpaceClick = { item ->
                        navController.navigate(
                            EntityDetailRoute.create(
                                type = "space",
                                id = item.id,
                            )
                        )
                    },

                    onLoadMore = {
                        spaceListViewModel.loadMore()
                    },

                    onRetry = {
                        spaceListViewModel.retry()
                    },
                )
            }

            composable(
                route = EntityDetailRoute.route,
                arguments = listOf(
                    navArgument(
                        EntityDetailRoute.typeArg
                    ) {
                        type = NavType.StringType
                    },

                    navArgument(
                        EntityDetailRoute.idArg
                    ) {
                        type = NavType.StringType
                    },
                ),
            ) { backStackEntry ->

                val type =
                    backStackEntry.arguments
                        ?.getString(
                            EntityDetailRoute.typeArg
                        )
                        .orEmpty()

                val id =
                    backStackEntry.arguments
                        ?.getString(
                            EntityDetailRoute.idArg
                        )
                        .orEmpty()

                if (type == "space") {
                    val entityViewModel:
                            EntityDetailViewModel =
                        viewModel(
                            key = "entity:$id",
                            factory =
                                EntityDetailViewModelFactory(
                                    id = id,
                                    repository =
                                        entityDetailRepository,
                                )
                        )

                    val entityState by
                    entityViewModel.uiState
                        .collectAsStateWithLifecycle()

                    EntityDetailScreen(
                        state = entityState,

                        onContentClick = { item ->
                            if (!item.canOpen) {
                                if (item.lockReason == "member_required") {
                                    println(
                                        "Membership purchase pending"
                                    )
                                }

                                return@EntityDetailScreen
                            }

                            when (item.type) {
                                "collection" -> {
                                    navController.navigate(
                                        TopicDetailRoute.create(
                                            id = item.id,
                                            publishId = item.publishId,
                                        )
                                    )
                                }

                                "doc" -> {
                                    navController.navigate(
                                        DocDetailRoute.create(
                                            docId = item.id,
                                        )
                                    )
                                }

                                "slide" -> {
                                    println(
                                        "Slide detail pending: ${item.id}"
                                    )
                                }
                            }
                        },
                    )
                }
            }

            composable(
                route = TopicDetailRoute.route,
                arguments = listOf(
                    navArgument(
                        TopicDetailRoute.idArg
                    ) {
                        type = NavType.StringType
                    },

                    navArgument(
                        TopicDetailRoute.publishIdArg
                    ) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                ),
            ) { backStackEntry ->

                val id =
                    backStackEntry.arguments
                        ?.getString(
                            TopicDetailRoute.idArg
                        )
                        .orEmpty()

                val publishId =
                    backStackEntry.arguments
                        ?.getString(
                            TopicDetailRoute.publishIdArg
                        )

                val topicViewModel:
                        TopicDetailViewModel =
                    viewModel(
                        key = "topic:$id",
                        factory =
                            TopicDetailViewModelFactory(
                                topicId = id,
                                repository =
                                    topicDetailRepository,
                            )
                    )

                val topicState by
                topicViewModel.uiState
                    .collectAsStateWithLifecycle()

                TopicDetailScreen(
                    state = topicState,

                    onSpaceClick = {
                        topicState.detail
                            ?.space
                            ?.let { space ->
                                navController.navigate(
                                    EntityDetailRoute.create(
                                        type = "space",
                                        id = space.id,
                                    )
                                )
                            }
                    },

                    onDocClick = { doc ->
                        if (!doc.canOpen) {
                            println(
                                "Membership pending: ${topicState.detail?.space?.id}"
                            )
                        } else {
                            val topicId =
                                topicState.detail
                                    ?.topic
                                    ?.id

                            navController.navigate(
                                DocDetailRoute.create(
                                    docId = doc.id,
                                    topicId = topicId,
                                )
                            )
                        }
                    },

                    onMembershipClick = {
                        println(
                            "Membership pending: ${topicState.detail?.space?.id}"
                        )
                    },
                )

            }

            composable(MainDestination.Discover.route) {
                DiscoverScreen(
                    state = discoverState,

                    onEntityClick = { item ->
                        navController.navigate(
                            EntityDetailRoute.create(
                                type = "space",
                                id = item.id,
                            )
                        )
                    },

                    onTopicClick = { item ->
                        navController.navigate(
                            TopicDetailRoute.create(
                                id = item.id,
                                publishId = item.publishId,
                            )
                        )
                    },

                    onDocClick = { item ->
                        when (item.type) {
                            "doc" -> {
                                navController.navigate(
                                    DocDetailRoute.create(
                                        docId = item.id,
                                    )
                                )
                            }

                            "slide" -> {
                                println(
                                    "Slide detail pending: ${item.id}"
                                )
                            }
                        }
                    },

                    onMoreEntities = { mode ->
                        navController.navigate(
                            SpaceListRoute.create(
                                mode = mode,
                            )
                        )
                    },

                    onMoreTopics = {
                        println(
                            "Discover topics more"
                        )
                    },

                    onMoreDocs = {
                        println(
                            "Discover docs more"
                        )
                    },
                )
            }

            composable(
                route = DocDetailRoute.route,
                arguments = listOf(
                    navArgument(
                        DocDetailRoute.docIdArg
                    ) {
                        type = NavType.StringType
                    },

                    navArgument(
                        DocDetailRoute.topicIdArg
                    ) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                ),
            ) { backStackEntry ->

                val docId =
                    backStackEntry.arguments
                        ?.getString(
                            DocDetailRoute.docIdArg
                        )
                        .orEmpty()

                val topicId =
                    backStackEntry.arguments
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
                                docId = docId,
                                topicId = topicId,
                                repository =
                                    docDetailRepository,
                            )
                    )

                val docState by
                docViewModel.uiState
                    .collectAsStateWithLifecycle()

                DocDetailScreen(
                    state = docState,

                    onSpaceClick = {
                        docState.detail
                            ?.space
                            ?.let { space ->
                                navController.navigate(
                                    EntityDetailRoute.create(
                                        type = "space",
                                        id = space.id,
                                    )
                                )
                            }
                    },
                )
            }

            composable(MainDestination.Chat.route) {
                ChatScreen()
            }

            composable(MainDestination.Engage.route) {
                EngageScreen()
            }

            composable(ACCOUNT_ROUTE) {
                AccountScreen(
                    onSignOut = onSignOut,
                )
            }
        }
    }
}