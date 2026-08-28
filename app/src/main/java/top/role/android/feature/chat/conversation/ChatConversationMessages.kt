package top.role.android.feature.chat.conversation

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import top.role.android.core.model.chat.MobileChatMessage
import top.role.android.core.model.chat.MobileChatReplyPreview

@Composable
internal fun ChatLoadEarlierMessages(
    loading: Boolean,
    onLoadMore: () -> Unit,
) {
    androidx.compose.foundation.layout.Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical =
                        4.dp
                ),

        contentAlignment =
            Alignment.Center,
    ) {
        if (
            loading
        ) {
            CircularProgressIndicator(
                modifier =
                    Modifier.widthIn(
                        max = 22.dp
                    ),

                strokeWidth =
                    2.dp,
            )
        } else {
            TextButton(
                onClick =
                    onLoadMore,
            ) {
                Text(
                    text =
                        "加载更早消息",

                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,
                )
            }
        }
    }
}

@Composable
internal fun ChatMessageRow(
    message: MobileChatMessage,
    imagePreviewRepository: ChatImagePreviewRepository,
    onLongClick: () -> Unit,
    onOpenImage: (String) -> Unit,
) {
    Row(
        modifier =
            Modifier.fillMaxWidth(),

        horizontalArrangement =
            if (
                message.mine
            ) {
                Arrangement.End
            } else {
                Arrangement.Start
            },
    ) {
        Column(
            horizontalAlignment =
                if (
                    message.mine
                ) {
                    Alignment.End
                } else {
                    Alignment.Start
                },

            modifier =
                Modifier.fillMaxWidth(
                    0.80f
                ),
        ) {
            if (
                !message.mine
            ) {
                message.sender
                    ?.displayName
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?.let { name ->
                        Text(
                            text =
                                name,

                            style =
                                MaterialTheme
                                    .typography
                                    .labelSmall,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant,

                            modifier =
                                Modifier.padding(
                                    start = 4.dp,
                                    bottom = 3.dp,
                                ),
                        )
                    }
            }

            ChatMessageBubble(
                message =
                    message,

                imagePreviewRepository =
                    imagePreviewRepository,

                onLongClick =
                    onLongClick,

                onOpenImage =
                    onOpenImage,
            )

            Text(
                text =
                    formatMessageTime(
                        message.createdAt
                    ),

                style =
                    MaterialTheme
                        .typography
                        .labelSmall,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant,

                modifier =
                    Modifier.padding(
                        start = 4.dp,
                        top = 3.dp,
                        end = 4.dp,
                    ),
            )
        }
    }
}

