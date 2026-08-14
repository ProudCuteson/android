package top.role.android.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import top.role.android.core.auth.AuthState
import top.role.android.feature.auth.SignInScreen
import top.role.android.feature.auth.SignInViewModel
import top.role.android.feature.auth.SignInViewModelFactory
import top.role.android.feature.main.MainScreen

@Composable
fun AppRoot(
    appContainer: AppContainer,
) {
    val sessionViewModel: AppSessionViewModel =
        viewModel(
            factory = AppSessionViewModelFactory(
                sessionStorage =
                    appContainer.sessionStorage,

                convexClient =
                    appContainer.convexClient,
            )
        )

    val authState by
    sessionViewModel.authState
        .collectAsStateWithLifecycle()

    when (authState) {
        AuthState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        AuthState.SignedOut -> {
            SignedOutContent(
                appContainer = appContainer,
            )
        }

        is AuthState.SignedIn -> {
            val session =
                (authState as AuthState.SignedIn).session

            MainScreen(
                discoverRepository = appContainer.discoverRepository,
                spaceListRepository = appContainer.spaceListRepository,
                entityDetailRepository = appContainer.entityDetailRepository,
                topicDetailRepository = appContainer.topicDetailRepository,
                docDetailRepository = appContainer.docDetailRepository,
                onSignOut = sessionViewModel::signOut,
            )
        }
    }
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