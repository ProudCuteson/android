package top.role.android.core.model.market

import kotlinx.serialization.Serializable
import top.role.android.core.model.payment.WechatAppPayParams


/*
 * ------------------------------------------------
 * Create Purchase
 * ------------------------------------------------
 */

@Serializable
data class CreateMarketPurchaseRequest(
    val listingId: String,
    val quantity: Double? = null,
    val shippingInfo: MarketShippingInfo? = null,
    val buyerNote: String? = null,
)


@Serializable
data class MarketShippingInfo(
    val receiverName: String,
    val receiverPhone: String,
    val regionText: String,
    val addressLine1: String,
    val postalCode: String? = null,
)


@Serializable
data class CreateMarketPurchaseResponse(
    val ok: Boolean = false,
    val purchase: MarketPurchaseCreated? = null,
    val payment: WechatAppPayParams? = null,
    val code: String? = null,
    val message: String? = null,
)


@Serializable
data class MarketPurchaseCreated(
    val orderId: String = "",
    val outTradeNo: String = "",

    val listingId: String = "",
    val title: String = "",

    val amountMinor: Double = 0.0,
    val currency: String = "",

    val quantity: Double = 1.0,

    /*
     * 后端如果复用了现有 pending order，
     * 这里会返回 true。
     */
    val reused: Boolean = false,
)


/*
 * ------------------------------------------------
 * Purchase Status
 * ------------------------------------------------
 */

@Serializable
data class MobileMarketPurchaseStatusResponse(
    val ok: Boolean = false,
    val code: String? = null,

    val found: Boolean = false,

    val orderId: String = "",
    val outTradeNo: String = "",

    val status: String = "",

    val listingId: String = "",

    val title: String = "",
    val cover: String? = null,

    val quantity: Double = 1.0,

    val amountMinor: Double = 0.0,
    val currency: String = "CNY",

    val paymentProvider: String? = null,
    val paymentChannel: String? = null,

    val providerTransactionId: String? = null,

    val dueAt: Double? = null,
    val paidAt: Double? = null,

    val createdAt: Double? = null,
    val updatedAt: Double? = null,

    val fulfillmentStatus: String? = null,
    val deliveryKind: String? = null,
)