package top.role.android.feature.chat.conversation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import top.role.android.core.model.chat.MobileChatMessage
import top.role.android.core.model.chat.MobileConversationDetail
import top.role.android.core.model.chat.MobileConversationKind

@Composable
fun ChatConversationScreen(
    state: ChatConversationUiState,

    onBack: () -> Unit,

    onInputTextChange: (String) -> Unit,
    onSendText: () -> Unit,

    onImageSelected: (android.net.Uri) -> Unit,
    onCancelImageDraft: () -> Unit,
    onSendImage: () -> Unit,
    imagePreviewRepository: ChatImagePreviewRepository,

    onStartReply: (MobileChatMessage) -> Unit,
    onCancelReply: () -> Unit,

    onRecallMessage: (MobileChatMessage) -> Unit,
    onDeleteMessageForMe: (MobileChatMessage) -> Unit,

    onLoadMore: () -> Unit,
    onRetry: () -> Unit,

    onGroupSettingsClick: (String) -> Unit,
) {
    val imagePickerLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts.PickVisualMedia(),
        ) { uri ->
            uri?.let(onImageSelected)
        }

    when {
        state.loading -> {
            ChatConversationLoading()
        }

        state.notFound -> {
            ChatConversationUnavailable(
                title =
                    "会话不存在或已不可访问",
                onBack =
                    onBack,
            )
        }

        state.conversation == null -> {
            ChatConversationUnavailable(
                title =
                    state.errorMessage
                        ?: "暂时无法加载会话",
                actionLabel =
                    "重试",
                onAction =
                    onRetry,
                onBack =
                    onBack,
            )
        }

        else -> {
            ChatConversationContent(
                state =
                    state,

                conversation =
                    state.conversation,

                onBack =
                    onBack,

                onInputTextChange =
                    onInputTextChange,

                onSendText =
                    onSendText,

                onPickImage = {
                    imagePickerLauncher.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts
                                .PickVisualMedia
                                .ImageOnly
                        )
                    )
                },

                onCancelImageDraft =
                    onCancelImageDraft,

                onSendImage =
                    onSendImage,

                imagePreviewRepository =
                    imagePreviewRepository,

                onStartReply =
                    onStartReply,

                onCancelReply =
                    onCancelReply,

                onRecallMessage =
                    onRecallMessage,

                onDeleteMessageForMe =
                    onDeleteMessageForMe,

                onLoadMore =
                    onLoadMore,

                onGroupSettingsClick =
                    onGroupSettingsClick,
            )
        }
    }
}

