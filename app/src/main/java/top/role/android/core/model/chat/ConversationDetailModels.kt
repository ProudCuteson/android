package top.role.android.core.model.chat

import kotlinx.serialization.Serializable

@Serializable
data class MobileConversationMember(
    val userId: String,
    val role: String? = null,
    val joinedAt: Double? = null,
    val user: MobileChatUserMeta? = null,
)

@Serializable
data class MobileConversationDetail(
    val conversationId: String,

    val kind: MobileConversationKind,

    val contextType: String? = null,
    val contextId: String? = null,

    val title: String? = null,
    val avatarUrl: String? = null,
    val directUser: MobileChatUserMeta? = null,
    val members: List<MobileConversationMember> = emptyList(),

    val lastMessageAt: Double? = null,
    val lastMessageText: String? = null,
    val lastSenderId: String? = null,

    val lastReadAt: Double = 0.0,
    val lastOpenedAt: Double? = null,

    val unread: Boolean = false,
    val unreadCount: Double = 0.0,

    val pinnedAt: Double? = null,
    val muteUntil: Double? = null,
    val notificationLevel: String = "all",

    val createdAt: Double,
    val updatedAt: Double,
) {
    val displayTitle: String
        get() = title
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: directUser
                ?.displayName
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
            ?: if (kind == MobileConversationKind.Group) {
                "未命名群聊"
            } else {
                "聊天"
            }

    val displayAvatarUrl: String?
        get() = avatarUrl
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: directUser
                ?.avatarUrl
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
}