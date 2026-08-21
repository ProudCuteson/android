package top.role.android.feature.market.order

import kotlinx.coroutines.flow.Flow

import top.role.android.core.model.market.MobileMarketOrderListResponse
import top.role.android.core.convex.ConvexMobileClient

class MarketOrderListRepository(
    private val convexClient: ConvexMobileClient,
) {

    fun observeOrders(
        offset: Double = 0.0,
        limit: Double = 12.0,
    ): Flow<Result<MobileMarketOrderListResponse>> {

        return convexClient.subscribe(
            name =
                "mobile/native/purchase/market:listMyMarketOrders",

            args =
                mapOf(
                    "offset" to
                            offset,

                    "limit" to
                            limit,
                ),
        )
    }
}