package top.role.android.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import top.role.android.core.preferences.AppAppearance
import top.role.android.ui.theme.TopRoleTheme

@Composable
fun TopRoleApp() {

    val context = LocalContext.current

    val appContainer = remember {
        AppContainer(
            context = context.applicationContext,
        )
    }

    val preferencesViewModel: AppPreferencesViewModel = viewModel(
        factory = AppPreferencesViewModel.factory(
            repository = appContainer.appPreferencesRepository,
        ),
    )

    val appearance by preferencesViewModel
        .appearance
        .collectAsStateWithLifecycle()

    val systemDarkTheme = isSystemInDarkTheme()
    val darkTheme = when (appearance) {
        AppAppearance.System -> systemDarkTheme
        AppAppearance.Light -> false
        AppAppearance.Dark -> true
    }

    TopRoleTheme(
        darkTheme = darkTheme,
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            AppRoot(
                appContainer = appContainer,
                appPreferencesViewModel = preferencesViewModel,
            )
        }
    }
}