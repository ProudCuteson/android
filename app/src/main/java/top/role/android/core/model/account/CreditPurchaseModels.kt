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

@Serializable
data class MobileRecentCreditPurchasesResponse(
    val ok: Boolean = false,

    val items:
    List<MobileRecentCreditPurchase> =
        emptyList(),
)


@Serializable
data class MobileRecentCreditPurchase(
    val id: String = "",
    val outTradeNo: String = "",
    val status: String = "",

    val packKey: String? = null,
    val packTitle: String? = null,

    val amountMinor: Double = 0.0,
    val amountText: String? = null,
    val currency: String = "",

    val credits: Double = 0.0,
    val discountRate: String? = null,
    val isCustom: Boolean = false,

    val paymentProvider: String? = null,
    val paymentChannel: String? = null,

    val balanceAfter: Double? = null,

    val paidAt: Double? = null,
    val settledAt: Double? = null,
    val failedAt: Double? = null,

    val failureCode: String? = null,
    val dueAt: Double? = null,

    val createdAt: Double? = null,
    val updatedAt: Double? = null,
)