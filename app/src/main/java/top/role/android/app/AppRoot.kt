package top.role.android.app

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import top.role.android.core.auth.AuthState
import top.role.android.app.navigation.InAppBrowserRoute
import top.role.android.core.data.note.NoteAdoptionLocalRepository
import top.role.android.feature.auth.SignInScreen
import top.role.android.feature.auth.SignInViewModel
import top.role.android.feature.auth.SignInViewModelFactory
import top.role.android.feature.banner.BannerRepository
import top.role.android.feature.main.MainScreen
import top.role.android.feature.main.navigation.browserNavGraph

@OptIn(FlowPreview::class)
@Composable
fun AppRoot(
    appContainer: AppContainer,
    appPreferencesViewModel: AppPreferencesViewModel,
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // MARK: Session

    val sessionViewModel: AppSessionViewModel = viewModel(
        factory = AppSessionViewModelFactory(
            context = context.applicationContext,
            sessionStorage = appContainer.sessionStorage,
            convexClient = appContainer.convexClient,
            authRepository = appContainer.authRepository,
        ),
    )

    val authState by sessionViewModel
        .authState
        .collectAsStateWithLifecycle()

    val localOwnerUserId by sessionViewModel
        .localOwnerUserId
        .collectAsStateWithLifecycle()

    var showRemoteSignIn by rememberSaveable {
        mutableStateOf(false)
    }

    // MARK: Preferences

    val appearance by appPreferencesViewModel
        .appearance
        .collectAsStateWithLifecycle()

    val weekStart by appPreferencesViewModel
        .weekStart
        .collectAsStateWithLifecycle()

    val hasAcceptedAuthAgreement by appPreferencesViewModel
        .hasAcceptedAuthAgreement
        .collectAsStateWithLifecycle()

    // MARK: Network

    val isOnline by appContainer
        .networkMonitor
        .isOnline
        .collectAsStateWithLifecycle(
            initialValue = false,
        )

    // MARK: Remote Identity

    val remoteUserId = (authState as? AuthState.SignedIn)?.userId

    // MARK: Local Identity Initialization

    if (localOwnerUserId == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }

        return
    }

    // 从这里开始，本地身份已经确定为非空 String。
    val resolvedLocalOwnerUserId =
        requireNotNull(localOwnerUserId)

    // MARK: Initial / Reconnect Sync

    LaunchedEffect(
        resolvedLocalOwnerUserId,
        remoteUserId,
        isOnline,
    ) {

        if (!isOnline) {
            return@LaunchedEffect
        }

        if (remoteUserId == null) {
            return@LaunchedEffect
        }

        appContainer.noteSyncCoordinator
            .sync(
                localOwnerUserId = resolvedLocalOwnerUserId,
            )
            .onSuccess { result ->
                Log.d(
                    "NoteSync",
                    "initial/reconnect sync: " +
                            "push=${result.pushSucceededCount}/" +
                            "${result.pushAttemptedCount}, " +
                            "failed=${result.pushFailedCount}",
                )
            }
            .onFailure { error ->
                Log.e(
                    "NoteSync",
                    "initial/reconnect sync failed",
                    error,
                )
            }
    }

    // MARK: Continuous Sync Requests

    LaunchedEffect(
        resolvedLocalOwnerUserId,
        remoteUserId,
        isOnline,
    ) {

        if (!isOnline) {
            return@LaunchedEffect
        }

        if (remoteUserId == null) {
            return@LaunchedEffect
        }

        appContainer
            .noteLocalRepository
            .syncRequests
            .filter {
                it == resolvedLocalOwnerUserId
            }
            .debounce(1_500.milliseconds)
            .collect {

                appContainer
                    .noteSyncCoordinator
                    .sync(
                        localOwnerUserId = resolvedLocalOwnerUserId,
                    )
                    .onSuccess { result ->
                        Log.d(
                            "NoteSync",
                            "continuous sync: " +
                                    "pull1=${result.firstPullMergedCount}, " +
                                    "push=${result.pushSucceededCount}/" +
                                    "${result.pushAttemptedCount}, " +
                                    "failed=${result.pushFailedCount}, " +
                                    "pull2=${result.finalPullMergedCount}",
                        )
                    }
                    .onFailure { error ->
                        Log.e(
                            "NoteSync",
                            "continuous sync failed",
                            error,
                        )
                    }
            }
    }

    // MARK: Foreground Session Check

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(
        lifecycleOwner,
        sessionViewModel,
        appContainer,
    ) {

        val observer = LifecycleEventObserver { _, event ->

            if (event == Lifecycle.Event.ON_START) {

                scope.launch {

                    val authenticatedUserId =
                        sessionViewModel
                            .checkSessionOnForeground()

                    val currentLocalOwnerUserId =
                        sessionViewModel
                            .localOwnerUserId
                            .value

                    if (
                        authenticatedUserId != null &&
                        currentLocalOwnerUserId != null
                    ) {
                        appContainer
                            .noteLocalRepository
                            .requestSync(
                                localOwnerUserId =
                                    currentLocalOwnerUserId,
                            )
                    }
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // MARK: Re-sign In State

    LaunchedEffect(authState) {

        if (
            showRemoteSignIn &&
            authState is AuthState.SignedIn
        ) {
            showRemoteSignIn = false
        }
    }

    // MARK: Main Content

    if (showRemoteSignIn) {

        SignedOutContent(
            appContainer = appContainer,
            hasAcceptedAuthAgreement = hasAcceptedAuthAgreement,
            onAcceptAuthAgreement = appPreferencesViewModel::acceptAuthAgreement,
            onContinueToDay = {
                showRemoteSignIn = false
            },
        )

    } else {

        MainScreen(
            // Identity
            localOwnerUserId = resolvedLocalOwnerUserId,
            authState = authState,
            remoteOnline = isOnline,
            // Note
            noteLocalRepository = appContainer.noteLocalRepository,
            noteAiRemoteDataSource = appContainer.noteAiRemoteDataSource,
            noteImageService = appContainer.noteImageService,
            imageRepository = appContainer.imageRepository,
            // Discover
            discoverRepository = appContainer.discoverRepository,
            spaceListRepository = appContainer.spaceListRepository,
            topicListRepository = appContainer.topicListRepository,
            contentListRepository = appContainer.contentListRepository,
            entityDetailRepository = appContainer.entityDetailRepository,
            topicDetailRepository = appContainer.topicDetailRepository,
            docDetailRepository = appContainer.docDetailRepository,
            slideDetailRepository = appContainer.slideDetailRepository,
            // Chat
            chatConversationListRepository = appContainer.chatConversationListRepository,
            chatConversationRepository = appContainer.chatConversationRepository,
            chatImagePreviewRepository = appContainer.chatImagePreviewRepository,
            chatFriendsRepository = appContainer.chatFriendsRepository,
            chatGroupRepository = appContainer.chatGroupRepository,
            // Engage
            engageRepository = appContainer.engageRepository,
            surveyListRepository = appContainer.surveyListRepository,
            surveyDetailRepository = appContainer.surveyDetailRepository,
            bountyListRepository = appContainer.bountyListRepository,
            bountyDetailRepository = appContainer.bountyDetailRepository,
            // Market
            marketListRepository = appContainer.marketListRepository,
            marketDetailRepository = appContainer.marketDetailRepository,
            marketOrderListRepository = appContainer.marketOrderListRepository,
            marketOrderDetailRepository = appContainer.marketOrderDetailRepository,
            // Account / Membership
            membershipRepository = appContainer.membershipRepository,
            wechatPayLauncher = appContainer.wechatPayLauncher,
            accountRepository = appContainer.accountRepository,
            accountPasswordRepository = appContainer.accountPasswordRepository,
            // Preferences
            appearance = appearance,
            weekStart = weekStart,
            onAppearanceChange = appPreferencesViewModel::setAppearance,
            onWeekStartChange = appPreferencesViewModel::setWeekStart,
            creditsRepository = appContainer.creditsRepository,

            noteAdoptionLocalRepository = appContainer.noteAdoptionLocalRepository,
            bannerRepository = appContainer.bannerRepository,

            // Authentication Actions
            onLogin = {
                showRemoteSignIn = true
            },

            onRetryAuth = {
                scope.launch {
                    sessionViewModel.checkSessionOnForeground()
                }
            },

            onReSignIn = {
                sessionViewModel.signOut(
                    onComplete = {
                        showRemoteSignIn = true
                    },
                )
            },

            onSignOut = {
                sessionViewModel.signOut()
            },
        )
    }
}


// MARK: Signed Out Content

@Composable
private fun SignedOutContent(
    appContainer: AppContainer,
    hasAcceptedAuthAgreement: Boolean,
    onAcceptAuthAgreement: () -> Unit,
    onContinueToDay: () -> Unit,
) {

    val signInViewModel: SignInViewModel = viewModel(
        factory = SignInViewModelFactory(
            authRepository = appContainer.authRepository,
            sessionStorage = appContainer.sessionStorage,
        ),
    )

    val state by signInViewModel
        .uiState
        .collectAsStateWithLifecycle()

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "sign_in",
    ) {

        composable(
            route = "sign_in",
        ) {

            SignInScreen(
                state = state,
                hasAcceptedAgreement = hasAcceptedAuthAgreement,
                onAcceptAgreement = onAcceptAuthAgreement,
                onPhoneChange = signInViewModel::updatePhone,
                onCodeChange = signInViewModel::updateCode,
                onPasswordChange = signInViewModel::updatePassword,
                onSendOtp = signInViewModel::sendOtp,
                onSignIn = signInViewModel::verifyAndSignIn,
                onPasswordSignIn = signInViewModel::signInWithPassword,
                onBackToPhone = signInViewModel::backToPhone,

                onOpenTerms = {
                    navController.navigate(
                        InAppBrowserRoute.create(
                            title = "用户协议",
                            url = "https://role.top/home/term",
                        ),
                    )
                },

                onOpenPrivacy = {
                    navController.navigate(
                        InAppBrowserRoute.create(
                            title = "隐私政策",
                            url = "https://role.top/home/privacy",
                        ),
                    )
                },
                onContinueToDay = onContinueToDay,
            )
        }

        // 现有 InAppBrowser 导航
        browserNavGraph(
            navController = navController,
        )
    }
}
