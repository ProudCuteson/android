package top.role.android.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlin.time.Duration.Companion.milliseconds
import top.role.android.core.auth.AuthState
import top.role.android.feature.auth.SignInScreen
import top.role.android.feature.auth.SignInViewModel
import top.role.android.feature.auth.SignInViewModelFactory
import top.role.android.feature.main.MainScreen


@OptIn(FlowPreview::class)
@Composable
fun AppRoot(
    appContainer: AppContainer,
) {
    val sessionViewModel: AppSessionViewModel =
        viewModel(
            factory =
                AppSessionViewModelFactory(
                    sessionStorage =
                        appContainer
                            .sessionStorage,

                    convexClient =
                        appContainer
                            .convexClient,
                )
        )

    val authState by
    sessionViewModel
        .authState
        .collectAsStateWithLifecycle()

    val localOwnerUserId by
    sessionViewModel
        .localOwnerUserId
        .collectAsStateWithLifecycle()

    LaunchedEffect(
        authState,
        localOwnerUserId,
    ) {
        val ownerUserId =
            localOwnerUserId
                ?: return@LaunchedEffect

        if (
            authState !is
                    AuthState.SignedIn
        ) {
            return@LaunchedEffect
        }

        appContainer
            .noteSyncCoordinator
            .sync(
                ownerUserId =
                    ownerUserId,
            )
            .onFailure {
                android.util.Log.e(
                    "NoteSync",
                    "initial sync failed",
                    it,
                )
            }
    }

    LaunchedEffect(
        authState,
        localOwnerUserId,
    ) {
        val ownerUserId =
            localOwnerUserId
                ?: return@LaunchedEffect

        if (
            authState !is
                    AuthState.SignedIn
        ) {
            return@LaunchedEffect
        }

        appContainer
            .noteLocalRepository
            .syncRequests
            .filter {
                it == ownerUserId
            }
            .debounce(
                1_500.milliseconds
            )
            .collect {
                appContainer
                    .noteSyncCoordinator
                    .sync(
                        ownerUserId =
                            ownerUserId,
                    )
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
                    .onFailure {
                        android.util.Log.e(
                            "NoteSync",
                            "continuous sync failed",
                            it,
                        )
                    }
            }
    }

    if (
        authState == AuthState.Loading &&
        localOwnerUserId == null
    ) {
        Box(
            modifier =
                Modifier.fillMaxSize(),

            contentAlignment =
                Alignment.Center,
        ) {
            CircularProgressIndicator()
        }

        return
    }

    if (localOwnerUserId == null) {
        SignedOutContent(
            appContainer =
                appContainer,
        )

        return
    }

    val lifecycleOwner =
        LocalLifecycleOwner.current

    DisposableEffect(
        lifecycleOwner,
        authState,
        localOwnerUserId,
    ) {
        val observer =
            LifecycleEventObserver {
                    _,
                    event ->

                if (
                    event ==
                    Lifecycle.Event.ON_START
                ) {
                    val ownerUserId =
                        localOwnerUserId

                    if (
                        ownerUserId != null &&
                        authState is
                                AuthState.SignedIn
                    ) {
                        appContainer
                            .noteLocalRepository
                            .requestSync(
                                ownerUserId =
                                    ownerUserId,
                            )
                    }
                }
            }

        lifecycleOwner.lifecycle
            .addObserver(
                observer,
            )

        onDispose {
            lifecycleOwner.lifecycle
                .removeObserver(
                    observer,
                )
        }
    }

    MainScreen(
        localOwnerUserId =
            localOwnerUserId!!,

        remoteAuthenticated =
            authState is
                    AuthState.SignedIn,

        noteLocalRepository =
            appContainer
                .noteLocalRepository,

        noteAiRemoteDataSource =
            appContainer
                .noteAiRemoteDataSource,

        noteImageService =
            appContainer
                .noteImageService,

        imageRepository =
            appContainer
                .imageRepository,

        discoverRepository =
            appContainer
                .discoverRepository,

        spaceListRepository =
            appContainer
                .spaceListRepository,

        topicListRepository =
            appContainer
                .topicListRepository,

        contentListRepository =
            appContainer
                .contentListRepository,

        entityDetailRepository =
            appContainer
                .entityDetailRepository,

        topicDetailRepository =
            appContainer
                .topicDetailRepository,

        docDetailRepository =
            appContainer
                .docDetailRepository,

        slideDetailRepository =
            appContainer
                .slideDetailRepository,

        chatConversationListRepository =
            appContainer
                .chatConversationListRepository,

        chatConversationRepository =
            appContainer
                .chatConversationRepository,

        chatFriendsRepository =
            appContainer
                .chatFriendsRepository,

        chatGroupRepository =
            appContainer
                .chatGroupRepository,

        onSignOut =
            sessionViewModel::signOut,
    )
}

@Composable
private fun SignedOutContent(
    appContainer: AppContainer,
) {
    val signInViewModel: SignInViewModel =
        viewModel(
            factory = SignInViewModelFactory(
                authRepository =
                    appContainer.authRepository,
                sessionStorage =
                    appContainer.sessionStorage,
            )
        )

    val state by
    signInViewModel.uiState
        .collectAsStateWithLifecycle()

    SignInScreen(
        state = state,
        onPhoneChange =
            signInViewModel::updatePhone,
        onCodeChange =
            signInViewModel::updateCode,
        onSendOtp =
            signInViewModel::sendOtp,
        onSignIn =
            signInViewModel::verifyAndSignIn,
        onBackToPhone =
            signInViewModel::backToPhone,
    )
}