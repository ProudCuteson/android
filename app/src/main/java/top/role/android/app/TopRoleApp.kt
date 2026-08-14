package top.role.android.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
fun TopRoleApp() {
    val context = LocalContext.current

    val appContainer = remember {
        AppContainer(
            context = context.applicationContext,
        )
    }

    AppRoot(
        appContainer = appContainer,
    )
}