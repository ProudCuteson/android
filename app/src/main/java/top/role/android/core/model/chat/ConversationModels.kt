package top.role.android.core.model.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class MobileConversationKind {
    @SerialName("direct")
    Direct,

    @SerialName("group")
    Group,
}

@Serializable
data class MobileChatUserMeta(
    val userId: String,
    val number: Double = 0.0,
    val displayName: String,
    val avatarUrl: String? = null,

    val email: String? = null,
    val phone: String? = null,

    val hasEmail: Boolean = false,
    val hasPhone: Boolean = false,
    val emailPublic: Boolean = false,
    val phonePublic: Boolean = false,
)

@Serializable
data class MobileConversationListItem(
    val conversationId: String,

    val kind: MobileConversationKind,

    val contextType: String? = null,
    val contextId: String? = null,

    val title: String? = null,
    val avatarUrl: String? = null,
    val directUser: MobileChatUserMeta? = null,

    val memberCount: Double = 0.0,

    val lastMessageAt: Double? = null,
    val lastMessageText: String? = null,
    val lastSenderId: String? = null,

    val lastReadAt: Double = 0.0,

    val unread: Boolean = false,
    val unreadCount: Double = 0.0,

    val pinnedAt: Double? = null,
    val muteUntil: Double? = null,
    val notificationLevel: String = "all",

    val updatedAt: Double,
) {
    val displayTitle: String
        get() = title
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: if (kind == MobileConversationKind.Group) {
                "未命名群聊"
            } else {
                directUser
                    ?.displayName
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() }
                    ?: "聊天"
            }

    val displayAvatarUrl: String?
        get() = avatarUrl
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: directUser
                ?.avatarUrl
                ?.trim()
                ?.takeIf { it.isNotEmpty() }

    val subtitle: String?
        get() = lastMessageText
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
}

@Serializable
data class MobileCreateGroupResult(
    val ok: Boolean,
    val conversationId: String,
)