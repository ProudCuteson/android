package top.role.android.feature.chat.group.setting

import top.role.android.core.model.chat.MobileConversationDetail
import top.role.android.core.model.chat.MobileFriendItem

data class ChatGroupSettingsUiState(
    val loading: Boolean = true,

    val conversation:
    MobileConversationDetail? = null,

    val friends:
    List<MobileFriendItem> =
        emptyList(),

    val currentUserId:
    String? = null,

    val processing: Boolean = false,

    val errorMessage:
    String? = null,
) {

    val isOwner: Boolean
        get() {
            val userId =
                currentUserId
                    ?: return false

            return conversation
                ?.members
                ?.any {
                    it.userId == userId &&
                            it.role == "owner"
                }
                ?: false
        }

    val memberUserIds: Set<String>
        get() =
            conversation
                ?.members
                ?.map {
                    it.userId
                }
                ?.toSet()
                ?: emptySet()

    val availableFriends:
            List<MobileFriendItem>
        get() =
            friends.filter {
                it.user.userId !in
                        memberUserIds
            }
}