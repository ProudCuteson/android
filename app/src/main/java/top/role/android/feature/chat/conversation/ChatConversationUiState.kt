package top.role.android.feature.chat.conversation

import top.role.android.core.model.chat.MobileChatMessage
import top.role.android.core.model.chat.MobileConversationDetail

data class ChatConversationUiState(
    val loading: Boolean = true,

    val conversation: MobileConversationDetail? = null,

    val messages: List<MobileChatMessage> = emptyList(),

    val messagesLoading: Boolean = true,

    val loadingMore: Boolean = false,
    val messagesDone: Boolean = true,
    val continueCursor: String? = null,

    val notFound: Boolean = false,

    val errorMessage: String? = null,

    val inputText: String = "",

    val sending: Boolean = false,
    val sendErrorMessage: String? = null,

    val replyTarget: ChatReplyTarget? = null,

    val imageDraft: ChatImageDraft? = null,
    val imageSending: Boolean = false,
    val imageSendErrorMessage: String? = null,

    val operatingMessageId: String? = null,
    val messageOperationError: String? = null,
)