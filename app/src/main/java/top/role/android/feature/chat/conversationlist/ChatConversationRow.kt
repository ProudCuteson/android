package top.role.android.feature.chat.conversationlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import top.role.android.core.model.chat.MobileConversationKind
import top.role.android.core.model.chat.MobileConversationListItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatConversationRow(
    item: MobileConversationListItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(
                    horizontal = 16.dp,
                    vertical = 10.dp,
                ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ChatConversationAvatar(
            imageUrl = item.displayAvatarUrl,
            title = item.displayTitle,
            isGroup = item.kind == MobileConversationKind.Group,
        )

        Spacer(
            modifier = Modifier.width(12.dp),
        )

        Column(
            modifier = Modifier.weight(1f),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = item.displayTitle,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )

                if (
                    item.kind ==
                    MobileConversationKind.Group
                ) {
                    Spacer(
                        modifier = Modifier.width(6.dp),
                    )

                    Text(
                        text = "群聊",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier =
                            Modifier
                                .background(
                                    color =
                                        MaterialTheme
                                            .colorScheme
                                            .surfaceVariant,
                                    shape =
                                        RoundedCornerShape(
                                            999.dp
                                        ),
                                )
                                .padding(
                                    horizontal = 6.dp,
                                    vertical = 2.dp,
                                ),
                    )
                }

                Spacer(
                    modifier = Modifier.width(8.dp),
                )

                val timeText =
                    formatChatTime(
                        item.lastMessageAt
                    )

                if (timeText != null) {
                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(4.dp),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text =
                        item.subtitle
                            ?: "暂无消息",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )

                if (item.unreadCount > 0.0) {
                    Spacer(
                        modifier = Modifier.width(8.dp),
                    )

                    val unread =
                        item
                            .unreadCount
                            .toInt()

                    Text(
                        text =
                            if (unread > 99) {
                                "99+"
                            } else {
                                unread.toString()
                            },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onError,
                        modifier =
                            Modifier
                                .background(
                                    color =
                                        MaterialTheme
                                            .colorScheme
                                            .error,
                                    shape =
                                        RoundedCornerShape(
                                            999.dp
                                        ),
                                )
                                .padding(
                                    horizontal = 6.dp,
                                    vertical = 2.dp,
                                ),
                    )
                } else if (item.unread) {
                    Spacer(
                        modifier = Modifier.width(8.dp),
                    )

                    Box(
                        modifier =
                            Modifier
                                .size(8.dp)
                                .background(
                                    color =
                                        MaterialTheme
                                            .colorScheme
                                            .error,
                                    shape =
                                        RoundedCornerShape(
                                            999.dp
                                        ),
                                ),
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatConversationAvatar(
    imageUrl: String?,
    title: String,
    isGroup: Boolean,
) {
    val shape =
        RoundedCornerShape(
            14.dp
        )

    Box(
        modifier =
            Modifier
                .size(48.dp)
                .clip(shape)
                .background(
                    MaterialTheme
                        .colorScheme
                        .surfaceVariant
                ),
        contentAlignment = Alignment.Center,
    ) {
        if (
            !imageUrl.isNullOrBlank()
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier =
                    Modifier
                        .size(48.dp)
                        .clip(shape),
            )
        } else {
            Text(
                text =
                    title
                        .firstOrNull()
                        ?.toString()
                        ?: if (isGroup) {
                            "群"
                        } else {
                            "聊"
                        },
                style =
                    MaterialTheme
                        .typography
                        .titleMedium,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant,
            )
        }
    }
}

private fun formatChatTime(
    value: Double?,
): String? {
    if (
        value == null ||
        value <= 0.0
    ) {
        return null
    }

    val date =
        Date(
            value.toLong()
        )

    val now =
        Date()

    val today =
        SimpleDateFormat(
            "yyyyMMdd",
            Locale.getDefault(),
        )

    if (
        today.format(date) ==
        today.format(now)
    ) {
        return SimpleDateFormat(
            "HH:mm",
            Locale.getDefault(),
        ).format(date)
    }

    val yesterday =
        Date(
            now.time -
                    24L *
                    60L *
                    60L *
                    1000L
        )

    if (
        today.format(date) ==
        today.format(yesterday)
    ) {
        return "昨天"
    }

    return SimpleDateFormat(
        "MM/dd",
        Locale.getDefault(),
    ).format(date)
}