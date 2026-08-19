package top.role.android.feature.chat.conversation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
internal fun ChatComposerArea(
    state:
    ChatConversationUiState,

    onInputTextChange:
        (String) -> Unit,

    onSendText:
        () -> Unit,

    onPickImage:
        () -> Unit,

    onCancelImageDraft:
        () -> Unit,

    onSendImage:
        () -> Unit,

    onCancelReply:
        () -> Unit,
) {
    Column(
        modifier =
            Modifier.fillMaxWidth(),
    ) {
        state.replyTarget
            ?.let { reply ->
                ChatReplyComposerBar(
                    reply =
                        reply,

                    onCancel =
                        onCancelReply,
                )
            }

        state.imageDraft
            ?.let { draft ->
                ChatImageDraftPreview(
                    draft =
                        draft,

                    sending =
                        state.imageSending,

                    errorMessage =
                        state.imageSendErrorMessage,

                    onCancel =
                        onCancelImageDraft,

                    onSend =
                        onSendImage,
                )
            }

        ChatMessageComposer(
            text =
                state.inputText,

            sending =
                state.sending,

            imageSending =
                state.imageSending,

            imageDraftExists =
                state.imageDraft != null,

            onTextChange =
                onInputTextChange,

            onSend =
                onSendText,

            onPickImage =
                onPickImage,
        )
    }
}

@Composable
private fun ChatReplyComposerBar(
    reply:
    ChatReplyTarget,

    onCancel:
        () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal =
                        14.dp,

                    vertical =
                        7.dp,
                ),

        verticalAlignment =
            Alignment.CenterVertically,
    ) {
        Column(
            modifier =
                Modifier.weight(1f),
        ) {
            Text(
                text =
                    "回复 ${reply.senderName}",

                style =
                    MaterialTheme
                        .typography
                        .labelMedium,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis,
            )

            Text(
                text =
                    when {
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
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis,
            )
        }

        IconButton(
            onClick =
                onCancel,
        ) {
            Icon(
                imageVector =
                    Icons.Default.Close,

                contentDescription =
                    "取消回复",
            )
        }
    }
}

@Composable
private fun ChatImageDraftPreview(
    draft:
    ChatImageDraft,

    sending:
    Boolean,

    errorMessage:
    String?,

    onCancel:
        () -> Unit,

    onSend:
        () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal =
                        14.dp,

                    vertical =
                        8.dp,
                ),
    ) {
        Box {
            AsyncImage(
                model =
                    draft.sourceUri,

                contentDescription =
                    "待发送图片",

                contentScale =
                    ContentScale.Crop,

                modifier =
                    Modifier
                        .size(
                            120.dp
                        )
                        .clip(
                            RoundedCornerShape(
                                12.dp
                            )
                        ),
            )

            IconButton(
                onClick =
                    onCancel,

                enabled =
                    !sending,

                modifier =
                    Modifier.align(
                        Alignment.TopEnd
                    ),
            ) {
                Icon(
                    imageVector =
                        Icons.Default.Close,

                    contentDescription =
                        "取消图片",
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(
                    7.dp
                )
        )

        when {
            draft.uploading -> {
                Text(
                    text =
                        "正在上传图片…",

                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,
                )
            }

            draft.sending -> {
                Text(
                    text =
                        "正在发送…",

                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,
                )
            }

            !errorMessage
                .isNullOrBlank() -> {

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    verticalAlignment =
                        Alignment.CenterVertically,
                ) {
                    Text(
                        text =
                            errorMessage,

                        style =
                            MaterialTheme
                                .typography
                                .bodySmall,

                        color =
                            MaterialTheme
                                .colorScheme
                                .error,

                        modifier =
                            Modifier.weight(1f),
                    )

                    TextButton(
                        onClick =
                            onSend,
                    ) {
                        Text(
                            "重试"
                        )
                    }
                }
            }

            else -> {
                Button(
                    onClick =
                        onSend,

                    enabled =
                        !sending,
                ) {
                    Text(
                        "发送图片"
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatMessageComposer(
    text:
    String,

    sending:
    Boolean,

    imageSending:
    Boolean,

    imageDraftExists:
    Boolean,

    onTextChange:
        (String) -> Unit,

    onSend:
        () -> Unit,

    onPickImage:
        () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(
                    start =
                        8.dp,

                    end =
                        8.dp,

                    top =
                        8.dp,

                    bottom =
                        10.dp,
                ),

        verticalAlignment =
            Alignment.Bottom,

        horizontalArrangement =
            Arrangement.spacedBy(
                4.dp
            ),
    ) {
        IconButton(
            onClick =
                onPickImage,

            enabled =
                !sending &&
                        !imageSending &&
                        !imageDraftExists,

            modifier =
                Modifier.size(
                    52.dp
                ),
        ) {
            Icon(
                imageVector =
                    Icons.Outlined.Image,

                contentDescription =
                    "选择图片",
            )
        }

        OutlinedTextField(
            value =
                text,

            onValueChange =
                onTextChange,

            modifier =
                Modifier
                    .weight(1f)
                    .heightIn(
                        min =
                            52.dp,

                        max =
                            132.dp,
                    ),

            placeholder = {
                Text(
                    text =
                        "输入消息",
                )
            },

            maxLines =
                4,

            shape =
                RoundedCornerShape(
                    18.dp
                ),
        )

        IconButton(
            onClick =
                onSend,

            enabled =
                !sending &&
                        !imageSending &&
                        text.isNotBlank(),

            modifier =
                Modifier.size(
                    52.dp
                ),
        ) {
            if (
                sending
            ) {
                CircularProgressIndicator(
                    modifier =
                        Modifier.size(
                            20.dp
                        ),

                    strokeWidth =
                        2.dp,
                )
            } else {
                Icon(
                    imageVector =
                        Icons
                            .AutoMirrored
                            .Filled
                            .Send,

                    contentDescription =
                        "发送",
                )
            }
        }
    }
}