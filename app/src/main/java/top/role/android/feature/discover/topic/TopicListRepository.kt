package top.role.android.feature.discover.topic

import kotlinx.coroutines.flow.first
import top.role.android.core.convex.ConvexMobileClient
import top.role.android.core.model.topic.MobileTopicListPage
import top.role.android.core.model.topic.TopicListMode

class TopicListRepository(
    private val convexClient: ConvexMobileClient,
) {

    suspend fun loadPage(
        mode: TopicListMode,
        cursor: String? = null,
        pageSize: Int = 12,
    ): Result<MobileTopicListPage> {

        return convexClient
            .subscribe<MobileTopicListPage>(
                name =
                    "mobile/android/topic:getTopicList",

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