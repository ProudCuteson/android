package top.role.android.feature.chat.conversationlist

import top.role.android.core.model.chat.MobileConversationListItem

sealed interface ChatConversationListUiState {
    data object Loading : ChatConversationListUiState

    data class Content(
        val conversations: List<MobileConversationListItem>,
    ) : ChatConversationListUiState

    data class Error(
        val message: String,
    ) : ChatConversationListUiState

}