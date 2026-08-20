package top.role.android.core.model.market

import kotlinx.serialization.Serializable


@Serializable
data class MobileMarketDetailResponse(
    val ok:
    Boolean,

    val code:
    String? = null,

    val listing:
    MobileMarketDetail? = null,
)


@Serializable
data class MobileMarketDetail(
    val id:
    String,

    val productId:
    String,

    val title:
    String,

    val subtitle:
    String = "",

    val description:
    String = "",

    val cover:
    String = "",

    val images:
    List<String> =
        emptyList(),

    val kind:
    String,

    val kindLabel:
    String = "",

    val priceCents:
    Double = 0.0,

    val currency:
    String,

    val price:
    String = "",

    val feeEnabled:
    Boolean = false,

    val feeCents:
    Double = 0.0,

    val feeLabel:
    String = "",

    val feeText:
    String = "",

    val allowMultiQuantity:
    Boolean = false,

    val needsShippingAddress:
    Boolean = false,

    val stockMode:
    String,

    val stockCount:
    Double? = null,

    val sellerSpace:
    MobileMarketSellerSpace,

    val platformFeatured:
    Boolean = false,

    val createdAt:
    Double = 0.0,

    val updatedAt:
    Double = 0.0,
)


@Serializable
data class MobileMarketSellerSpace(
    val id:
    String,

    val number:
    Double,

    val name:
    String,

    val avatarUrl:
    String? = null,

    val description:
    String? = null,
)