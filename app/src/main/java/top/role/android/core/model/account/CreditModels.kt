package top.role.android.core.model.account

import kotlinx.serialization.Serializable


@Serializable
data class CreateCreditPurchaseRequest(
    val packKey:
    String,

    val customAmountMinor:
    Double? = null,
)

@Serializable
data class ContinueCreditPurchaseRequest(
    val outTradeNo: String,
)

@Serializable
data class MobileCreditPurchaseStatusResponse(
    val ok: Boolean = false,
    val found: Boolean = false,

    val billingIntentId: String = "",
    val outTradeNo: String = "",

    val status: String = "",

    val paidAt: Double? = null,
    val settledAt: Double? = null,
    val failedAt: Double? = null,

    val failureCode: String? = null,

    val dueAt: Double? = null,

    val amountMinor: Double = 0.0,
    val amountText: String? = null,
    val currency: String = "",

    val credits: Double = 0.0,

    val packKey: String? = null,
    val packTitle: String? = null,
    val discountRate: String? = null,

    val isCustom: Boolean = false,

    val paymentProvider: String? = null,
    val paymentChannel: String? = null,

    val prepayId: String? = null,

    val balanceAfter: Double? = null,

    val code: String? = null,
)

@Serializable
data class MobileCreditPurchaseOptionsResponse(
    val ok: Boolean = false,

    val packs:
    List<MobileCreditPurchaseOption> =
        emptyList(),

    val custom:
    MobileCustomCreditPurchaseOption? =
        null,
)


@Serializable
data class MobileCreditPurchaseOption(
    val packKey: String = "",
    val title: String = "",
    val currency: String = "",

    val amountMinor: Double = 0.0,
    val amountText: String = "",

    val credits: Double = 0.0,

    val discountRate: String? = null,
    val recommended: Boolean = false,
    val desc: String? = null,
)


@Serializable
data class MobileCustomCreditPurchaseOption(
    val enabled: Boolean = false,
    val title: String = "",
    val currency: String = "",

    val minAmountMinor: Double = 0.0,
    val maxAmountMinor: Double = 0.0,

    val baseRateText: String = "",
    val desc: String? = null,
)