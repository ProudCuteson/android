package top.role.android.feature.chat.conversationlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ChatConversationListViewModelFactory(
    private val repository: ChatConversationListRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
    ): T {
        if (
            modelClass.isAssignableFrom(
                ChatConversationListViewModel::class.java
            )
        ) {
            return ChatConversationListViewModel(
                repository = repository,
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}