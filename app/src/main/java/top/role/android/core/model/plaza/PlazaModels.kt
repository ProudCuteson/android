package top.role.android.core.model.plaza

import kotlinx.serialization.Serializable

@Serializable
data class PlazaProfileInfo(
    val profileId: String? = null,
    val displayName: String? = null,
    val bio: String? = null,
    val avatarUrl: String? = null,
    val websiteUrl: String? = null,
)

@Serializable
data class MiniEntityCard(
    val id: String,
    val type: String,
    val title: String,
    val subtitle: String,
    val entityLabel: String? = null,
    val cover: String? = null,
    val isFollowing: Boolean? = null,
    val tags: List<String> = emptyList(),
    val number: Double? = null,
    val profile: PlazaProfileInfo? = null,
)

@Serializable
data class MiniPublishedItem(
    val id: String,
    val convexId: String,
    val publishId: String,

    val type: String,
    val displayType: String,
    val typeLabel: String,

    val title: String,
    val summary: String,
    val cover: String? = null,

    val spaceId: String,
    val spaceNumber: Double? = null,
    val spaceName: String? = null,

    val accessLevel: String,
    val locked: Boolean,
    val canOpen: Boolean,
    val lockReason: String? = null,

    val ownerUserId: String,

    val accessSource: String,

    val topicId: String? = null,
    val topicPublishId: String? = null,
    val topicTitle: String? = null,

    val tags: List<String> = emptyList(),

    val featured: Boolean,
    val timeLabel: String,
    val updatedAt: Double,
)

@Serializable
data class MiniTopicListItem(
    val id: String,
    val convexId: String,
    val publishId: String,

    val type: String,
    val displayType: String,
    val typeLabel: String,

    val title: String,
    val summary: String,
    val cover: String? = null,

    val spaceId: String,
    val spaceNumber: Double? = null,
    val spaceName: String? = null,

    val accessLevel: String,
    val locked: Boolean,
    val canOpen: Boolean,
    val lockReason: String? = null,

    val ownerUserId: String,

    val accessSource: String,

    val topicId: String? = null,
    val topicPublishId: String? = null,
    val topicTitle: String? = null,

    val tags: List<String> = emptyList(),

    val featured: Boolean,
    val timeLabel: String,
    val updatedAt: Double,

    val itemCount: Double? = null,
)

@Serializable
data class SharedMobilePlazaPage(
    val featuredItems: List<MiniEntityCard> = emptyList(),
    val followingItems: List<MiniEntityCard> = emptyList(),
    val latestItems: List<MiniEntityCard> = emptyList(),

    val publicDocs: List<MiniPublishedItem> = emptyList(),
    val publicTopics: List<MiniTopicListItem> = emptyList(),
)