@Composable
private fun ChatConversationContent(
    state: ChatConversationUiState,

    conversation: MobileConversationDetail,

    onBack: () -> Unit,

    onInputTextChange: (String) -> Unit,
    onSendText: () -> Unit,

    onPickImage: () -> Unit,
    onCancelImageDraft: () -> Unit,
    onSendImage: () -> Unit,
    imagePreviewRepository: ChatImagePreviewRepository,

    onStartReply: (MobileChatMessage) -> Unit,
    onCancelReply: () -> Unit,

    onRecallMessage: (MobileChatMessage) -> Unit,
    onDeleteMessageForMe: (MobileChatMessage) -> Unit,

    onLoadMore: () -> Unit,

    onGroupSettingsClick: (String) -> Unit,
) {
    val listState =
        rememberLazyListState()

    var selectedMessage by
    remember {
        mutableStateOf<MobileChatMessage?>(
            null
        )
    }

    var previewImageId by
    remember {
        mutableStateOf<String?>(
            null
        )
    }

    val newestMessageId =
        state.messages
            .lastOrNull()
            ?.messageId

    LaunchedEffect(
        newestMessageId,
    ) {
        if (
            newestMessageId != null &&
            state.messages.isNotEmpty()
        ) {
            listState.animateScrollToItem(
                state.messages.lastIndex
            )
        }
    }

    Column(
        modifier =
            Modifier.fillMaxSize(),
    ) {
        ChatConversationTopBar(
            conversation =
                conversation,

            onBack =
                onBack,

            onGroupSettingsClick =
                onGroupSettingsClick,
        )

        HorizontalDivider()

        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
        ) {
            when {
                state.messagesLoading &&
                        state.messages.isEmpty() -> {

                    CircularProgressIndicator(
                        modifier =
                            Modifier.align(
                                Alignment.Center
                            ),
                    )
                }

                state.messages.isEmpty() -> {
                    Text(
                        text =
                            "暂无消息",

                        style =
                            MaterialTheme
                                .typography
                                .bodyMedium,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant,

                        modifier =
                            Modifier.align(
                                Alignment.Center
                            ),
                    )
                }

                else -> {
                    LazyColumn(
                        state =
                            listState,

                        modifier =
                            Modifier.fillMaxSize(),

                        contentPadding =
                            PaddingValues(
                                horizontal =
                                    12.dp,

                                vertical =
                                    14.dp,
                            ),

                        verticalArrangement =
                            Arrangement.spacedBy(
                                10.dp
                            ),
                    ) {
                        if (
                            !state.messagesDone
                        ) {
                            item(
                                key =
                                    "load-more",
                            ) {
                                ChatLoadEarlierMessages(
                                    loading =
                                        state.loadingMore,

                                    onLoadMore =
                                        onLoadMore,
                                )
                            }
                        }

                        items(
                            items =
                                state.messages,

                            key = {
                                it.messageId
                            },
                        ) { message ->
                            ChatMessageRow(
                                message =
                                    message,

                                onLongClick = {
                                    selectedMessage =
                                        message
                                },

                                imagePreviewRepository =
                                    imagePreviewRepository,

                                onOpenImage = {
                                    previewImageId =
                                        it
                                },
                            )
                        }
                    }
                }
            }
        }

        ChatConversationOperationErrors(
            state =
                state,
        )

        HorizontalDivider()

        ChatComposerArea(
            state =
                state,

            imagePreviewRepository =
                imagePreviewRepository,

            onInputTextChange =
                onInputTextChange,

            onSendText =
                onSendText,

            onPickImage =
                onPickImage,

            onCancelImageDraft =
                onCancelImageDraft,

            onSendImage =
                onSendImage,

            onCancelReply =
                onCancelReply,
        )
    }

    selectedMessage?.let { message ->
        ChatMessageActionDialog(
            message =
                message,

            operating =
                state.operatingMessageId ==
                        message.messageId,

            onDismiss = {
                selectedMessage =
                    null
            },

            onReply = {
                selectedMessage =
                    null

                onStartReply(
                    message
                )
            },

            onRecall = {
                selectedMessage =
                    null

                onRecallMessage(
                    message
                )
            },

            onDelete = {
                selectedMessage =
                    null

                onDeleteMessageForMe(
                    message
                )
            },
        )
    }

    previewImageId?.let { imageId ->
        ChatFullScreenImageDialog(
            imageId =
                imageId,

            imagePreviewRepository =
                imagePreviewRepository,

            onDismiss = {
                previewImageId =
                    null
            },
        )
    }

}

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
private fun ChatConversationTopBar(
    conversation:
    MobileConversationDetail,

    onBack:
        () -> Unit,

    onGroupSettingsClick:
        (String) -> Unit,
) {
    val isGroup =
        conversation.kind ==
                MobileConversationKind.Group

    TopAppBar(
        navigationIcon = {
            IconButton(
                onClick =
                    onBack,
            ) {
                Icon(
                    imageVector =
                        Icons
                            .AutoMirrored
                            .Filled
                            .ArrowBack,

                    contentDescription =
                        "返回",
                )
            }
        },

        title = {
            Column(
                verticalArrangement =
                    Arrangement.Center,
            ) {
                Text(
                    text =
                        conversation.displayTitle,

                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,

                    fontWeight =
                        FontWeight.SemiBold,

                    maxLines =
                        1,

                    overflow =
                        TextOverflow.Ellipsis,
                )

                if (
                    isGroup
                ) {
                    Text(
                        text =
                            "${conversation.members.size} 位成员",

                        style =
                            MaterialTheme
                                .typography
                                .labelSmall,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant,
                    )
                }
            }
        },

        actions = {
            if (
                isGroup
            ) {
                IconButton(
                    onClick = {
                        onGroupSettingsClick(
                            conversation.conversationId
                        )
                    },
                ) {
                    Icon(
                        imageVector =
                            Icons
                                .Outlined
                                .Groups,

                        contentDescription =
                            "群聊信息",
                    )
                }
            }
        },

        colors =
            TopAppBarDefaults
                .topAppBarColors(
                    containerColor =
                        MaterialTheme
                            .colorScheme
                            .surface,
                ),
    )
}