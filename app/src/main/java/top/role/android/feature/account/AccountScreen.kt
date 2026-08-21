package top.role.android.feature.account

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import top.role.android.core.model.account.MobileAccountHome
import top.role.android.core.model.account.MobileAccountProfile
import androidx.compose.material3.Button


@Composable
fun AccountScreen(
    viewModel:
    AccountViewModel,

    onOpenCredits:
        () -> Unit,

    onOpenPassword:
        () -> Unit,

    onOpenSettings:
        () -> Unit,

    onReSignIn:
        () -> Unit,

    onSignOut:
        () -> Unit,
) {
    val uiState by
    viewModel.uiState
        .collectAsStateWithLifecycle()

    AccountScreenContent(
        uiState =
            uiState,

        onOpenCredits =
            onOpenCredits,

        onOpenPassword =
            onOpenPassword,

        onOpenSettings =
            onOpenSettings,

        onReSignIn = onReSignIn,

        onSignOut =
            onSignOut,
    )
}


@Composable
private fun AccountScreenContent(
    uiState:
    AccountUiState,

    onOpenCredits:
        () -> Unit,

    onOpenPassword:
        () -> Unit,

    onOpenSettings:
        () -> Unit,

    onReSignIn:
        () -> Unit,

    onSignOut:
        () -> Unit,
) {
    when {
        uiState.loading &&
                uiState.account == null -> {

            AccountLoadingState()
        }

        uiState.account != null -> {

            AccountContent(
                account =
                    uiState.account,

                onOpenCredits =
                    onOpenCredits,

                onOpenPassword =
                    onOpenPassword,

                onOpenSettings =
                    onOpenSettings,

                onSignOut =
                    onSignOut,
            )
        }

        else -> {

            AccountErrorState(
                message =
                    uiState.errorMessage
                        ?: "账户信息暂时不可用。",
                onReSignIn =
                    onReSignIn,
            )
        }
    }
}


@Composable
private fun AccountLoadingState() {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),

        verticalArrangement =
            Arrangement.Center,

        horizontalAlignment =
            Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()

        Spacer(
            modifier =
                Modifier.height(16.dp)
        )

        Text(
            text =
                "正在加载账户信息…",

            style =
                MaterialTheme.typography.bodyMedium,

            color =
                MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}


@Composable
private fun AccountErrorState(
    message: String,
    onReSignIn: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),

        verticalArrangement =
            Arrangement.Center,

        horizontalAlignment =
            Alignment.CenterHorizontally,
    ) {

        Text(
            text =
                "无法加载账户",

            style =
                MaterialTheme.typography.titleMedium,
        )

        Spacer(
            modifier =
                Modifier.height(8.dp)
        )

        Text(
            text =
                message,

            color =
                MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(
            modifier =
                Modifier.height(20.dp)
        )

        Button(
            onClick =
                onReSignIn,
        ) {
            Text(
                "重新登录"
            )
        }
    }
}

