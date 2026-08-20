package top.role.android.feature.bounty.detail

import kotlinx.coroutines.flow.Flow
import top.role.android.core.convex.ConvexMobileClient
import top.role.android.core.model.bounty.MobileBountyDetailResponse
import top.role.android.core.model.bounty.MobileBountyTakeResult

class BountyDetailRepository(
    private val convexClient:
    ConvexMobileClient,
) {

    fun observeDetail(
        bountyId: String,
    ): Flow<
            Result<MobileBountyDetailResponse>
            > {
        return convexClient.subscribe(
            name =
                "mobile/native/bounty/index:getMobileBountyDetail",

            args =
                mapOf(
                    "bountyId" to bountyId,
                ),
        )
    }

    suspend fun take(
        bountyId: String,
    ): MobileBountyTakeResult {
        return convexClient.mutation(
            name =
                "mobile/native/bounty/index:takeMobileBounty",

            args =
                mapOf(
                    "bountyId" to bountyId,
                ),
        )
    }
}