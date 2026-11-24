package top.role.android.core.model.content

import kotlinx.serialization.Serializable
import top.role.android.core.model.chart.MobileChartRuntime

@Serializable
data class MobileDocInfo(
    val id: String,
    val title: String,
    val description: String? = null,
    val coverUrl: String? = null,
    val updatedAt: Double? = null,
    val publishedAt: Double? = null,
)

@Serializable
data class MobileProfileRef(
    val id: String,
    val name: String,
    val avatarUrl: String? = null,
    val subtitle: String? = null,
    val number: Double? = null,
)

@Serializable
data class MobileDocSpaceRef(
    val id: String,
    val number: Double? = null,
    val name: String? = null,
    val avatarUrl: String? = null,
)

@Serializable
data class MobileDocDetailResponse(
    val doc: MobileDocInfo,
    val userId: String,

    val author: MobileProfileRef? = null,
    val space: MobileDocSpaceRef? = null,

    val renderDoc: MobileRenderDoc,
    val charts: Map<String, MobileChartRuntime>,
)