@Composable
private fun AccountContent(
    account:
    MobileAccountHome,

    onOpenCredits:
        () -> Unit,

    onOpenPassword:
        () -> Unit,

    onOpenSettings:
        () -> Unit,

    onSignOut:
        () -> Unit,
) {
    LazyColumn(
        modifier =
            Modifier.fillMaxSize(),

        contentPadding =
            PaddingValues(
                horizontal = 20.dp,
                vertical = 20.dp,
            ),

        verticalArrangement =
            Arrangement.spacedBy(
                24.dp
            ),
    ) {

        item {
            AccountProfileHeader(
                profile =
                    account.accountProfile,
            )
        }

        item {
            AccountSection(
                title =
                    "基础信息",
            ) {
                AccountInfoRow(
                    icon =
                        Icons.Outlined.Phone,

                    label =
                        "手机号",

                    value =
                        displayValue(
                            account.accountProfile.phone
                        ),
                )

                SectionDivider()

                AccountInfoRow(
                    icon =
                        Icons.Outlined.Email,

                    label =
                        "邮箱",

                    value =
                        displayValue(
                            account.accountProfile.email
                        ),
                )
            }
        }

        item {
            AccountSection(
                title =
                    "Credits",
            ) {
                AccountInfoRow(
                    icon =
                        Icons.Outlined.AccountBalanceWallet,

                    label =
                        "余额",

                    value =
                        formatCredits(
                            account.balance
                        ),
                )

                SectionDivider()

                AccountNavigationRow(
                    icon =
                        Icons.Outlined.AccountBalanceWallet,

                    title =
                        "购买 Credits",

                    onClick =
                        onOpenCredits,
                )
            }
        }

        item {
            AccountSection(
                title =
                    "安全",
            ) {
                AccountInfoRow(
                    icon =
                        Icons.Outlined.Shield,

                    label =
                        "密码",

                    value =
                        if (
                            account
                                .accountProfile
                                .hasPassword
                        ) {
                            "已设置"
                        } else {
                            "未设置"
                        },
                )

                SectionDivider()

                AccountNavigationRow(
                    icon =
                        Icons.Outlined.Lock,

                    title =
                        if (
                            account
                                .accountProfile
                                .hasPassword
                        ) {
                            "修改密码"
                        } else {
                            "设置密码"
                        },

                    onClick =
                        onOpenPassword,
                )

                if (
                    account
                        .accountProfile
                        .passwordTip
                        .isNotBlank()
                ) {
                    Text(
                        text =
                            account
                                .accountProfile
                                .passwordTip,

                        modifier =
                            Modifier.padding(
                                start = 16.dp,
                                end = 16.dp,
                                bottom = 14.dp,
                            ),

                        style =
                            MaterialTheme.typography.bodySmall,

                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        item {
            AccountSection(
                title =
                    "应用",
            ) {
                AccountNavigationRow(
                    icon =
                        Icons.Outlined.Settings,

                    title =
                        "设置",

                    onClick =
                        onOpenSettings,
                )
            }
        }

        item {
            TextButton(
                modifier =
                    Modifier.fillMaxWidth(),

                onClick =
                    onSignOut,
            ) {
                Icon(
                    imageVector =
                        Icons.Outlined.Logout,

                    contentDescription =
                        null,
                )

                Spacer(
                    modifier =
                        Modifier.size(8.dp)
                )

                Text(
                    text =
                        "退出登录",

                    color =
                        MaterialTheme.colorScheme.error,
                )
            }
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
private fun AccountProfileHeader(
    profile:
    MobileAccountProfile,
) {
    Row(
        modifier =
            Modifier.fillMaxWidth(),

        verticalAlignment =
            Alignment.CenterVertically,
    ) {

        Surface(
            modifier =
                Modifier.size(56.dp),

            shape =
                MaterialTheme.shapes.extraLarge,

            color =
                MaterialTheme.colorScheme.surfaceVariant,
        ) {

            if (
                profile.avatarUrl
                    .isNotBlank()
            ) {
                AsyncImage(
                    model =
                        profile.avatarUrl,

                    contentDescription =
                        profile.name,

                    modifier =
                        Modifier.fillMaxSize(),
                )
            } else {
                Column(
                    modifier =
                        Modifier.fillMaxSize(),

                    verticalArrangement =
                        Arrangement.Center,

                    horizontalAlignment =
                        Alignment.CenterHorizontally,
                ) {
                    Text(
                        text =
                            profileInitial(
                                profile.name
                            ),

                        style =
                            MaterialTheme.typography.titleLarge,

                        fontWeight =
                            FontWeight.SemiBold,

                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Spacer(
            modifier =
                Modifier.size(16.dp)
        )

        Column(
            modifier =
                Modifier.weight(1f),
        ) {
            Text(
                text =
                    displayValue(
                        profile.name
                    ),

                style =
                    MaterialTheme.typography.titleLarge,

                fontWeight =
                    FontWeight.SemiBold,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis,
            )
        }
    }
}


@Composable
private fun AccountSection(
    title:
    String,

    content:
    @Composable () -> Unit,
) {
    Column {
        Text(
            text =
                title,

            modifier =
                Modifier.padding(
                    start = 4.dp,
                    bottom = 8.dp,
                ),

            style =
                MaterialTheme.typography.labelLarge,

            color =
                MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Surface(
            modifier =
                Modifier.fillMaxWidth(),

            shape =
                MaterialTheme.shapes.large,

            tonalElevation =
                1.dp,
        ) {
            Column(
                modifier =
                    Modifier.fillMaxWidth(),
            ) {
                content()
            }
        }
    }
}


@Composable
private fun AccountInfoRow(
    icon:
    androidx.compose.ui.graphics.vector.ImageVector,

    label:
    String,

    value:
    String,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 15.dp,
                ),

        verticalAlignment =
            Alignment.CenterVertically,
    ) {
        Icon(
            imageVector =
                icon,

            contentDescription =
                null,

            modifier =
                Modifier.size(20.dp),

            tint =
                MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(
            modifier =
                Modifier.size(14.dp)
        )

        Text(
            text =
                label,

            style =
                MaterialTheme.typography.bodyLarge,
        )

        Spacer(
            modifier =
                Modifier.weight(1f)
        )

        Text(
            text =
                value,

            style =
                MaterialTheme.typography.bodyMedium,

            color =
                MaterialTheme.colorScheme.onSurfaceVariant,

            maxLines =
                1,

            overflow =
                TextOverflow.Ellipsis,
        )
    }
}


@Composable
private fun AccountNavigationRow(
    icon:
    androidx.compose.ui.graphics.vector.ImageVector,

    title:
    String,

    onClick:
        () -> Unit,
) {
    Modifier.clickable(
        onClick =
            onClick,
    )

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    onClick =
                        onClick,
                )
                .padding(
                    horizontal = 16.dp,
                    vertical = 15.dp,
                ),

        verticalAlignment =
            Alignment.CenterVertically,
    ) {
        Icon(
            imageVector =
                icon,

            contentDescription =
                null,

            modifier =
                Modifier.size(20.dp),

            tint =
                MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(
            modifier =
                Modifier.size(14.dp)
        )

        Text(
            text =
                title,

            modifier =
                Modifier.weight(1f),

            style =
                MaterialTheme.typography.bodyLarge,
        )

        Icon(
            imageVector =
                Icons.Outlined.ChevronRight,

            contentDescription =
                null,

            modifier =
                Modifier.size(20.dp),

            tint =
                MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(
        modifier =
            Modifier.padding(
                start = 50.dp,
            ),
    )
}


private fun displayValue(
    value:
    String,
): String {

    return value
        .trim()
        .ifEmpty {
            "—"
        }
}


private fun profileInitial(
    name:
    String,
): String {

    return name
        .trim()
        .firstOrNull()
        ?.uppercase()
        ?: "U"
}


private fun formatCredits(
    value:
    Double,
): String {

    return if (
        value % 1.0 == 0.0
    ) {
        "${value.toLong()} C"
    } else {
        "$value C"
    }
}