package top.role.android.feature.reading

import kotlinx.coroutines.flow.Flow
import top.role.android.core.convex.ConvexMobileClient
import top.role.android.core.model.content.MobileDocDetailResponse

class DocDetailRepository(
    private val convexClient: ConvexMobileClient,
) {

    fun observeDocDetail(
        docId: String,
        topicId: String? = null,
    ): Flow<Result<MobileDocDetailResponse>> {

        val args =
            buildMap<String, Any?> {
                put(
                    "docId",
                    docId,
                )

                if (!topicId.isNullOrBlank()) {
                    put(
                        "topicId",
                        topicId,
                    )
                }
            }

        return convexClient.subscribe(
            name =
                "mobile/ios/doc:getMobileDocDetail",

            args = args,
        )
    }
}