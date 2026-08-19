package top.role.android.feature.chat.group.setting

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

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import coil3.compose.AsyncImage

import top.role.android.core.model.chat.MobileChatUserMeta
import top.role.android.core.model.chat.MobileConversationMember

@Composable
fun ChatGroupSettingsScreen(
    state:
    ChatGroupSettingsUiState,

    onUpdateTitle:
        (String) -> Unit,

    onAddMembers:
        (List<String>) -> Unit,

    onRemoveMember:
        (String) -> Unit,

    onTransferOwner:
        (String) -> Unit,

    onLeave:
        () -> Unit,
) {
    val conversation =
        state.conversation

    if (
        state.loading &&
        conversation == null
    ) {
        Box(
            modifier =
                Modifier.fillMaxSize(),

            contentAlignment =
                Alignment.Center,
        ) {
            CircularProgressIndicator()
        }

        return
    }

    if (
        conversation == null
    ) {
        Box(
            modifier =
                Modifier.fillMaxSize(),

            contentAlignment =
                Alignment.Center,
        ) {
            Text(
                text =
                    state.errorMessage
                        ?: "群聊不存在",
            )
        }

        return
    }

    var showRename by
    remember {
        mutableStateOf(false)
    }

    var showAddMembers by
    remember {
        mutableStateOf(false)
    }

    var leaveConfirm by
    remember {
        mutableStateOf(false)
    }

    LazyColumn(
        modifier =
            Modifier.fillMaxSize(),
    ) {

        item {

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            16.dp
                        ),

                verticalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    ),
            ) {

                Text(
                    text =
                        conversation.displayTitle,

                    style =
                        MaterialTheme
                            .typography
                            .titleLarge,

                    fontWeight =
                        FontWeight.SemiBold,
                )

                Text(
                    text =
                        "${conversation.members.size} 位成员",

                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,
                )

                if (
                    state.isOwner
                ) {

                    TextButton(
                        onClick = {
                            showRename =
                                true
                        },
                    ) {
                        Text(
                            "修改群名称"
                        )
                    }

                    TextButton(
                        onClick = {
                            showAddMembers =
                                true
                        },
                    ) {
                        Text(
                            "添加成员"
                        )
                    }
                }
            }
        }

        item {
            HorizontalDivider()
        }

        item {

            Text(
                text =
                    "群成员",

                style =
                    MaterialTheme
                        .typography
                        .titleSmall,

                modifier =
                    Modifier.padding(
                        horizontal =
                            16.dp,

                        vertical =
                            12.dp,
                    ),
            )
        }

        items(
            items =
                conversation.members,

            key = {
                it.userId
            },
        ) { member ->

            ChatGroupMemberRow(
                member =
                    member,

                currentUserId =
                    state.currentUserId,

                isCurrentUserOwner =
                    state.isOwner,

                processing =
                    state.processing,

                onRemove = {
                    onRemoveMember(
                        member.userId
                    )
                },

                onTransferOwner = {
                    onTransferOwner(
                        member.userId
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

                    modifier =
                        Modifier.padding(
                            16.dp
                        ),
                )
            }
        }

        item {

            Button(
                onClick = {
                    leaveConfirm =
                        true
                },

                enabled =
                    !state.processing,

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            16.dp
                        ),
            ) {
                Text(
                    if (
                        state.isOwner
                    ) {
                        "退出群聊"
                    } else {
                        "退出群聊"
                    }
                )
            }
        }
    }

    if (
        showRename
    ) {

        RenameGroupDialog(
            initialTitle =
                conversation.displayTitle,

            onDismiss = {
                showRename =
                    false
            },

            onConfirm = { title ->

                showRename =
                    false

                onUpdateTitle(
                    title
                )
            },
        )
    }

    if (
        showAddMembers
    ) {

        AddGroupMembersDialog(
            friends =
                state.availableFriends,

            onDismiss = {
                showAddMembers =
                    false
            },

            onConfirm = { userIds ->

                showAddMembers =
                    false

                onAddMembers(
                    userIds
                )
            },
        )
    }

    if (
        leaveConfirm
    ) {

        AlertDialog(
            onDismissRequest = {
                leaveConfirm =
                    false
            },

            title = {
                Text(
                    "退出群聊"
                )
            },

            text = {
                Text(
                    if (
                        state.isOwner
                    ) {
                        "群主需要先将群主转让给其他成员，才能退出群聊。"
                    } else {
                        "确定退出这个群聊吗？"
                    }
                )
            },

            confirmButton = {

                if (
                    !state.isOwner
                ) {
                    TextButton(
                        onClick = {
                            leaveConfirm =
                                false

                            onLeave()
                        },
                    ) {
                        Text(
                            "退出"
                        )
                    }
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        leaveConfirm =
                            false
                    },
                ) {
                    Text(
                        "取消"
                    )
                }
            },
        )
    }
}

