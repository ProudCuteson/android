package top.role.android.core.model.content

import kotlinx.serialization.Serializable

@Serializable
data class MobileContentSpaceRef(
    val id: String,
    val number: Double? = null,
    val name: String? = null,
)

@Serializable
data class MobileContentCard(
    val id: String,
    val publishId: String,

    val type: String,

    val title: String,
    val description: String,

    val cover: String? = null,

    val space: MobileContentSpaceRef,

    val ownerUserId: String,

    val accessLevel: String,

    val canOpen: Boolean,

    val lockReason: String? = null,

    val tags: List<String> = emptyList(),

    val featured: Boolean = false,

    val updatedAt: Double,
)

@Serializable
data class MobileContentListPage(
    val page: List<MobileContentCard> = emptyList(),

    val isDone: Boolean,

    val continueCursor: String,
)