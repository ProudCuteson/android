package top.role.android.feature.bounty

import kotlinx.coroutines.flow.Flow

import top.role.android.core.convex.ConvexMobileClient
import top.role.android.core.model.bounty.MobileBountyListMode
import top.role.android.core.model.bounty.MobileBountyPage

class BountyListRepository(
    private val convexClient:
    ConvexMobileClient,
) {

    fun observePage(
        mode:
        MobileBountyListMode,

        cursor:
        String? = null,

        numItems:
        Double = 12.0,
    ): Flow<Result<MobileBountyPage>> {

        return convexClient.subscribe(
            name =
                "mobile/native/bounty/index:listMobileBounties",

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