package top.role.android.core.model.engage

import kotlinx.serialization.Serializable

@Serializable
data class MobileMarketCard(
    val id: String,

    val productId: String,
    val sellerSpaceId: String,

    val title: String,
    val subtitle: String = "",
    val cover: String = "",

    val kind: String,
    val kindLabel: String = "",

    val priceCents: Double = 0.0,
    val currency: String,
    val price: String = "",

    val stockMode: String,
    val stockCount: Double? = null,

    val sellerSpace:
    MobileMarketSellerSpace,

    val updatedAt: Double = 0.0,

    val featured: Boolean = false,
)


@Serializable
data class MobileMarketSellerSpace(
    val id: String,

    val number: Double,

    val name: String,

    val avatarUrl: String? = null,
)