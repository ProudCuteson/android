package top.role.android.core.model.topic

import kotlinx.serialization.Serializable

@Serializable
data class TopicSummary(
    val id: String,
    val convexId: String,
    val publishId: String? = null,

    val title: String,
    val summary: String,
    val cover: String? = null,

    val itemCount: Double? = null,

    val accessLevel: String,
    val locked: Boolean,
    val canOpen: Boolean,
    val lockReason: String? = null,

    val timeLabel: String? = null,
    val updatedAt: Double? = null,
)

@Serializable
data class TopicSpaceRef(
    val id: String,
    val number: Double? = null,
    val name: String,
    val avatarUrl: String? = null,
    val entityLabel: String? = null,
    val isMember: Boolean? = null,
)

@Serializable
data class TopicDocItem(
    val id: String,
    val convexId: String,
    val publishId: String? = null,

    val title: String,
    val summary: String,
    val cover: String? = null,

    val accessLevel: String,
    val locked: Boolean,
    val canOpen: Boolean,
    val lockReason: String? = null,

    val timeLabel: String,
    val updatedAt: Double,
)

@Serializable
data class TopicDetailResponse(
    val topic: TopicSummary? = null,
    val space: TopicSpaceRef? = null,
    val docs: List<TopicDocItem> = emptyList(),

    val continueCursor: String? = null,
    val isDone: Boolean = true,
)