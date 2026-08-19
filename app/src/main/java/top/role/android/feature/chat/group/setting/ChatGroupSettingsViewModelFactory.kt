package top.role.android.feature.chat.group.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

import top.role.android.feature.chat.friends.ChatFriendsRepository
import top.role.android.feature.chat.group.ChatGroupRepository

class ChatGroupSettingsViewModelFactory(
    private val conversationId:
    String,

    private val currentUserId:
    String?,

    private val groupRepository:
    ChatGroupRepository,

    private val friendsRepository:
    ChatFriendsRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
    ): T {

        if (
            modelClass.isAssignableFrom(
                ChatGroupSettingsViewModel::class.java
            )
        ) {
            return ChatGroupSettingsViewModel(
                conversationId =
                    conversationId,

                currentUserId =
                    currentUserId,

                groupRepository =
                    groupRepository,

                friendsRepository =
                    friendsRepository,
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}