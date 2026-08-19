package top.role.android.feature.chat.conversation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import top.role.android.core.model.chat.MobileChatMessage

@Composable
internal fun ChatMessageActionDialog(
    message:
    MobileChatMessage,

    operating:
    Boolean,

    onDismiss:
        () -> Unit,

    onReply:
        () -> Unit,

    onRecall:
        () -> Unit,

    onDelete:
        () -> Unit,
) {
    AlertDialog(
        onDismissRequest =
            onDismiss,

        title = {
            Text(
                "消息操作"
            )
        },

        text = {
            Column {
                if (
                    message.deletedAt == null
                ) {
                    TextButton(
                        onClick =
                            onReply,

                        enabled =
                            !operating,
                    ) {
                        Text(
                            "回复"
                        )
                    }
                }

                if (
                    message.mine &&
                    message.deletedAt == null
                ) {
                    TextButton(
                        onClick =
                            onRecall,

                        enabled =
                            !operating,
                    ) {
                        Text(
                            "撤回"
                        )
                    }
                }

                TextButton(
                    onClick =
                        onDelete,

                    enabled =
                        !operating,
                ) {
                    Text(
                        "仅自己删除"
                    )
                }
            }
        },

        confirmButton = {},

        dismissButton = {
            TextButton(
                onClick =
                    onDismiss,
            ) {
                Text(
                    "取消"
                )
            }
        },
    )
}

@Composable
internal fun ChatFullScreenImageDialog(
    url:
    String,

    onDismiss:
        () -> Unit,
) {
    Dialog(
        onDismissRequest =
            onDismiss,

        properties =
            DialogProperties(
                usePlatformDefaultWidth =
                    false,
            ),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black
                    ),
        ) {
            AsyncImage(
                model =
                    url,

                contentDescription =
                    "图片原图",

                modifier =
                    Modifier.fillMaxSize(),

                contentScale =
                    ContentScale.Fit,
            )

            IconButton(
                onClick =
                    onDismiss,

                modifier =
                    Modifier
                        .align(
                            Alignment.TopEnd
                        )
                        .padding(
                            12.dp
                        ),
            ) {
                Icon(
                    imageVector =
                        Icons.Default.Close,

                    contentDescription =
                        "关闭",

                    tint =
                        Color.White,
                )
            }
        }
    }
}