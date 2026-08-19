package top.role.android.core.model.chat

import kotlinx.serialization.Serializable

@Serializable
data class MobileDirectConversationResult(
    val ok: Boolean,
    val conversationId: String,
    val created: Boolean,
)