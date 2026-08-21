package top.role.android.feature.main

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding

import androidx.compose.material3.Scaffold

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

import androidx.compose.ui.Modifier
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

import top.role.android.app.navigation.AccountRoute

import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

import top.role.android.app.navigation.ChatFriendLookupRoute
import top.role.android.app.navigation.ChatFriendsRoute
import top.role.android.app.navigation.ChatGroupCreateRoute
import top.role.android.app.navigation.MarketOrderListRoute
import top.role.android.app.navigation.NoteLibraryRoute
import top.role.android.app.navigation.SpaceListRoute

import top.role.android.core.data.image.ImageRepository
import top.role.android.core.data.image.NoteImageService
import top.role.android.core.data.note.NoteLocalRepository
import top.role.android.core.data.note.ai.NoteAiRemoteDataSource
import top.role.android.core.model.space.SpaceListMode

import top.role.android.feature.chat.conversation.ChatConversationRepository
import top.role.android.feature.chat.conversationlist.ChatConversationListRepository
import top.role.android.feature.chat.friends.ChatFriendsRepository
import top.role.android.feature.chat.group.ChatGroupRepository

import top.role.android.feature.discover.DiscoverRepository
import top.role.android.feature.discover.content.ContentListRepository
import top.role.android.feature.discover.space.SpaceListRepository
import top.role.android.feature.discover.topic.TopicListRepository

import top.role.android.feature.entity.EntityDetailRepository

import top.role.android.feature.main.navigation.MainNavGraph
import top.role.android.feature.main.navigation.MainNavigationDependencies

import top.role.android.feature.reading.DocDetailRepository
import top.role.android.feature.slide.SlideDetailRepository
import top.role.android.feature.topic.TopicDetailRepository

import top.role.android.feature.engage.EngageRepository
import top.role.android.feature.survey.SurveyListRepository
import top.role.android.feature.survey.detail.SurveyDetailRepository
import top.role.android.feature.bounty.BountyListRepository
import top.role.android.feature.bounty.detail.BountyDetailRepository
import top.role.android.feature.market.MarketListRepository
import top.role.android.feature.market.detail.MarketDetailRepository
import top.role.android.feature.membership.MembershipRepository
import top.role.android.core.payment.WechatPayLauncher
import top.role.android.feature.account.AccountRepository
import top.role.android.feature.market.order.MarketOrderListRepository

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

    chatConversationListRepository: ChatConversationListRepository,
    chatConversationRepository: ChatConversationRepository,
    chatFriendsRepository: ChatFriendsRepository,
    chatGroupRepository: ChatGroupRepository,

    engageRepository: EngageRepository,
    surveyListRepository: SurveyListRepository,
    surveyDetailRepository: SurveyDetailRepository,
    bountyListRepository: BountyListRepository,
    bountyDetailRepository: BountyDetailRepository,
    marketListRepository: MarketListRepository,
    marketDetailRepository: MarketDetailRepository,
    marketOrderListRepository: MarketOrderListRepository,

    membershipRepository: MembershipRepository,
    wechatPayLauncher: WechatPayLauncher,

    accountRepository: AccountRepository,

    onReSignIn:
        () -> Unit,

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

            chatConversationListRepository,
            chatConversationRepository,
            chatFriendsRepository,
            chatGroupRepository,

            engageRepository,
            surveyListRepository,
            surveyDetailRepository,
            bountyListRepository,
            bountyDetailRepository,
            marketListRepository,
            marketDetailRepository,
            marketOrderListRepository,

            membershipRepository,
            wechatPayLauncher,

            accountRepository,

        ) {
            MainNavigationDependencies(
                localOwnerUserId = localOwnerUserId,
                remoteAuthenticated = remoteAuthenticated,

                noteLocalRepository = noteLocalRepository,
                noteAiRemoteDataSource = noteAiRemoteDataSource,
                noteImageService = noteImageService,
                imageRepository = imageRepository,

                discoverRepository = discoverRepository,
                spaceListRepository = spaceListRepository,
                topicListRepository = topicListRepository,
                contentListRepository = contentListRepository,
                entityDetailRepository = entityDetailRepository,
                topicDetailRepository = topicDetailRepository,
                docDetailRepository = docDetailRepository,
                slideDetailRepository = slideDetailRepository,

                chatConversationListRepository = chatConversationListRepository,
                chatConversationRepository = chatConversationRepository,
                chatFriendsRepository = chatFriendsRepository,
                chatGroupRepository = chatGroupRepository,

                engageRepository = engageRepository,
                surveyListRepository = surveyListRepository,
                surveyDetailRepository = surveyDetailRepository,
                bountyListRepository = bountyListRepository,
                bountyDetailRepository = bountyDetailRepository,
                marketListRepository = marketListRepository,
                marketDetailRepository = marketDetailRepository,
                marketOrderListRepository = marketOrderListRepository,

                membershipRepository = membershipRepository,
                wechatPayLauncher = wechatPayLauncher,

                accountRepository = accountRepository,
            )
        }

    Scaffold(
        /*
         * MainScreen 自己不统一消费 system insets。
         *
         * - MainTopBar / NavigationBar 自己处理系统区域
         * - Conversation / Note 等 page-owned 页面
         *   自己处理 TopAppBar / navigationBars / IME
         */
        contentWindowInsets =
            WindowInsets(
                left = 0,
                top = 0,
                right = 0,
                bottom = 0,
            ),

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
                            AccountRoute.route
                        ) {
                            launchSingleTop =
                                true
                        }
                    },

                    onFriends = {
                        navController.navigate(
                            ChatFriendsRoute.route
                        ) {
                            launchSingleTop =
                                true
                        }
                    },

                    onAddFriend = {
                        navController.navigate(
                            ChatFriendLookupRoute.route
                        ) {
                            launchSingleTop =
                                true
                        }
                    },

                    onCreateGroup = {
                        navController.navigate(
                            ChatGroupCreateRoute.route
                        ) {
                            launchSingleTop =
                                true
                        }
                    },

                    onMarketOrders = {
                        navController.navigate(
                            MarketOrderListRoute.route
                        )
                    },
                )
            }
        },

        bottomBar = {
            if (
                shellState.showBottomBar
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

        MainNavGraph(
            navController =
                navController,

            modifier =
                Modifier.padding(
                    innerPadding
                ),

            dependencies =
                navigationDependencies,

            onReSignIn =
                onReSignIn,

            onSignOut =
                onSignOut,
        )
    }
}