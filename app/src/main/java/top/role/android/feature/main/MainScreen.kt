package top.role.android.feature.main

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.map

import top.role.android.app.navigation.AccountRoute
import top.role.android.app.navigation.ChatFriendLookupRoute
import top.role.android.app.navigation.ChatFriendsRoute
import top.role.android.app.navigation.ChatGroupCreateRoute
import top.role.android.app.navigation.MarketOrderListRoute
import top.role.android.app.navigation.NoteLibraryRoute
import top.role.android.app.navigation.SpaceListRoute
import top.role.android.app.navigation.SettingsRoute
import top.role.android.app.navigation.NoteAdoptionRoute

import top.role.android.core.auth.AuthState
import top.role.android.core.payment.WechatPayLauncher
import top.role.android.core.preferences.AppAppearance
import top.role.android.core.preferences.WeekStart
import top.role.android.core.data.image.ImageRepository
import top.role.android.core.data.image.NoteImageService
import top.role.android.core.data.note.NoteAdoptionLocalRepository
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

import top.role.android.feature.account.AccountRepository
import top.role.android.feature.account.password.AccountPasswordRepository
import top.role.android.feature.chat.conversation.ChatImagePreviewRepository
import top.role.android.feature.credit.CreditsRepository
import top.role.android.feature.market.order.MarketOrderListRepository
import top.role.android.feature.market.order.detail.MarketOrderDetailRepository
import top.role.android.feature.banner.BannerRepository
import top.role.android.feature.browse.pdf.PdfDocumentLoader
import top.role.android.feature.content.picker.ContentPickerRepository
import top.role.android.feature.entity.service.EntityServiceRepository

@Composable
fun MainScreen(
    localOwnerUserId: String,
    authState: AuthState,
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
    noteAdoptionLocalRepository: NoteAdoptionLocalRepository,
    bannerRepository: BannerRepository,
    entityServiceRepository: EntityServiceRepository,
    contentPickerRepository: ContentPickerRepository,

    pdfDocumentLoader: PdfDocumentLoader,

    appearance: AppAppearance,
    weekStart: WeekStart,
    onAppearanceChange: (AppAppearance) -> Unit,
    onWeekStartChange: (WeekStart) -> Unit,

    onRestoreAuth: suspend () -> Boolean,
    onLogin: () -> Unit,
    onRetryAuth: () -> Unit,
    onReSignIn: () -> Unit,
    onSignOut: () -> Unit,
) {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val currentRoute = navBackStackEntry
        ?.destination
        ?.route

    val currentSpaceListMode =
        if (currentRoute == SpaceListRoute.route) {
            SpaceListMode.fromValue(
                navBackStackEntry
                    ?.arguments
                    ?.getString(SpaceListRoute.modeArg)
            )
        } else {
            null
        }

    val adoptionCount by remember(
        noteAdoptionLocalRepository,
        localOwnerUserId,
    ) {
        noteAdoptionLocalRepository
            .observeUnclaimedNotes(localOwnerUserId)
            .map { it.size }
    }.collectAsStateWithLifecycle(initialValue = 0)

    val baseShellState = resolveMainShellState(
        route = currentRoute,
        spaceListMode = currentSpaceListMode,
    )

    val shellState = baseShellState.copy(
        showAdoption =
            baseShellState.showAdoption &&
                    authState is AuthState.SignedIn &&
                    adoptionCount > 0,
    )

    val navigationDependencies =
        remember(
            localOwnerUserId,
            authState,
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

            noteAdoptionLocalRepository,
            bannerRepository,

            entityServiceRepository,
            contentPickerRepository,

        ) {
            MainNavigationDependencies(
                localOwnerUserId = localOwnerUserId,
                authState = authState,
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

                noteAdoptionLocalRepository = noteAdoptionLocalRepository,
                bannerRepository = bannerRepository,
                entityServiceRepository = entityServiceRepository,
                contentPickerRepository = contentPickerRepository,
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
                    onAdoption = {
                        navController.navigate(NoteAdoptionRoute.route) {
                            launchSingleTop = true
                        }
                    },
                    adoptionCount = adoptionCount,
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
                    onSettings = {
                        navController.navigate(SettingsRoute.route) {
                            launchSingleTop = true
                        }
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
            pdfDocumentLoader = pdfDocumentLoader,
            dependencies = navigationDependencies,
            appearance = appearance,
            weekStart = weekStart,
            onAppearanceChange = onAppearanceChange,
            onWeekStartChange = onWeekStartChange,
            onLogin = onLogin,
            onRetryAuth = onRetryAuth,
            onRestoreAuth = onRestoreAuth,
            onReSignIn = onReSignIn,
            onSignOut = onSignOut,
        )
    }
}