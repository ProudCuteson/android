package top.role.android.feature.main

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding

import androidx.compose.material3.Scaffold

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

import androidx.compose.ui.Modifier

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
import top.role.android.core.preferences.AppAppearance
import top.role.android.core.preferences.WeekStart
import top.role.android.feature.account.AccountRepository
import top.role.android.feature.account.password.AccountPasswordRepository
import top.role.android.feature.chat.conversation.ChatImagePreviewRepository
import top.role.android.feature.credit.CreditsRepository
import top.role.android.feature.market.order.MarketOrderListRepository
import top.role.android.feature.market.order.detail.MarketOrderDetailRepository

@Composable
fun MainScreen(
    localOwnerUserId: String,
    remoteAuthenticated: Boolean,
    remoteOnline: Boolean,

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
    chatImagePreviewRepository: ChatImagePreviewRepository,
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
    marketOrderDetailRepository: MarketOrderDetailRepository,

    membershipRepository: MembershipRepository,
    wechatPayLauncher: WechatPayLauncher,

    accountRepository: AccountRepository,
    accountPasswordRepository: AccountPasswordRepository,
    creditsRepository: CreditsRepository,

    appearance: AppAppearance,
    weekStart: WeekStart,
    onAppearanceChange: (AppAppearance) -> Unit,
    onWeekStartChange: (WeekStart) -> Unit,

    onReSignIn: () -> Unit,
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
            remoteOnline,

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
            chatImagePreviewRepository,
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
            marketOrderDetailRepository,

            membershipRepository,
            wechatPayLauncher,

            accountRepository,
            accountPasswordRepository,
            creditsRepository,

        ) {
            MainNavigationDependencies(
                localOwnerUserId = localOwnerUserId,
                remoteAuthenticated = remoteAuthenticated,
                remoteOnline = remoteOnline,

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
                chatImagePreviewRepository = chatImagePreviewRepository,
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
                marketOrderDetailRepository = marketOrderDetailRepository,

                membershipRepository = membershipRepository,
                wechatPayLauncher = wechatPayLauncher,

                accountRepository = accountRepository,
                accountPasswordRepository = accountPasswordRepository,
                creditsRepository = creditsRepository,
            )
        }

    Scaffold(
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
                    state = shellState,
                    onBack = { navController.popBackStack() },
                    onLibrary = {
                        navController.navigate(NoteLibraryRoute.route) {
                            launchSingleTop = true
                        }
                    },
                    onAccount = {
                        navController.navigate(AccountRoute.route) {
                            launchSingleTop = true
                        }
                    },
                    onFriends = {
                        navController.navigate(ChatFriendsRoute.route) {
                            launchSingleTop = true
                        }
                    },
                    onAddFriend = {
                        navController.navigate(ChatFriendLookupRoute.route) {
                            launchSingleTop = true
                        }
                    },
                    onCreateGroup = {
                        navController.navigate(ChatGroupCreateRoute.route) {
                            launchSingleTop = true
                        }
                    },
                    onMarketOrders = {
                        navController.navigate(MarketOrderListRoute.route)
                    },
                    onRichText = {
                        navController.navigate("note/rich-text-playground")
                    },
                )
            }
        },

        bottomBar = {
            if (
                shellState.showBottomBar
            ) {
                MainBottomBar(
                    navController = navController,
                    currentRoute = currentRoute,
                )
            }
        },
    ) { innerPadding ->

        MainNavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            dependencies = navigationDependencies,
            appearance = appearance,
            weekStart = weekStart,
            onAppearanceChange = onAppearanceChange,
            onWeekStartChange = onWeekStartChange,
            onReSignIn = onReSignIn,
            onSignOut = onSignOut,
        )
    }
}