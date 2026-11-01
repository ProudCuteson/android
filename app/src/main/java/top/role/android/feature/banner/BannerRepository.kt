package top.role.android.feature.banner

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import top.role.android.core.convex.ConvexMobileClient

@Serializable
data class MobileShareCard(
    val id: String,
    val title: String,
    val text: String,
    val author: String? = null,
    val ctaLabel: String? = null,
    val ctaHref: String? = null,
)

class BannerRepository(
    private val convexClient: ConvexMobileClient,
) {

    fun observeBanner(): Flow<Result<List<MobileShareCard>>> {
        return convexClient.subscribe(
            name = "mobile/native/banner/get:getQuickStartBanner",
            args = emptyMap(),
        )
    }
}
