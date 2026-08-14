package top.role.android.core.model.entity

import kotlinx.serialization.Serializable

@Serializable
data class EntityAnnouncementBlock(
    val title: String? = null,
    val subtitle: String? = null,
    val text: String? = null,
)

@Serializable
data class EntityAnnouncement(
    val style: String? = null,
    val blocks: List<EntityAnnouncementBlock> = emptyList(),
    val md: String = "",
    val updatedAt: Double,
    val updatedBy: String? = null,
)

@Serializable
data class EntityDetail(
    val id: String,
    val convexId: String,

    val entityType: String,
    val entityLabel: String,

    val title: String,
    val subtitle: String,
    val cover: String? = null,

    val number: Double? = null,
    val kind: String? = null,

    val statsLabel: String? = null,
    val tags: List<String> = emptyList(),

    val role: String? = null,
    val isMember: Boolean? = null,
)

@Serializable
data class EntityPublishedItem(
    val id: String,
    val convexId: String,
    val publishId: String,

    val type: String,
    val typeLabel: String,

    val title: String,
    val summary: String,
    val cover: String? = null,

    val accessLevel: String,

    val locked: Boolean,
    val canOpen: Boolean,
    val lockReason: String? = null,

    val accessSource: String,

    val topicId: String? = null,
    val topicPublishId: String? = null,
    val topicTitle: String? = null,

    val featured: Boolean,

    val timeLabel: String,
    val updatedAt: Double,
)

@Serializable
data class EntityEventItem(
    val id: String,
    val convexId: String,
    val title: String,
    val summary: String,
    val timeLabel: String,
)

@Serializable
data class EntityDetailResponse(
    val entity: EntityDetail? = null,
    val announcement: EntityAnnouncement? = null,
    val events: List<EntityEventItem> = emptyList(),
    val contents: List<EntityPublishedItem> = emptyList(),
)