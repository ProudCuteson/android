package top.role.android.feature.market.order.detail

import kotlinx.coroutines.flow.Flow

import top.role.android.core.convex.ConvexMobileClient
import top.role.android.core.model.market.MobileMarketOrderDetailResponse


class MarketOrderDetailRepository(
    private val convexClient: ConvexMobileClient,
) {

    fun observeOrderDetail(
        orderId: String,
    ): Flow<Result<MobileMarketOrderDetailResponse>> {

        return convexClient.subscribe(
            name =
                "mobile/native/purchase/market:getMyMarketOrderDetail",

            args =
                mapOf(
                    "orderId" to
                            orderId,
                ),
        )
    }
}