package top.role.android.feature.chat.conversation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import top.role.android.core.data.image.ImageRepository

class ChatConversationViewModelFactory(
    private val conversationId: String,
    private val repository: ChatConversationRepository,
    private val imageRepository: ImageRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
    ): T {
        if (
            modelClass.isAssignableFrom(
                ChatConversationViewModel::class.java
            )
        ) {
            return ChatConversationViewModel(
                conversationId = conversationId,
                repository = repository,
                imageRepository = imageRepository,
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}