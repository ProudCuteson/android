package top.role.android.feature.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout

import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import top.role.android.core.model.account.MobileAccountHome
import top.role.android.core.preferences.AppAppearance
import top.role.android.core.preferences.WeekStart

import top.role.android.feature.account.components.AccountAboutSection
import top.role.android.feature.account.components.AccountBasicInfoSection
import top.role.android.feature.account.components.AccountCreditsSection
import top.role.android.feature.account.components.AccountPreferencesSection
import top.role.android.feature.account.components.AccountProfileSection
import top.role.android.feature.account.components.AccountSecuritySection



@Composable
fun AccountScreen(
    viewModel: AccountViewModel?,
    remoteAuthenticated: Boolean,

    appearance: AppAppearance,
    weekStart: WeekStart,

    onAppearanceChange: (AppAppearance) -> Unit,
    onWeekStartChange: (WeekStart) -> Unit,

    versionName: String,

    onOpenCredits: () -> Unit,
    onOpenPassword: () -> Unit,

    onOpenTerms: () -> Unit,
    onOpenPrivacy: () -> Unit,

    onReSignIn: () -> Unit,
    onSignOut: () -> Unit,
) {
    val uiState =
        viewModel
            ?.uiState
            ?.collectAsStateWithLifecycle()
            ?.value

    AccountScreenContent(
        uiState = uiState,
        remoteAuthenticated = remoteAuthenticated,

        onRetry = {
            viewModel?.retry()
        },

        appearance = appearance,
        weekStart = weekStart,

        onAppearanceChange = onAppearanceChange,
        onWeekStartChange = onWeekStartChange,

        versionName = versionName,

        onOpenCredits = onOpenCredits,
        onOpenPassword = onOpenPassword,

        onOpenTerms = onOpenTerms,
        onOpenPrivacy = onOpenPrivacy,

        onReSignIn = onReSignIn,
        onSignOut = onSignOut,
    )
}

@Composable
private fun AccountScreenContent(
    uiState: AccountUiState?,
    remoteAuthenticated: Boolean,
    onRetry: () -> Unit,

    appearance: AppAppearance,
    weekStart: WeekStart,

    onAppearanceChange: (AppAppearance) -> Unit,
    onWeekStartChange: (WeekStart) -> Unit,

    versionName: String,

    onOpenCredits: () -> Unit,
    onOpenPassword: () -> Unit,

    onOpenTerms: () -> Unit,
    onOpenPrivacy: () -> Unit,

    onReSignIn: () -> Unit,
    onSignOut: () -> Unit,
) {
    val account =
        if (remoteAuthenticated) {
            uiState?.account
        } else {
            null
        }

    when {
        !remoteAuthenticated -> {
            AccountFallbackContent(
                title = "当前未登录",
                message = "登录后可查看账户信息、积分及使用在线服务。本地笔记仍可正常使用。",
                actionLabel = "登录账户",
                onAction = onReSignIn,

                appearance = appearance,
                weekStart = weekStart,
                onAppearanceChange = onAppearanceChange,
                onWeekStartChange = onWeekStartChange,
                versionName = versionName,
                onOpenTerms = onOpenTerms,
                onOpenPrivacy = onOpenPrivacy,
            )
        }

        account != null -> {
            AccountContent(
                account = account,
                appearance = appearance,
                weekStart = weekStart,
                onAppearanceChange = onAppearanceChange,
                onWeekStartChange = onWeekStartChange,
                versionName = versionName,
                onOpenCredits = onOpenCredits,
                onOpenPassword = onOpenPassword,
                onOpenTerms = onOpenTerms,
                onOpenPrivacy = onOpenPrivacy,
                onSignOut = onSignOut,
            )
        }

        else -> {
            val loading =
                uiState == null || uiState.loading

            AccountFallbackContent(
                title =
                    if (loading) {
                        "正在加载账户信息"
                    } else {
                        "无法加载账户"
                    },

                message =
                    if (loading) {
                        "正在获取您的账户资料…"
                    } else {
                        uiState.errorMessage
                            ?: "账户信息暂时不可用。"
                    },

                loading = loading,

                actionLabel =
                    if (loading) null else "重试",

                onAction =
                    if (loading) null else onRetry,

                appearance = appearance,
                weekStart = weekStart,
                onAppearanceChange = onAppearanceChange,
                onWeekStartChange = onWeekStartChange,
                versionName = versionName,
                onOpenTerms = onOpenTerms,
                onOpenPrivacy = onOpenPrivacy,
            )
        }
    }
}