@Composable
private fun ChatMessageBubble(
    message: MobileChatMessage,
    imagePreviewRepository: ChatImagePreviewRepository,
    onLongClick: () -> Unit,
    onOpenImage: (String) -> Unit,
) {
    val backgroundColor =
        if (
            message.mine
        ) {
            MaterialTheme
                .colorScheme
                .primaryContainer
        } else {
            MaterialTheme
                .colorScheme
                .surfaceVariant
        }

    val contentColor =
        if (
            message.mine
        ) {
            MaterialTheme
                .colorScheme
                .onPrimaryContainer
        } else {
            MaterialTheme
                .colorScheme
                .onSurfaceVariant
        }

    val shape =
        if (
            message.mine
        ) {
            RoundedCornerShape(
                topStart =
                    16.dp,

                topEnd =
                    16.dp,

                bottomStart =
                    16.dp,

                bottomEnd =
                    5.dp,
            )
        } else {
            RoundedCornerShape(
                topStart =
                    16.dp,

                topEnd =
                    16.dp,

                bottomStart =
                    5.dp,

                bottomEnd =
                    16.dp,
            )
        }

    Column(
        modifier =
            Modifier
                .clip(
                    shape
                )
                .combinedClickable(
                    onClick = {},
                    onLongClick =
                        onLongClick,
                )
                .then(
                    Modifier
                        .padding(0.dp)
                )
                .background(
                    color =
                        backgroundColor,

                    shape =
                        shape,
                )
                .padding(
                    horizontal =
                        13.dp,

                    vertical =
                        9.dp,
                ),
    ) {
        if (
            message.deletedAt != null
        ) {
            Text(
                text =
                    "消息已撤回",

                color =
                    contentColor,

                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,
            )

            return
        }

        message.replyTo?.let { reply ->
            ChatReplyPreview(
                reply =
                    reply,

                imagePreviewRepository =
                    imagePreviewRepository,

                contentColor =
                    contentColor,
            )

            Spacer(
                modifier =
                    Modifier.height(
                        7.dp
                    )
            )
        }

        when (
            message.kind
        ) {
            "text" -> {
                Text(
                    text =
                        message.text
                            .orEmpty(),

                    color =
                        contentColor,

                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,
                )
            }

            "image" -> {
                ChatImageMessageContent(
                    message =
                        message,

                    contentColor =
                        contentColor,

                    imagePreviewRepository =
                        imagePreviewRepository,

                    onOpenImage =
                        onOpenImage,

                    onLongClick =
                        onLongClick,
                )
            }

            else -> {
                Text(
                    text =
                        "[${message.kind}]",

                    color =
                        contentColor,

                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun ChatReplyPreview(
    reply:
    MobileChatReplyPreview,

    imagePreviewRepository:
    ChatImagePreviewRepository,

    contentColor:
    Color,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        8.dp
                    )
                )
                .padding(
                    horizontal = 9.dp,
                    vertical = 7.dp,
                ),

        horizontalArrangement =
            Arrangement.spacedBy(
                8.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically,
    ) {
        if (
            !reply.deleted &&
            reply.kind == "image"
        ) {
            reply.imageId
                ?.takeIf {
                    it.isNotBlank()
                }
                ?.let { imageId ->
                    ChatImagePreview(
                        imageId =
                            imageId,

                        kind =
                            "thumb",

                        repository =
                            imagePreviewRepository,

                        modifier =
                            Modifier
                                .width(
                                    42.dp
                                )
                                .height(
                                    42.dp
                                )
                                .clip(
                                    RoundedCornerShape(
                                        7.dp
                                    )
                                ),

                        contentDescription =
                            "回复图片",

                        contentScale =
                            ContentScale.Crop,
                    )
                }
        }

        Column(
            modifier =
                Modifier.weight(
                    1f
                ),
        ) {
            reply.senderName
                ?.takeIf {
                    it.isNotBlank()
                }
                ?.let { name ->
                    Text(
                        text =
                            name,

                        style =
                            MaterialTheme
                                .typography
                                .labelSmall,

                        color =
                            contentColor.copy(
                                alpha =
                                    0.75f
                            ),

                        maxLines =
                            1,

                        overflow =
                            TextOverflow.Ellipsis,
                    )
                }

            Text(
                text =
                    when {
                        reply.deleted ->
                            "消息已撤回"

                        reply.kind ==
                                "image" ->
                            "[图片]"

                        !reply.text
                            .isNullOrBlank() ->
                            reply.text

                        else ->
                            "消息"
                    },

                style =
                    MaterialTheme
                        .typography
                        .bodySmall,

                color =
                    contentColor.copy(
                        alpha =
                            0.85f
                    ),

                maxLines =
                    2,

                overflow =
                    TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ChatImageMessageContent(
    message: MobileChatMessage,
    imagePreviewRepository: ChatImagePreviewRepository,
    contentColor: Color,
    onOpenImage: (String) -> Unit,
    onLongClick: () -> Unit,
) {
    val imageId =
        message.attachments
            ?.firstOrNull()
            ?.imageId
            ?.takeIf {
                it.isNotBlank()
            }

    if (
        imageId == null
    ) {
        Text(
            text =
                "[图片]",

            color =
                contentColor,
        )

        return
    }

    Column {
        ChatImagePreview(
            imageId =
                imageId,

            kind =
                "thumb",

            repository =
                imagePreviewRepository,

            modifier =
                Modifier
                    .widthIn(
                        max = 240.dp
                    )
                    .heightIn(
                        min = 120.dp,
                        max = 320.dp
                    )
                    .clip(
                        RoundedCornerShape(
                            12.dp
                        )
                    )
                    .combinedClickable(
                        onClick = {
                            onOpenImage(
                                imageId
                            )
                        },

                        onLongClick =
                            onLongClick,
                    ),

            contentDescription =
                "聊天图片",

            contentScale =
                ContentScale.Crop,
        )

        message.text
            ?.takeIf {
                it.isNotBlank()
            }
            ?.let { caption ->
                Text(
                    text =
                        caption,

                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,

                    color =
                        contentColor,

                    modifier =
                        Modifier.padding(
                            top = 7.dp,
                        ),
                )
            }
    }
}