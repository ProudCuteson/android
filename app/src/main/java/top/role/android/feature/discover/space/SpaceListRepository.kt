package top.role.android.feature.discover.space

import kotlinx.coroutines.flow.first
import top.role.android.core.convex.ConvexMobileClient
import top.role.android.core.model.space.MobileSpaceListPage
import top.role.android.core.model.space.SpaceListMode

class SpaceListRepository(
    private val convexClient: ConvexMobileClient,
) {

    suspend fun loadPage(
        mode: SpaceListMode,
        cursor: String? = null,
        pageSize: Int = 12,
    ): Result<MobileSpaceListPage> {

        return convexClient
            .subscribe<MobileSpaceListPage>(
                name =
                    "mobile/android/space:getSpaceList",

                args = mapOf(
                    "mode" to mode.value,

                    "paginationOpts" to mapOf(
                        "numItems" to
                                pageSize.toDouble(),

                        "cursor" to cursor,
                    ),
                ),
            )
            .first()
    }
}