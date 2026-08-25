package top.role.android.feature.topic

import kotlinx.coroutines.flow.Flow
import top.role.android.core.convex.ConvexMobileClient
import top.role.android.core.model.topic.TopicDetailResponse

class TopicDetailRepository(
    private val convexClient: ConvexMobileClient,
) {

    fun observeTopicDetail(
        topicId: String,
        cursor: String? = null,
        numItems: Int = 12,
    ): Flow<Result<TopicDetailResponse>> {

        return convexClient.subscribe(
            name =
                "mobile/native/topic/index:getTopicDetail",

            args = mapOf(
                "topicId" to topicId,

                "paginationOpts" to mapOf(
                    "cursor" to cursor,
                    "numItems" to numItems.toDouble(),
                ),

                "limit" to 12.0,
            ),
        )
    }
}