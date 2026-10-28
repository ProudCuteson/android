package top.role.android.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlin.time.Duration.Companion.milliseconds
import top.role.android.core.auth.AuthState
import top.role.android.feature.auth.SignInScreen
import top.role.android.feature.auth.SignInViewModel
import top.role.android.feature.auth.SignInViewModelFactory
import top.role.android.feature.main.MainScreen
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import top.role.android.app.navigation.InAppBrowserRoute
import top.role.android.feature.main.navigation.browserNavGraph

@OptIn(FlowPreview::class)
@Composable
fun AppRoot(
    appContainer: AppContainer,
    appPreferencesViewModel: AppPreferencesViewModel
) {

    val sessionViewModel: AppSessionViewModel = viewModel(
        factory = AppSessionViewModelFactory(
            sessionStorage = appContainer.sessionStorage,
            convexClient = appContainer.convexClient,
        )
    )

    val authState by sessionViewModel
        .authState
        .collectAsStateWithLifecycle()

    val localOwnerUserId by  sessionViewModel
        .localOwnerUserId
        .collectAsStateWithLifecycle()

    var showRemoteSignIn by rememberSaveable {
        mutableStateOf(false)
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val appearance by appPreferencesViewModel
        .appearance
        .collectAsStateWithLifecycle()

    val weekStart by appPreferencesViewModel
        .weekStart
        .collectAsStateWithLifecycle()

    val hasAcceptedAuthAgreement by appPreferencesViewModel
        .hasAcceptedAuthAgreement
        .collectAsStateWithLifecycle()

    val isOnline by appContainer
        .networkMonitor
        .isOnline
        .collectAsStateWithLifecycle( initialValue = false )

    val syncOwnerUserId = (authState as? AuthState.SignedIn)
        ?.session
        ?.userId
        ?.takeIf { userId ->
            userId == localOwnerUserId
        }

    LaunchedEffect(
        syncOwnerUserId,
        isOnline,
    ) {
        val ownerUserId =
            syncOwnerUserId
                ?: return@LaunchedEffect

        if (!isOnline) {
            return@LaunchedEffect
        }

        appContainer
            .noteSyncCoordinator
            .sync(
                ownerUserId = ownerUserId,
            )
            .onSuccess { result ->
                android.util.Log.d(
                    "NoteSync",
                    "initial/reconnect sync: " +
                            "push=${result.pushSucceededCount}/" +
                            "${result.pushAttemptedCount}, " +
                            "failed=${result.pushFailedCount}",
                )
            }
            .onFailure { error ->
                android.util.Log.e(
                    "NoteSync",
                    "initial/reconnect sync failed",
                    error,
                )
            }
    }

    LaunchedEffect(
        syncOwnerUserId,
        isOnline,
    ) {
        val ownerUserId = syncOwnerUserId?: return@LaunchedEffect

        if (!isOnline) {
            return@LaunchedEffect
        }

        appContainer
            .noteLocalRepository
            .syncRequests
            .filter {it == ownerUserId}
            .debounce(1_500.milliseconds)
            .collect {
                appContainer
                    .noteSyncCoordinator
                    .sync(ownerUserId = ownerUserId)
                    .onSuccess { result ->
                        android.util.Log.d(
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
                        android.util.Log.e(
                            "NoteSync",
                            "continuous sync failed",
                            error,
                        )
                    }
            }
    }

    if (
        authState == AuthState.Loading &&
        localOwnerUserId == null
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (localOwnerUserId == null) {
        SignedOutContent(
            appContainer = appContainer,
            hasAcceptedAuthAgreement = hasAcceptedAuthAgreement,
            onAcceptAuthAgreement = appPreferencesViewModel::acceptAuthAgreement,
        )

        return
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(
        lifecycleOwner,
        sessionViewModel,
        appContainer,
    ) {
        val observer =
            LifecycleEventObserver { _, event ->

                if (
                    event == Lifecycle.Event.ON_START
                ) {
                    scope.launch {

                        val authenticatedUserId =
                            sessionViewModel
                                .checkSessionOnForeground(
                                    context = context
                                )

                        val ownerUserId =
                            sessionViewModel
                                .localOwnerUserId.value

                        if (
                            authenticatedUserId != null &&
                            authenticatedUserId == ownerUserId
                        ) {
                            appContainer
                                .noteLocalRepository
                                .requestSync(
                                    ownerUserId =
                                        authenticatedUserId,
                                )
                        }
                    }
                }
            }

        lifecycleOwner.lifecycle
            .addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle
                .removeObserver(observer)
        }
    }

    LaunchedEffect(authState) {
        if (
            showRemoteSignIn &&
            authState is AuthState.SignedIn
        ) {
            showRemoteSignIn = false
        }
    }

    if (showRemoteSignIn) {
        SignedOutContent(
            appContainer = appContainer,
            hasAcceptedAuthAgreement = hasAcceptedAuthAgreement,
            onAcceptAuthAgreement = appPreferencesViewModel::acceptAuthAgreement,
        )
    } else {

        MainScreen(
            localOwnerUserId = localOwnerUserId!!,
            remoteAuthenticated = authState is AuthState.SignedIn,
            remoteOnline = isOnline,

            noteLocalRepository = appContainer.noteLocalRepository,
            noteAiRemoteDataSource = appContainer.noteAiRemoteDataSource,
            noteImageService = appContainer.noteImageService,
            imageRepository = appContainer.imageRepository,

            discoverRepository = appContainer.discoverRepository,
            spaceListRepository = appContainer.spaceListRepository,
            topicListRepository = appContainer.topicListRepository,
            contentListRepository = appContainer.contentListRepository,
            entityDetailRepository = appContainer.entityDetailRepository,
            topicDetailRepository = appContainer.topicDetailRepository,
            docDetailRepository = appContainer.docDetailRepository,
            slideDetailRepository = appContainer.slideDetailRepository,

            chatConversationListRepository = appContainer.chatConversationListRepository,
            chatConversationRepository = appContainer.chatConversationRepository,
            chatImagePreviewRepository = appContainer.chatImagePreviewRepository,
            chatFriendsRepository = appContainer.chatFriendsRepository,
            chatGroupRepository = appContainer.chatGroupRepository,

            engageRepository = appContainer.engageRepository,
            surveyListRepository = appContainer.surveyListRepository,
            surveyDetailRepository = appContainer.surveyDetailRepository,
            bountyListRepository = appContainer.bountyListRepository,
            bountyDetailRepository = appContainer.bountyDetailRepository,

            marketListRepository = appContainer.marketListRepository,
            marketDetailRepository = appContainer.marketDetailRepository,
            marketOrderListRepository = appContainer.marketOrderListRepository,
            marketOrderDetailRepository = appContainer.marketOrderDetailRepository,

            membershipRepository = appContainer.membershipRepository,
            wechatPayLauncher = appContainer.wechatPayLauncher,
            accountRepository = appContainer.accountRepository,
            accountPasswordRepository = appContainer.accountPasswordRepository,

            appearance = appearance,
            weekStart = weekStart,
            onAppearanceChange = appPreferencesViewModel::setAppearance,
            onWeekStartChange = appPreferencesViewModel::setWeekStart,
            creditsRepository = appContainer.creditsRepository,

            onReSignIn = {
                sessionViewModel.signOut(
                    context = context,
                    onComplete = {showRemoteSignIn = true},
                )
            },

            onSignOut = {
                sessionViewModel.signOut(context = context)
            },
        )
    }

}

@Composable
private fun SignedOutContent(
    appContainer: AppContainer,
    hasAcceptedAuthAgreement: Boolean,
    onAcceptAuthAgreement: () -> Unit,
) {
    val signInViewModel: SignInViewModel = viewModel(
        factory = SignInViewModelFactory(
            authRepository = appContainer.authRepository,
            sessionStorage = appContainer.sessionStorage,
        )
    )

    val state by signInViewModel.uiState
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
                        )
                    )
                },
                onOpenPrivacy = {
                    navController.navigate(
                        InAppBrowserRoute.create(
                            title = "隐私政策",
                            url = "https://role.top/home/privacy",
                        )
                    )
                },
            )
        }

        // 这里注册现有 InAppBrowserScreen
        browserNavGraph(
            navController = navController,
        )
    }
}