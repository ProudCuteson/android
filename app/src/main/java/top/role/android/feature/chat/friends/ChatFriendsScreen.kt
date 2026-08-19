package top.role.android.feature.chat.friends

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.foundation.shape.CircleShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person

import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

import androidx.compose.runtime.Composable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

import coil3.compose.AsyncImage

import top.role.android.core.model.chat.MobileChatUserMeta
import top.role.android.core.model.chat.MobileFriendInviteDirection
import top.role.android.core.model.chat.MobileFriendInviteItem
import top.role.android.core.model.chat.MobileFriendItem

@Composable
fun ChatFriendsScreen(
    state:
    ChatFriendsUiState,

    onAcceptInvite:
        (String) -> Unit,

    onRejectInvite:
        (String) -> Unit,

    onFriendClick:
        (String) -> Unit,

    onRetry:
        () -> Unit,
) {
    Column(
        modifier =
            Modifier.fillMaxSize(),
    ) {

        when {
            state.loading &&
                    state.friends.isEmpty() -> {

                Box(
                    modifier =
                        Modifier.fillMaxSize(),

                    contentAlignment =
                        Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            else -> {
                LazyColumn(
                    modifier =
                        Modifier.fillMaxSize(),
                ) {

                    if (
                        state.invites.isNotEmpty()
                    ) {
                        item {
                            ChatFriendsSectionTitle(
                                text =
                                    "好友申请",
                            )
                        }

                        items(
                            items =
                                state.invites,

                            key = {
                                it.inviteId
                            },
                        ) { invite ->

                            ChatFriendInviteRow(
                                item =
                                    invite,

                                processing =
                                    state
                                        .processingInviteId ==
                                            invite.inviteId,

                                onAccept = {
                                    onAcceptInvite(
                                        invite.inviteId
                                    )
                                },

                                onReject = {
                                    onRejectInvite(
                                        invite.inviteId
                                    )
                                },
                            )

                            HorizontalDivider()
                        }
                    }

                    item {
                        ChatFriendsSectionTitle(
                            text =
                                "好友",
                        )
                    }

                    if (
                        state.friends.isEmpty()
                    ) {
                        item {
                            Text(
                                text =
                                    "暂无好友",

                                style =
                                    MaterialTheme
                                        .typography
                                        .bodyMedium,

                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .onSurfaceVariant,

                                modifier =
                                    Modifier.padding(
                                        horizontal =
                                            16.dp,

                                        vertical =
                                            20.dp,
                                    ),
                            )
                        }
                    } else {
                        items(
                            items =
                                state.friends,

                            key = {
                                it.friendshipId
                            },
                        ) { friend ->

                            ChatFriendRow(
                                item =
                                    friend,

                                loading =
                                    state
                                        .openingUserId ==
                                            friend
                                                .user
                                                .userId,

                                onClick = {
                                    onFriendClick(
                                        friend
                                            .user
                                            .userId
                                    )
                                },
                            )

                            HorizontalDivider()
                        }
                    }

                    if (
                        state.errorMessage != null
                    ) {
                        item {
                            Column(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            16.dp
                                        ),

                                horizontalAlignment =
                                    Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text =
                                        state
                                            .errorMessage,

                                    color =
                                        MaterialTheme
                                            .colorScheme
                                            .error,

                                    style =
                                        MaterialTheme
                                            .typography
                                            .bodySmall,
                                )

                                TextButton(
                                    onClick =
                                        onRetry,
                                ) {
                                    Text(
                                        text =
                                            "重试",
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatFriendsSectionTitle(
    text: String,
) {
    Text(
        text =
            text,

        style =
            MaterialTheme
                .typography
                .labelLarge,

        color =
            MaterialTheme
                .colorScheme
                .onSurfaceVariant,

        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal =
                        16.dp,

                    vertical =
                        10.dp,
                ),
    )
}

@Composable
private fun ChatFriendRow(
    item:
    MobileFriendItem,

    loading:
    Boolean,

    onClick:
        () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    enabled =
                        !loading,

                    onClick =
                        onClick,
                )
                .padding(
                    horizontal =
                        16.dp,

                    vertical =
                        10.dp,
                ),

        verticalAlignment =
            Alignment.CenterVertically,
    ) {

        ChatFriendAvatar(
            user =
                item.user,
        )

        Spacer(
            modifier =
                Modifier.width(
                    12.dp
                ),
        )

        ChatFriendUserInfo(
            user =
                item.user,

            modifier =
                Modifier.weight(
                    1f
                ),
        )

        if (
            loading
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
                    Icons.Default.ChevronRight,

                contentDescription =
                    null,

                tint =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ChatFriendInviteRow(
    item:
    MobileFriendInviteItem,

    processing:
    Boolean,

    onAccept:
        () -> Unit,

    onReject:
        () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal =
                        16.dp,

                    vertical =
                        10.dp,
                ),

        verticalArrangement =
            Arrangement.spacedBy(
                8.dp
            ),
    ) {

        Row(
            verticalAlignment =
                Alignment.CenterVertically,
        ) {

            ChatFriendAvatar(
                user =
                    item.user,
            )

            Spacer(
                modifier =
                    Modifier.width(
                        12.dp
                    ),
            )

            ChatFriendUserInfo(
                user =
                    item.user,

                modifier =
                    Modifier.weight(
                        1f
                    ),
            )
        }

        item
            .message
            ?.takeIf {
                it.isNotBlank()
            }
            ?.let { message ->

                Text(
                    text =
                        message,

                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,
                )
            }

        when (
            item.direction
        ) {
            MobileFriendInviteDirection.Incoming -> {

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(
                            8.dp
                        ),

                    verticalAlignment =
                        Alignment.CenterVertically,
                ) {

                    Button(
                        onClick =
                            onAccept,

                        enabled =
                            !processing,
                    ) {
                        Text(
                            text =
                                "接受",
                        )
                    }

                    TextButton(
                        onClick =
                            onReject,

                        enabled =
                            !processing,
                    ) {
                        Text(
                            text =
                                "拒绝",
                        )
                    }

                    if (
                        processing
                    ) {
                        CircularProgressIndicator(
                            modifier =
                                Modifier.size(
                                    20.dp
                                ),

                            strokeWidth =
                                2.dp,
                        )
                    }
                }
            }

            MobileFriendInviteDirection.Outgoing -> {

                Text(
                    text =
                        "等待对方处理",

                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ChatFriendAvatar(
    user:
    MobileChatUserMeta,
) {
    val avatarUrl =
        user
            .avatarUrl
            ?.takeIf {
                it.isNotBlank()
            }

    if (
        avatarUrl != null
    ) {
        AsyncImage(
            model =
                avatarUrl,

            contentDescription =
                user.displayName,

            contentScale =
                ContentScale.Crop,

            modifier =
                Modifier
                    .size(
                        42.dp
                    )
                    .clip(
                        CircleShape
                    ),
        )
    } else {
        Box(
            modifier =
                Modifier.size(
                    42.dp
                ),

            contentAlignment =
                Alignment.Center,
        ) {
            Icon(
                imageVector =
                    Icons.Default.Person,

                contentDescription =
                    null,
            )
        }
    }
}

@Composable
private fun ChatFriendUserInfo(
    user:
    MobileChatUserMeta,

    modifier:
    Modifier = Modifier,
) {
    Column(
        modifier =
            modifier,
    ) {

        Text(
            text =
                user.displayName,

            style =
                MaterialTheme
                    .typography
                    .bodyMedium,

            fontWeight =
                FontWeight.SemiBold,

            maxLines =
                1,

            overflow =
                TextOverflow.Ellipsis,
        )

        if (
            user.number > 0
        ) {
            Text(
                text =
                    formatPublicUserNumber(
                        user.number
                    ),

                style =
                    MaterialTheme
                        .typography
                        .bodySmall,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant,
            )
        }
    }
}

private fun formatPublicUserNumber(
    value: Double,
): String {

    val number =
        value.toLong()

    return "U$number"
}