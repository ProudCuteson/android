package top.role.android.core.model.market

import kotlinx.serialization.Serializable


/*
 * ------------------------------------------------
 * Order List
 * ------------------------------------------------
 */

@Serializable
data class MobileMarketOrderListResponse(
    val ok: Boolean = false,

    val items: List<MobileMarketOrderCard> =
        emptyList(),

    val pagination: MobileMarketOrderPagination =
        MobileMarketOrderPagination(),
)


@Serializable
data class MobileMarketOrderPagination(
    val offset: Double = 0.0,
    val limit: Double = 12.0,

    val nextOffset: Double? = null,

    val hasMore: Boolean = false,
)


@Serializable
data class MobileMarketOrderCard(
    val id: String = "",

    val listingId: String = "",

    val title: String = "",
    val subtitle: String? = null,

    val cover: String? = null,

    val kind: String = "",

    val quantity: Double = 1.0,

    val totalPriceCents: Double = 0.0,
    val currency: String = "CNY",

    /*
     * 后端已经格式化好的展示价格。
     */
    val totalPrice: String = "",

    /*
     * marketOrders.status
     *
     * pending
     * paid
     * cancelled
     * refunded
     */
    val status: String = "",

    /*
     * 后端结合 order + fulfillment
     * 已生成展示文案。
     */
    val statusLabel: String = "",

    val fulfillmentStatus: String? = null,
    val deliveryKind: String? = null,

    val outTradeNo: String = "",

    val dueAt: Double? = null,
    val paidAt: Double? = null,

    val createdAt: Double = 0.0,
    val updatedAt: Double = 0.0,
)