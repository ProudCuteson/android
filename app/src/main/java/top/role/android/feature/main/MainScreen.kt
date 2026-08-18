package top.role.android.feature.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

import top.role.android.app.navigation.SpaceListRoute
import top.role.android.core.model.space.SpaceListMode

import top.role.android.feature.discover.DiscoverRepository
import top.role.android.feature.discover.content.ContentListRepository
import top.role.android.feature.discover.space.SpaceListRepository
import top.role.android.feature.discover.topic.TopicListRepository
import top.role.android.feature.entity.EntityDetailRepository
import top.role.android.feature.reading.DocDetailRepository
import top.role.android.feature.slide.SlideDetailRepository
import top.role.android.feature.topic.TopicDetailRepository
import top.role.android.feature.main.navigation.MainNavGraph
import androidx.compose.runtime.remember
import top.role.android.feature.main.navigation.MainNavigationDependencies
import top.role.android.core.data.note.NoteLocalRepository
import top.role.android.app.navigation.NoteLibraryRoute
import top.role.android.core.data.image.ImageRepository
import top.role.android.core.data.image.NoteImageService
import top.role.android.core.data.note.ai.NoteAiRemoteDataSource

@Composable
fun MainScreen(
    localOwnerUserId: String,
    remoteAuthenticated: Boolean,
    noteLocalRepository: NoteLocalRepository,
    noteAiRemoteDataSource: NoteAiRemoteDataSource,
    noteImageService: NoteImageService,
    imageRepository: ImageRepository,
    discoverRepository: DiscoverRepository,
    spaceListRepository: SpaceListRepository,
    topicListRepository: TopicListRepository,
    contentListRepository: ContentListRepository,
    entityDetailRepository: EntityDetailRepository,
    topicDetailRepository: TopicDetailRepository,
    docDetailRepository: DocDetailRepository,
    slideDetailRepository: SlideDetailRepository,
    onSignOut: () -> Unit,
) {
    val navController =
        rememberNavController()

    val navBackStackEntry by
    navController
        .currentBackStackEntryAsState()

    val currentRoute =
        navBackStackEntry
            ?.destination
            ?.route

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

    val shellState =
        resolveMainShellState(
            route =
                currentRoute,

            spaceListMode =
                currentSpaceListMode,
        )

    Scaffold(
        topBar = {
            if (
                shellState.showTopBar
            ) {
                MainTopBar(
                    state =
                        shellState,

                    onBack = {
                        navController
                            .popBackStack()
                    },

                    onLibrary = {
                        navController.navigate(
                            NoteLibraryRoute.route
                        ) {
                            launchSingleTop =
                                true
                        }
                    },

                    onAccount = {
                        navController.navigate(
                            ACCOUNT_ROUTE
                        ) {
                            launchSingleTop =
                                true
                        }
                    },
                )
            }
        },

        bottomBar = {
            if (
                shellState
                    .showBottomBar
            ) {
                MainBottomBar(
                    navController =
                        navController,

                    currentRoute =
                        currentRoute,
                )
            }
        },
    ) { innerPadding ->

        val navigationDependencies =
            remember(
                localOwnerUserId,
                remoteAuthenticated,
                noteLocalRepository,
                noteAiRemoteDataSource,
                noteImageService,
                imageRepository,
                discoverRepository,
                spaceListRepository,
                topicListRepository,
                contentListRepository,
                entityDetailRepository,
                topicDetailRepository,
                docDetailRepository,
                slideDetailRepository,
            ) {
                MainNavigationDependencies(
                    localOwnerUserId =
                        localOwnerUserId,

                    remoteAuthenticated =
                        remoteAuthenticated,

                    noteLocalRepository =
                        noteLocalRepository,

                    noteAiRemoteDataSource =
                        noteAiRemoteDataSource,

                    noteImageService =
                        noteImageService,

                    imageRepository =
                        imageRepository,

                    discoverRepository =
                        discoverRepository,

                    spaceListRepository =
                        spaceListRepository,

                    topicListRepository =
                        topicListRepository,

                    contentListRepository =
                        contentListRepository,

                    entityDetailRepository =
                        entityDetailRepository,

                    topicDetailRepository =
                        topicDetailRepository,

                    docDetailRepository =
                        docDetailRepository,

                    slideDetailRepository =
                        slideDetailRepository,
                )
            }

        MainNavGraph(
            navController =
                navController,

            modifier =
                Modifier.padding(
                    innerPadding
                ),

            dependencies =
                navigationDependencies,

            onSignOut =
                onSignOut,
        )

    }
}