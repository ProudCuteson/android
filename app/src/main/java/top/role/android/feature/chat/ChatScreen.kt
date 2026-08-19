package top.role.android.feature.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

import androidx.compose.runtime.Composable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import top.role.android.feature.chat.conversationlist.ChatConversationListUiState
import top.role.android.feature.chat.conversationlist.ChatConversationRow

@Composable
fun ChatScreen(
    state: ChatConversationListUiState,
    onRetry: () -> Unit,
    onConversationClick: (String) -> Unit,
) {
    Column(
        modifier =
            Modifier.fillMaxSize(),
    ) {

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
        ) {
            when (state) {
                ChatConversationListUiState.Loading -> {
                    Box(
                        modifier =
                            Modifier.fillMaxSize(),

                        contentAlignment =
                            Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                ChatConversationListUiState.Offline -> {
                    ChatMessageState(
                        title =
                            "当前处于离线状态",

                        description =
                            "连接网络后即可继续使用聊天。",

                        actionLabel =
                            "重试",

                        onAction =
                            onRetry,
                    )
                }

                ChatConversationListUiState.SignedOut -> {
                    ChatMessageState(
                        title =
                            "登录后使用聊天",

                        description =
                            "请先登录账号。",
                    )
                }

                is ChatConversationListUiState.Error -> {
                    ChatMessageState(
                        title =
                            "无法加载聊天",

                        description =
                            state.message,

                        actionLabel =
                            "重试",

                        onAction =
                            onRetry,
                    )
                }

                is ChatConversationListUiState.Content -> {
                    if (
                        state.conversations.isEmpty()
                    ) {
                        ChatMessageState(
                            title =
                                "暂无会话",

                            description =
                                "新的聊天会显示在这里。",
                        )
                    } else {
                        LazyColumn(
                            modifier =
                                Modifier.fillMaxSize(),
                        ) {
                            items(
                                items =
                                    state.conversations,

                                key = {
                                    it.conversationId
                                },
                            ) { item ->

                                ChatConversationRow(
                                    item =
                                        item,

                                    onClick = {
                                        onConversationClick(
                                            item.conversationId
                                        )
                                    },
                                )
                            }
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
    description: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Box(
        modifier =
            Modifier.fillMaxSize(),

        contentAlignment =
            Alignment.Center,
    ) {
        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally,

            verticalArrangement =
                Arrangement.spacedBy(
                    8.dp
                ),
        ) {
            Text(
                text =
                    title,
            )

            Text(
                text =
                    description,
            )

            if (
                actionLabel != null &&
                onAction != null
            ) {
                Button(
                    onClick =
                        onAction,
                ) {
                    Text(
                        text =
                            actionLabel,
                    )
                }
            }
        }
    }
}