@Composable
private fun AccountFallbackContent(
    title: String,
    message: String,

    loading: Boolean = false,

    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,

    appearance: AppAppearance,
    weekStart: WeekStart,

    onAppearanceChange: (AppAppearance) -> Unit,
    onWeekStartChange: (WeekStart) -> Unit,

    versionName: String,

    onOpenTerms: () -> Unit,
    onOpenPrivacy: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),

        contentPadding = PaddingValues(
            horizontal = 20.dp,
            vertical = 20.dp,
        ),

        verticalArrangement =
            Arrangement.spacedBy(24.dp),
    ) {

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement =
                    Arrangement.spacedBy(12.dp),
            ) {
                if (loading) {
                    CircularProgressIndicator()
                }

                Text(
                    text = title,
                    style =
                        MaterialTheme.typography.titleMedium,
                )

                Text(
                    text = message,
                    style =
                        MaterialTheme.typography.bodyMedium,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant,
                )

                if (
                    actionLabel != null &&
                    onAction != null
                ) {
                    Button(
                        onClick = onAction,
                    ) {
                        Text(actionLabel)
                    }
                }
            }
        }

        item {
            AccountPreferencesSection(
                appearance = appearance,
                weekStart = weekStart,

                onAppearanceChange =
                    onAppearanceChange,

                onWeekStartChange =
                    onWeekStartChange,
            )
        }

        item {
            AccountAboutSection(
                versionName = versionName,
                onOpenTerms = onOpenTerms,
                onOpenPrivacy = onOpenPrivacy,
            )
        }

        item {
            Spacer(
                modifier = Modifier.height(16.dp)
            )
        }
    }
}

@Composable
private fun AccountLoadingState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Spacer( modifier = Modifier.height(16.dp))
        Text(
            text = "正在加载账户信息…",
            style =  MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AccountErrorState(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "无法加载账户",
            style = MaterialTheme.typography.titleMedium,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = onRetry) {
            Text("重试")
        }
    }
}

@Composable
private fun AccountContent(
    account: MobileAccountHome,

    appearance: AppAppearance,
    weekStart: WeekStart,

    onAppearanceChange: (AppAppearance) -> Unit,
    onWeekStartChange: (WeekStart) -> Unit,

    versionName: String,

    onOpenCredits: () -> Unit,
    onOpenPassword: () -> Unit,

    onOpenTerms: () -> Unit,
    onOpenPrivacy: () -> Unit,

    onSignOut: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = 20.dp,
            vertical = 20.dp,
            ),
        verticalArrangement =
            Arrangement.spacedBy(
                24.dp
            ),
    ) {

        item {
            AccountProfileSection(
                profile =
                    account.accountProfile,
            )
        }


        item {
            AccountBasicInfoSection(
                profile =
                    account.accountProfile,
            )
        }


        item {
            AccountCreditsSection(
                balance =
                    account.balance,

                onOpenCredits =
                    onOpenCredits,
            )
        }


        item {
            AccountSecuritySection(
                profile =
                    account.accountProfile,

                onOpenPassword =
                    onOpenPassword,
            )
        }


        item {
            AccountPreferencesSection(
                appearance =
                    appearance,

                weekStart =
                    weekStart,

                onAppearanceChange =
                    onAppearanceChange,

                onWeekStartChange =
                    onWeekStartChange,
            )
        }


        item {
            AccountAboutSection(
                versionName =
                    versionName,

                onOpenTerms =
                    onOpenTerms,

                onOpenPrivacy =
                    onOpenPrivacy,
            )
        }


        item {
            AccountSignOutButton(
                onSignOut =
                    onSignOut,
            )
        }


        item {
            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )
        }
    }
}


@Composable
private fun AccountSignOutButton(
    onSignOut: () -> Unit,
) {
    TextButton(
        modifier =
            Modifier.fillMaxWidth(),

        onClick =
            onSignOut,
    ) {

        Icon(
            imageVector =
                Icons.AutoMirrored.Outlined.Logout,

            contentDescription =
                null,
        )


        Spacer(
            modifier =
                Modifier.padding(
                    horizontal = 4.dp,
                )
        )


        Text(
            text =
                "退出登录",

            color =
                MaterialTheme.colorScheme.error,
        )
    }
}