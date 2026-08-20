package top.role.android.core.model.bounty

import kotlinx.serialization.Serializable

import top.role.android.core.model.engage.MobileBountyCard

@Serializable
data class MobileBountyPage(
    val page:
    List<MobileBountyCard> =
        emptyList(),

    val isDone:
    Boolean = true,

    val continueCursor:
    String = "",
)