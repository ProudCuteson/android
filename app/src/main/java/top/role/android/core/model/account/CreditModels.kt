package top.role.android.core.model.account

import kotlinx.serialization.Serializable


@Serializable
data class MobileCreditPurchaseOptionsResponse(
    val ok: Boolean = false,
    val packs: List<MobileCreditPurchaseOption> = emptyList(),
    val custom: MobileCustomCreditPurchaseOption? = null,
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