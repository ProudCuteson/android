package top.role.android.feature.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import top.role.android.core.auth.AuthState
import top.role.android.ui.auth.AuthStatusContent
import top.role.android.ui.components.RemoteMessageState
import top.role.android.feature.chat.conversationlist.ChatConversationListUiState
import top.role.android.feature.chat.conversationlist.ChatConversationRow
import top.role.android.feature.chat.conversationlist.ChatConversationListViewModel

@Composable
fun ChatScreen(
    viewModel: ChatConversationListViewModel,
    authState: AuthState,
    remoteOnline: Boolean,
    onLogin: () -> Unit,
    onRetryAuth: () -> Unit,
    onReSignIn: () -> Unit,
    onConversationClick: (String) -> Unit,
    onHideConversation: (String) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    AuthStatusContent(
        authState = authState,
        onLogin = onLogin,
        onRetry = onRetryAuth,
        onReSignIn = onReSignIn,
    ) { _ ->

        when {
            !remoteOnline -> {
                RemoteMessageState(
                    title = "当前处于离线状态",
                    description = "连接网络后将自动恢复聊天。",
                )
            }

            state is ChatConversationListUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            state is ChatConversationListUiState.Error -> {
                val error = state as ChatConversationListUiState.Error

                ChatMessageState(
                    title = "无法加载聊天",
                    actionLabel = "重试",
                    onAction = viewModel::retry,
                )
            }

            state is ChatConversationListUiState.Content -> {
                val content = state as ChatConversationListUiState.Content

                if (content.conversations.isEmpty()) {
                    ChatMessageState(
                        title = "暂无会话",
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(
                            items = content.conversations,
                            key = { it.conversationId },
                        ) { item ->
                            ChatConversationRow(
                                item = item,
                                onClick = {
                                    onConversationClick(item.conversationId)
                                },
                                onHide = {
                                    onHideConversation(item.conversationId)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatMessageState(
    title: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text =  title,
            )

            if (
                actionLabel != null &&
                onAction != null
            ) {
                Button(
                    onClick = onAction,
                ) {
                    Text(
                        text = actionLabel,
                    )
                }
            }
        }
    }
}