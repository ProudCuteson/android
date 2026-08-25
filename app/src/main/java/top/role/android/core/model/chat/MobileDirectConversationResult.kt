package top.role.android.core.model.chat

import kotlinx.serialization.Serializable

@Serializable
data class MobileOpenFriendConversationResult(
    val ok: Boolean,
    val conversationId: String? = null,
    val friendshipId: String? = null,
    val code: String? = null,
)