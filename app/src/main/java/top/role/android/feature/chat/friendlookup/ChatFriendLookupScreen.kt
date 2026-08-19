package top.role.android.feature.chat.friendlookup

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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person

import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions

import coil3.compose.AsyncImage

import top.role.android.core.model.chat.MobileChatUserMeta
import top.role.android.core.model.chat.MobileFriendLookupItem
import top.role.android.core.model.chat.MobileFriendRelation

@Composable
fun ChatFriendLookupScreen(
    state:
    ChatFriendLookupUiState,

    onQueryChange:
        (String) -> Unit,

    onClear:
        () -> Unit,

    onSearch:
        () -> Unit,

    onAdd:
        (String) -> Unit,

    onOpenChat:
        (String) -> Unit,
) {
    Column(
        modifier =
            Modifier.fillMaxSize(),
    ) {

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 8.dp,
                    ),

            verticalAlignment =
                Alignment.CenterVertically,

            horizontalArrangement =
                Arrangement.spacedBy(
                    8.dp
                ),
        ) {

            OutlinedTextField(
                value =
                    state.queryText,

                onValueChange =
                    onQueryChange,

                modifier =
                    Modifier.weight(1f),

                placeholder = {
                    Text(
                        "搜索用户编号或名称"
                    )
                },

                singleLine =
                    true,

                keyboardOptions =
                    KeyboardOptions(
                        imeAction =
                            ImeAction.Search,
                    ),

                keyboardActions =
                    KeyboardActions(
                        onSearch = {
                            onSearch()
                        },
                    ),

                trailingIcon = {
                    if (
                        state
                            .queryText
                            .isNotEmpty()
                    ) {
                        IconButton(
                            onClick =
                                onClear,
                        ) {
                            Icon(
                                imageVector =
                                    Icons.Default.Clear,

                                contentDescription =
                                    "清空",
                            )
                        }
                    }
                },
            )

            Button(
                onClick =
                    onSearch,

                enabled =
                    state
                        .queryText
                        .isNotBlank() &&
                            !state.loading,
            ) {
                Text(
                    "搜索"
                )
            }
        }

        when {
            state.loading -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                24.dp
                            ),

                    contentAlignment =
                        Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            state.results.isEmpty() &&
                    state.queryText
                        .isNotBlank() -> {

                Text(
                    text =
                        "没有找到相关用户",

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
                            16.dp
                        ),
                )
            }

            else -> {
                LazyColumn(
                    modifier =
                        Modifier.fillMaxSize(),
                ) {

                    items(
                        items =
                            state.results,

                        key = {
                            it.user.userId
                        },
                    ) { item ->

                        ChatFriendLookupRow(
                            item =
                                item,

                            sending =
                                state.sendingUserId ==
                                        item.user.userId,

                            onAdd = {
                                onAdd(
                                    item.user.userId
                                )
                            },

                            onOpenChat = {
                                onOpenChat(
                                    item.user.userId
                                )
                            },
                        )

                        HorizontalDivider()
                    }

                    if (
                        state.errorMessage != null
                    ) {
                        item {
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
                                        16.dp
                                    ),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatFriendLookupRow(
    item:
    MobileFriendLookupItem,

    sending:
    Boolean,

    onAdd:
        () -> Unit,

    onOpenChat:
        () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 10.dp,
                ),

        verticalAlignment =
            Alignment.CenterVertically,
    ) {

        ChatLookupAvatar(
            user =
                item.user,
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
                    item.user.displayName,

                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,

                fontWeight =
                    FontWeight.SemiBold,

                maxLines = 1,

                overflow =
                    TextOverflow.Ellipsis,
            )

            if (
                item.user.number > 0
            ) {
                Text(
                    text =
                        "U${item.user.number.toLong()}",

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
            when (
                item.relation
            ) {
                MobileFriendRelation.Self -> {
                    Text(
                        text =
                            "自己",

                        style =
                            MaterialTheme
                                .typography
                                .bodySmall,
                    )
                }

                MobileFriendRelation.Friend -> {
                    TextButton(
                        onClick =
                            onOpenChat,
                    ) {
                        Text(
                            "聊天"
                        )
                    }
                }

                MobileFriendRelation.IncomingPending -> {
                    Text(
                        text =
                            "待处理",

                        style =
                            MaterialTheme
                                .typography
                                .bodySmall,
                    )
                }

                MobileFriendRelation.OutgoingPending -> {
                    Text(
                        text =
                            "已申请",

                        style =
                            MaterialTheme
                                .typography
                                .bodySmall,
                    )
                }

                MobileFriendRelation.None -> {
                    TextButton(
                        onClick =
                            onAdd,
                    ) {
                        Text(
                            "添加"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatLookupAvatar(
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