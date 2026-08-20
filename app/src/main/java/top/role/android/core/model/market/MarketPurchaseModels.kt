package top.role.android.core.model.market

import kotlinx.serialization.Serializable
import top.role.android.core.model.payment.WechatAppPayParams

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
    val reused: Boolean = false,
)