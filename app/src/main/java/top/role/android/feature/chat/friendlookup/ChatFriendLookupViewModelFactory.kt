package top.role.android.feature.chat.friendlookup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

import top.role.android.feature.chat.friends.ChatFriendsRepository

class ChatFriendLookupViewModelFactory(
    private val repository:
    ChatFriendsRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
    ): T {

        if (
            modelClass.isAssignableFrom(
                ChatFriendLookupViewModel::class.java
            )
        ) {
            return ChatFriendLookupViewModel(
                repository = repository,
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}