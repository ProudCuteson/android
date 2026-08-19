package top.role.android.feature.chat.group

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
import androidx.compose.material.icons.filled.Person

import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text

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

@Composable
fun ChatGroupCreateScreen(
    state:
    ChatGroupCreateUiState,

    onTitleChange:
        (String) -> Unit,

    onToggleMember:
        (String) -> Unit,

    onCreate:
        () -> Unit,

    onRetry:
        () -> Unit,
) {
    Column(
        modifier =
            Modifier.fillMaxSize(),
    ) {

        OutlinedTextField(
            value =
                state.title,

            onValueChange =
                onTitleChange,

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal =
                            16.dp,

                        vertical =
                            12.dp,
                    ),

            label = {
                Text(
                    "群聊名称"
                )
            },

            placeholder = {
                Text(
                    "输入群聊名称"
                )
            },

            singleLine =
                true,
        )

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal =
                            16.dp,

                        vertical =
                            4.dp,
                    ),

            verticalAlignment =
                Alignment.CenterVertically,
        ) {

            Text(
                text =
                    "选择好友",

                style =
                    MaterialTheme
                        .typography
                        .titleSmall,
            )

            Spacer(
                modifier =
                    Modifier.weight(1f),
            )

            Text(
                text =
                    "已选择 ${state.selectedUserIds.size} 人",

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

        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
        ) {

            when {
                state.loading -> {

                    CircularProgressIndicator(
                        modifier =
                            Modifier.align(
                                Alignment.Center
                            ),
                    )
                }

                state.friends.isEmpty() -> {

                    Column(
                        modifier =
                            Modifier.align(
                                Alignment.Center
                            ),

                        horizontalAlignment =
                            Alignment.CenterHorizontally,

                        verticalArrangement =
                            Arrangement.spacedBy(
                                8.dp
                            ),
                    ) {

                        Text(
                            text =
                                "暂无好友",
                        )

                        Button(
                            onClick =
                                onRetry,
                        ) {
                            Text(
                                "重试"
                            )
                        }
                    }
                }

                else -> {

                    LazyColumn(
                        modifier =
                            Modifier.fillMaxSize(),
                    ) {

                        items(
                            items =
                                state.friends,

                            key = {
                                it.friendshipId
                            },
                        ) { friend ->

                            val selected =
                                friend
                                    .user
                                    .userId in
                                        state
                                            .selectedUserIds

                            ChatGroupFriendRow(
                                user =
                                    friend.user,

                                selected =
                                    selected,

                                onClick = {
                                    onToggleMember(
                                        friend
                                            .user
                                            .userId
                                    )
                                },
                            )

                            HorizontalDivider()
                        }
                    }
                }
            }
        }

        if (
            state.errorMessage != null
        ) {
            Text(
                text =
                    state.errorMessage,

                color =
                    MaterialTheme
                        .colorScheme
                        .error,

                style =
                    MaterialTheme
                        .typography
                        .bodySmall,

                modifier =
                    Modifier.padding(
                        horizontal =
                            16.dp,

                        vertical =
                            4.dp,
                    ),
            )
        }

        Button(
            onClick =
                onCreate,

            enabled =
                state.canCreate,

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        16.dp
                    ),
        ) {
            if (
                state.creating
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
                Text(
                    text =
                        "创建群聊",
                )
            }
        }
    }
}

@Composable
private fun ChatGroupFriendRow(
    user:
    MobileChatUserMeta,

    selected:
    Boolean,

    onClick:
        () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
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

        ChatGroupAvatar(
            user =
                user,
        )

        Spacer(
            modifier =
                Modifier.width(
                    12.dp
                ),
        )

        Column(
            modifier =
                Modifier.weight(1f),
        ) {

            Text(
                text =
                    user.displayName,

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
                        "U${user.number.toLong()}",

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

        Checkbox(
            checked =
                selected,

            onCheckedChange = {
                onClick()
            },
        )
    }
}

@Composable
private fun ChatGroupAvatar(
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