package top.role.android.core.model.chat

import kotlinx.serialization.Serializable

@Serializable
data class MobileGroupActionResult(
    val ok: Boolean,
)

@Serializable
data class MobileTransferGroupOwnerResult(
    val ok: Boolean,

    val changed: Boolean = false,

    val reason: String? = null,

    val ownerId: String? = null,
)