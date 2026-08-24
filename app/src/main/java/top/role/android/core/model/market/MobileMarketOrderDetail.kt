package top.role.android.core.model.market

import kotlinx.serialization.Serializable


@Serializable
data class MobileMarketOrderDetailResponse(
    val ok: Boolean = false,

    val order: MobileMarketOrderDetail? = null,

    val code: String? = null,
)


@Serializable
data class MobileMarketOrderDetail(
    val id: String = "",

    val listingId: String = "",
    val productId: String = "",
    val sellerSpaceId: String = "",

    val title: String = "",
    val subtitle: String? = null,
    val cover: String? = null,

    val kind: String = "",

    val quantity: Double = 1.0,

    val unitPriceCents: Double = 0.0,
    val totalPriceCents: Double = 0.0,

    val currency: String = "CNY",

    val totalPrice: String = "",

    val feeEnabled: Boolean = false,
    val feeKind: String? = null,
    val feeCents: Double? = null,
    val feeLabel: String? = null,

    val additionFeeKind: String? = null,
    val additionFeeCents: Double? = null,

    val status: String = "",
    val statusLabel: String = "",

    val shippingInfo: MobileMarketOrderShippingInfo? = null,

    val buyerNote: String? = null,

    val outTradeNo: String? = null,

    val paymentProvider: String? = null,
    val paymentChannel: String? = null,
    val providerTransactionId: String? = null,

    val dueAt: Double? = null,
    val paidAt: Double? = null,

    val createdAt: Double = 0.0,
    val updatedAt: Double = 0.0,

    val sellerSpace: MobileMarketOrderSellerSpace? = null,

    val fulfillment: MobileMarketOrderFulfillment? = null,
)


@Serializable
data class MobileMarketOrderShippingInfo(
    val receiverName: String = "",
    val receiverPhone: String = "",

    val regionText: String = "",
    val addressLine1: String = "",

    val postalCode: String? = null,
)


@Serializable
data class MobileMarketOrderSellerSpace(
    val id: String = "",

    val name: String = "",

    val number: Double? = null,

    val avatarUrl: String? = null,
)


@Serializable
data class MobileMarketOrderFulfillment(
    val status: String? = null,

    val deliveryKind: String? = null,

    val sellerMessage: String? = null,

    val deliveryTrackingCompany: String? = null,
    val deliveryTrackingNo: String? = null,
    val deliveryTrackingUrl: String? = null,

    val deliveryContent: String? = null,

    val shippedAt: Double? = null,
    val receivedAt: Double? = null,
    val completedAt: Double? = null,

    val updatedAt: Double? = null,
)