package top.role.android.feature.chat.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

import top.role.android.feature.chat.friends.ChatFriendsRepository

class ChatGroupCreateViewModelFactory(
    private val friendsRepository:
    ChatFriendsRepository,

    private val repository:
    ChatGroupRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
    ): T {

        if (
            modelClass.isAssignableFrom(
                ChatGroupCreateViewModel::class.java
            )
        ) {
            return ChatGroupCreateViewModel(
                friendsRepository =
                    friendsRepository,

                repository =
                    repository,
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}