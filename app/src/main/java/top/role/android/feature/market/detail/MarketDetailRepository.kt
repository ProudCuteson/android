package top.role.android.feature.market.detail

import kotlinx.coroutines.flow.Flow

import top.role.android.core.convex.ConvexMobileClient
import top.role.android.core.model.market.MobileMarketDetailResponse


class MarketDetailRepository(
    private val convexClient:
    ConvexMobileClient,
) {

    fun observeDetail(
        listingId:
        String,
    ): Flow<
            Result<MobileMarketDetailResponse>
            > {
        return convexClient.subscribe(
            name =
                "mobile/native/market/index:getMobileMarketListingDetail",

            args =
                mapOf(
                    "listingId" to
                            listingId,
                ),
        )
    }
}