@Composable
private fun ChatGroupMemberRow(
    member:
    MobileConversationMember,

    currentUserId:
    String?,

    isCurrentUserOwner:
    Boolean,

    processing:
    Boolean,

    onRemove:
        () -> Unit,

    onTransferOwner:
        () -> Unit,
) {
    val user =
        member.user

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal =
                        16.dp,

                    vertical =
                        10.dp,
                ),

        verticalAlignment =
            Alignment.CenterVertically,
    ) {

        ChatGroupMemberAvatar(
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
                    user?.displayName
                        ?: "用户",

                fontWeight =
                    FontWeight.Medium,
            )

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(
                        6.dp
                    ),
            ) {

                if (
                    member.role == "owner"
                ) {
                    Text(
                        text =
                            "群主",

                        style =
                            MaterialTheme
                                .typography
                                .bodySmall,
                    )
                }

                if (
                    member.userId ==
                    currentUserId
                ) {
                    Text(
                        text =
                            "我",

                        style =
                            MaterialTheme
                                .typography
                                .bodySmall,
                    )
                }
            }
        }

        if (
            isCurrentUserOwner &&
            member.userId !=
            currentUserId &&
            member.role != "owner"
        ) {

            Column(
                horizontalAlignment =
                    Alignment.End,
            ) {

                TextButton(
                    onClick =
                        onTransferOwner,

                    enabled =
                        !processing,
                ) {
                    Text(
                        "设为群主"
                    )
                }

                TextButton(
                    onClick =
                        onRemove,

                    enabled =
                        !processing,
                ) {
                    Text(
                        "移除"
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatGroupMemberAvatar(
    user:
    MobileChatUserMeta?,
) {
    val avatarUrl =
        user
            ?.avatarUrl
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
private fun RenameGroupDialog(
    initialTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var title by
    remember(
        initialTitle
    ) {
        mutableStateOf(
            initialTitle
        )
    }

    AlertDialog(
        onDismissRequest =
            onDismiss,

        title = {
            Text(
                "修改群名称"
            )
        },

        text = {

            OutlinedTextField(
                value =
                    title,

                onValueChange = {
                    title =
                        it
                },

                singleLine =
                    true,

                label = {
                    Text(
                        "群名称"
                    )
                },
            )
        },

        confirmButton = {

            TextButton(
                onClick = {
                    onConfirm(
                        title
                    )
                },

                enabled =
                    title.isNotBlank(),
            ) {
                Text(
                    "保存"
                )
            }
        },

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
private fun AddGroupMembersDialog(
    friends:
    List<top.role.android.core.model.chat.MobileFriendItem>,

    onDismiss:
        () -> Unit,

    onConfirm:
        (List<String>) -> Unit,
) {
    var selected by
    remember {
        mutableStateOf(
            emptySet<String>()
        )
    }

    AlertDialog(
        onDismissRequest =
            onDismiss,

        title = {
            Text(
                "添加成员"
            )
        },

        text = {

            if (
                friends.isEmpty()
            ) {

                Text(
                    "没有可添加的好友"
                )

            } else {

                LazyColumn {

                    items(
                        items =
                            friends,

                        key = {
                            it.user.userId
                        },
                    ) { friend ->

                        val userId =
                            friend.user.userId

                        val checked =
                            userId in selected

                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {

                                        selected =
                                            if (
                                                checked
                                            ) {
                                                selected -
                                                        userId
                                            } else {
                                                selected +
                                                        userId
                                            }
                                    }
                                    .padding(
                                        vertical =
                                            8.dp
                                    ),

                            verticalAlignment =
                                Alignment.CenterVertically,
                        ) {

                            Text(
                                text =
                                    friend
                                        .user
                                        .displayName,

                                modifier =
                                    Modifier.weight(
                                        1f
                                    ),
                            )

                            androidx.compose.material3.Checkbox(
                                checked =
                                    checked,

                                onCheckedChange = {

                                    selected =
                                        if (
                                            checked
                                        ) {
                                            selected -
                                                    userId
                                        } else {
                                            selected +
                                                    userId
                                        }
                                },
                            )
                        }
                    }
                }
            }
        },

        confirmButton = {

            TextButton(
                onClick = {
                    onConfirm(
                        selected.toList()
                    )
                },

                enabled =
                    selected.isNotEmpty(),
            ) {
                Text(
                    "添加"
                )
            }
        },

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