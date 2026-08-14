package top.role.android.core.model.topic

import kotlinx.serialization.Serializable

@Serializable
data class MobileTopicSpaceRef(
    val id: String,
    val number: Double? = null,
    val name: String? = null,
)

@Serializable
data class MobileTopicCard(
    val id: String,
    val publishId: String,

    val type: String,

    val title: String,
    val description: String,

    val cover: String? = null,

    val space: MobileTopicSpaceRef,

    val ownerUserId: String,

    val accessLevel: String,

    val canOpen: Boolean,

    val lockReason: String? = null,

    val tags: List<String> = emptyList(),

    val following: Boolean = false,

    val featured: Boolean = false,

    val itemCount: Double,

    val updatedAt: Double,
)

@Serializable
data class MobileTopicListPage(
    val page: List<MobileTopicCard> = emptyList(),

    val isDone: Boolean,

    val continueCursor: String,
)