package top.role.android.core.model.bounty

import kotlinx.serialization.Serializable

@Serializable
data class MobileBountyDetailResponse(
    val ok: Boolean,

    val code: String? = null,

    val bounty:
    MobileBountyDetail? = null,
)

@Serializable
data class MobileBountyDetail(
    val id: String,

    val title: String,
    val description: String = "",
    val content: String = "",

    val kind: String,
    val status: String,

    val visibility: String = "public",

    val rewardC: Double = 0.0,
    val reward: String = "",

    val allowMultipleSubmissions:
    Boolean = false,

    val deadlineAt: Double = 0.0,
    val deadlineLabel: String = "",
    val expired: Boolean = false,

    val ownedByMe: Boolean = false,
    val taken: Boolean = false,
    val takeStatus: String? = null,

    val references:
    List<MobileBountyReference> =
        emptyList(),

    val createdAt: Double = 0.0,
    val updatedAt: Double = 0.0,
)

@Serializable
data class MobileBountyReference(
    val label: String = "",
    val url: String = "",
)