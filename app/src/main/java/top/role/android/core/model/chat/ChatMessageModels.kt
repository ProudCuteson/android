package top.role.android.core.model.chat

import kotlinx.serialization.Serializable

@Serializable
data class MobileChatAttachment(
    val type: String? = null,

    val imageId: String? = null,
    val thumbnailUrl: String? = null,
    val originalUrl: String? = null,

    val name: String? = null,
    val mime: String? = null,

    val size: Double? = null,
    val width: Double? = null,
    val height: Double? = null,
)

@Serializable
data class MobileChatMessage(
    val messageId: String,
    val conversationId: String,

    val senderId: String,
    val sender: MobileChatUserMeta? = null,

    val mine: Boolean = false,

    val kind: String,

    val text: String? = null,
    val attachments:
    List<MobileChatAttachment>? = null,

    val replyToMessageId: String? = null,
    val replyTo: MobileChatReplyPreview? = null,

    val createdAt: Double,

    val editedAt: Double? = null,
    val deletedAt: Double? = null,
)

@Serializable
data class MobileChatReplyPreview(
    val messageId: String,
    val senderId: String,
    val senderName: String? = null,

    val kind: String,

    val text: String? = null,
    val thumbnailUrl: String? = null,

    val deleted: Boolean = false,
)

@Serializable
data class MobileChatMessagePage(
    val page:
    List<MobileChatMessage> =
        emptyList(),

    val isDone: Boolean = true,

    val continueCursor: String = "",
)