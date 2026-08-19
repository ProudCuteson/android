package top.role.android.core.model.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class MobileFriendInviteDirection {
    @SerialName("incoming")
    Incoming,

    @SerialName("outgoing")
    Outgoing,
}

@Serializable
enum class MobileFriendRelation {
    @SerialName("self")
    Self,

    @SerialName("friend")
    Friend,

    @SerialName("incomingPending")
    IncomingPending,

    @SerialName("outgoingPending")
    OutgoingPending,

    @SerialName("none")
    None,
}

@Serializable
data class MobileFriendItem(
    val friendshipId: String,
    val pairKey: String,

    val createdAt: Double,

    val user: MobileChatUserMeta,
)

@Serializable
data class MobileFriendInviteItem(
    val inviteId: String,
    val pairKey: String,

    val direction: MobileFriendInviteDirection,

    val createdAt: Double,
    val updatedAt: Double,

    val message: String? = null,

    val user: MobileChatUserMeta,
)

@Serializable
data class MobileFriendLookupItem(
    val user: MobileChatUserMeta,

    val relation: MobileFriendRelation,

    val pendingInviteId: String? = null,
    val friendshipId: String? = null,
)

@Serializable
data class MobileFriendActionResult(
    val ok: Boolean,

    val friendshipId: String? = null,
    val conversationId: String? = null,

    val inviteId: String? = null,

    val status: String? = null,

    val user: MobileChatUserMeta? = null,
)