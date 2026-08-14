package top.role.android.feature.discover

import kotlinx.coroutines.flow.Flow
import top.role.android.core.convex.ConvexMobileClient
import top.role.android.core.model.discover.MobileDiscoverPage

class DiscoverRepository(
    private val convexClient: ConvexMobileClient,
) {

    fun observeDiscover():
            Flow<Result<MobileDiscoverPage>> {

        return convexClient.subscribe(
            name =
                "mobile/android/discover:getDiscoverPage",

            args = emptyMap(),
        )
    }
}