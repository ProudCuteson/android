package top.role.android.feature.market

import kotlinx.coroutines.flow.Flow

import top.role.android.core.convex.ConvexMobileClient
import top.role.android.core.model.market.MobileMarketListMode
import top.role.android.core.model.market.MobileMarketPage


class MarketListRepository(
    private val convexClient:
    ConvexMobileClient,
) {

    fun observePage(
        mode:
        MobileMarketListMode,

        cursor:
        String? = null,

        numItems:
        Double = 12.0,
    ): Flow<Result<MobileMarketPage>> {

        return convexClient.subscribe(
            name =
                "mobile/native/market/index:listMobileMarketListings",

            args =
                mapOf(
                    "mode" to
                            mode.value,

                    "paginationOpts" to
                            mapOf(
                                "numItems" to
                                        numItems,

                                "cursor" to
                                        cursor,
                            ),
                ),
        )
    }
}