package top.role.android.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import top.role.android.core.auth.AuthState

@Composable
fun AuthStatusContent(
    authState: AuthState,
    onLogin: () -> Unit,
    onRetry: () -> Unit,
    onReSignIn: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (userId: String) -> Unit,
) {
    when (val state = authState) {
        AuthState.Loading -> {
            AuthLoadingContent(modifier)
        }

        AuthState.SignedOut -> {
            AuthRequiredContent(
                modifier = modifier,
                onLogin = onLogin,
            )
        }

        is AuthState.SignedIn -> {
            content(state.userId)
        }

        is AuthState.NeedVerify -> {
            AuthVerifyContent(
                modifier = modifier,
                onRetry = onRetry,
            )
        }

        is AuthState.RequireLogin -> {
            AuthLoginRequiredContent(
                modifier = modifier,
                onReSignIn = onReSignIn,
            )
        }

        is AuthState.AccountUnavailable -> {
            AccountUnavailableContent(
                modifier = modifier,
                code = state.code,
                onReSignIn = onReSignIn,
            )
        }
    }
}

@Composable
private fun AuthLoadingContent(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(28.dp),
        )
    }
}

@Composable
private fun AuthRequiredContent(
    modifier: Modifier = Modifier,
    onLogin: () -> Unit,
) {
    AuthMessageContent(
        modifier = modifier,
        title = "登录后继续",
        description = "请登录账户后使用此功能。",
        action = {
            Button(onClick = onLogin) {
                Text("登录")
            }
        },
    )
}

@Composable
private fun AuthVerifyContent(
    modifier: Modifier = Modifier,
    onRetry: () -> Unit,
) {
    AuthMessageContent(
        modifier = modifier,
        title = "暂时无法验证身份",
        description = "请检查网络连接后重试。",
        action = {
            OutlinedButton(onClick = onRetry) {
                Text("重新检查")
            }
        },
    )
}

@Composable
private fun AuthLoginRequiredContent(
    modifier: Modifier = Modifier,
    onReSignIn: () -> Unit,
) {
    AuthMessageContent(
        modifier = modifier,
        title = "登录状态已失效",
        description = "请重新登录后继续使用。",
        action = {
            Button(onClick = onReSignIn) {
                Text("重新登录")
            }
        },
    )
}

@Composable
private fun AccountUnavailableContent(
    modifier: Modifier = Modifier,
    code: String,
    onReSignIn: () -> Unit,
) {
    AuthMessageContent(
        modifier = modifier,
        title = "账户暂不可用",
        description = when (code) {
            "ACCOUNT_DELETED" -> "该账户已注销。"
            "USER_NOT_FOUND" -> "未找到对应账户。"
            else -> "当前账户无法使用，请重新登录或联系支持。"
        },
        action = {
            OutlinedButton(onClick = onReSignIn) {
                Text("重新登录")
            }
        },
    )
}

@Composable
private fun AuthMessageContent(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    action: @Composable () -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            action()
        }
    }
}