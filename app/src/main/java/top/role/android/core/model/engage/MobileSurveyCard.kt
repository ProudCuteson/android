package top.role.android.core.model.engage

import kotlinx.serialization.Serializable

@Serializable
data class MobileSurveyCard(
    val id: String,

    val title: String,
    val description: String = "",
    val coverImageUrl: String = "",

    val responseKind: String,

    val views: Double = 0.0,
    val responses: Double = 0.0,

    val publishedAt: Double = 0.0,
    val updatedAt: Double = 0.0,

    val featured: Boolean = false,
)