package top.role.android.core.model.market

import kotlinx.serialization.Serializable

import top.role.android.core.model.engage.MobileMarketCard

@Serializable
data class MobileMarketPage(
    val page:
    List<MobileMarketCard> =
        emptyList(),

    val isDone:
    Boolean = true,

    val continueCursor:
    String = "",
)