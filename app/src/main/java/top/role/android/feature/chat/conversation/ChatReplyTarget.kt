package top.role.android.feature.chat.conversation

data class ChatReplyTarget(
    val messageId: String,
    val senderName: String,
    val kind: String,
    val text: String? = null,
    val imageId: String? = null,
)