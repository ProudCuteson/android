package top.role.android.feature.entity

import kotlinx.coroutines.flow.Flow
import top.role.android.core.convex.ConvexMobileClient
import top.role.android.core.model.entity.EntityDetailResponse

class EntityDetailRepository(
    private val convexClient: ConvexMobileClient,
) {

    fun observeSpaceDetail(
        id: String,
    ): Flow<Result<EntityDetailResponse>> {

        return convexClient.subscribe(
            name =
                "mobile/native/space/get:getMobileEntityDetail",

            args = mapOf(
                "type" to "space",
                "id" to id,

                "contentLimit" to 12.0,
                "eventsLimit" to 3.0,
            ),
        )
    }
}