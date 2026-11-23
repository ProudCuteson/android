package top.role.android.feature.content.picker

import kotlinx.coroutines.flow.first
import top.role.android.core.convex.ConvexMobileClient

class ContentPickerRepository(
    private val convexClient: ConvexMobileClient,
) {
    suspend fun loadPage(
        type: String,
        query: String = "",
        cursor: String? = null,
        pageSize: Int = 20,
    ): ContentPickerPageResponse {

        val args = mapOf<String, Any?>(
            "type" to type,
            "query" to query.trim(),
            "paginationOpts" to mapOf(
                "numItems" to pageSize.toDouble(),
                "cursor" to cursor,
            ),
        )

        return convexClient
            .subscribe<ContentPickerPageResponse>(
                name = "content/picker:listSelectableContent",
                args = args,
            )
            .first()
            .getOrThrow()
    }
}
