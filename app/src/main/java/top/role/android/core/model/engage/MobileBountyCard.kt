package top.role.android.core.model.engage

import kotlinx.serialization.Serializable

@Serializable
data class MobileBountyCard(
    val id: String,

    val title: String,
    val description: String = "",
    val cover: String = "",

    val kind: String,
    val status: String,

    val rewardC: Double = 0.0,
    val reward: String = "",

    val deadlineAt: Double = 0.0,
    val deadlineLabel: String = "",

    val expired: Boolean = false,

    val allowMultipleSubmissions:
    Boolean = false,

    val updatedAt: Double = 0.0,

    val featured: Boolean = false,
)