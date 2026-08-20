package top.role.android.feature.engage

import kotlinx.coroutines.flow.Flow

import top.role.android.core.convex.ConvexMobileClient
import top.role.android.core.model.engage.MobileEngageHome

class EngageRepository(
    private val convexClient: ConvexMobileClient,
) {

    fun observeHome():
            Flow<Result<MobileEngageHome>> {
        return convexClient.subscribe(
            name =
                "mobile/native/engage/index:getMobileEngageHome",

            args =
                emptyMap(),
        )
    }

}