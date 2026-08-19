package top.role.android.feature.chat.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ChatFriendsViewModelFactory(
    private val repository:
    ChatFriendsRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
    ): T {

        if (
            modelClass.isAssignableFrom(
                ChatFriendsViewModel::class.java
            )
        ) {
            return ChatFriendsViewModel(
                repository =
                    repository,
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}