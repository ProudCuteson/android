package top.role.android.core.model.account

import kotlinx.serialization.Serializable
import top.role.android.core.model.payment.WechatAppPayParams

@Serializable
data class CreateCreditPurchaseResponse(
    val ok: Boolean = false,
    val purchase: CreditPurchaseCreated? = null,
    val payment: WechatAppPayParams? = null,
    val code: String? = null,
    val message: String? = null,
)

@Serializable
data class CreditPurchaseCreated(
    val billingIntentId: String = "",
    val outTradeNo: String = "",
    val amountMinor: Double = 0.0,
    val amountText: String = "",
    val currency: String = "",
    val credits: Double = 0.0,
    val pack: CreditPurchasePack? = null,
)

@Serializable
data class CreditPurchasePack(
    val packKey: String = "",
    val title: String = "",
    val amountMinor: Double = 0.0,
    val amountText: String = "",
    val currency: String = "",
    val credits: Double = 0.0,
    val discountRate: String? = null,
    val recommended: Boolean = false,
    val desc: String? = null,
    val isCustom: Boolean = false,
)