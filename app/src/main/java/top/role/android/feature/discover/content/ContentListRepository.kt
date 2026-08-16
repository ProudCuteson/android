package top.role.android.feature.discover.content

import kotlinx.coroutines.flow.first
import top.role.android.core.convex.ConvexMobileClient
import top.role.android.core.model.content.ContentListMode
import top.role.android.core.model.content.MobileContentListPage
import top.role.android.core.model.content.MobileContentType

class ContentListRepository(
    private val convexClient: ConvexMobileClient,
) {

    suspend fun loadPage(
        mode: ContentListMode,
        contentType: MobileContentType,
        cursor: String? = null,
        pageSize: Int = 12,
    ): Result<MobileContentListPage> {

        return convexClient
            .subscribe<MobileContentListPage>(
                name =
                    "mobile/android/content:getContentList",

                args = mapOf(
                    "mode" to
                            mode.value,

                    "contentType" to
                            contentType.value,

                    "paginationOpts" to
                            mapOf(
                                "numItems" to
                                        pageSize.toDouble(),

                                "cursor" to
                                        cursor,
                            ),
                ),
            )
            .first()
    }
}