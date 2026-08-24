package top.role.android.feature.credit

import top.role.android.core.model.account.MobileRecentCreditPurchase
import top.role.android.core.model.account.MobileCreditPurchaseOption
import top.role.android.core.model.account.MobileCustomCreditPurchaseOption

data class CreditsUiState(
    val loading: Boolean = true,

    val packs:
    List<MobileCreditPurchaseOption> =
        emptyList(),

    val custom:
    MobileCustomCreditPurchaseOption? =
        null,

    val customAmountText:
    String = "",

    val recentPurchases:
    List<MobileRecentCreditPurchase> =
        emptyList(),

    val recentLoading:
    Boolean = true,

    val purchasingPackKey:
    String? = null,

    val paying:
    Boolean = false,

    val confirming:
    Boolean = false,

    val paymentSuccess:
    Boolean = false,

    val purchaseErrorMessage:
    String? = null,

    val errorMessage:
    String? = null,
)