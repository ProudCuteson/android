package top.role.android.feature.chat.friends

import top.role.android.core.model.chat.MobileFriendInviteItem
import top.role.android.core.model.chat.MobileFriendItem

data class ChatFriendsUiState(
    val loading: Boolean = true,

    val friends:
    List<MobileFriendItem> =
        emptyList(),

    val invites:
    List<MobileFriendInviteItem> =
        emptyList(),

    val processingInviteId:
    String? = null,

    val openingUserId:
    String? = null,

    val errorMessage:
    String? = null,
)