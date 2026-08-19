package top.role.android.feature.chat.group

import top.role.android.core.model.chat.MobileFriendItem

data class ChatGroupCreateUiState(
    val loading: Boolean = true,

    val friends:
    List<MobileFriendItem> =
        emptyList(),

    val title: String = "",

    val selectedUserIds:
    Set<String> =
        emptySet(),

    val creating: Boolean = false,

    val errorMessage: String? = null,
) {

    val canCreate: Boolean
        get() =
            title.isNotBlank() &&
                    selectedUserIds.isNotEmpty() &&
                    !creating
}