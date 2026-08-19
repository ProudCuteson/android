package top.role.android.feature.chat.friendlookup

import top.role.android.core.model.chat.MobileFriendLookupItem

data class ChatFriendLookupUiState(
    val queryText: String = "",

    val loading: Boolean = false,

    val results:
    List<MobileFriendLookupItem> =
        emptyList(),

    val sendingUserId:
    String? = null,

    val errorMessage:
    String? = null,
)