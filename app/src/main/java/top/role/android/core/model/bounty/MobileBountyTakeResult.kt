package top.role.android.core.model.bounty

import kotlinx.serialization.Serializable

@Serializable
data class MobileBountyTakeResult(
    val ok: Boolean,

    val code: String? = null,

    val takeId: String? = null,

    val status: String? = null,

    val message: String? = null,
)