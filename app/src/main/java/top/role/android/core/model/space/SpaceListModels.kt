package top.role.android.core.model.space

import kotlinx.serialization.Serializable

enum class SpaceListMode(
    val value: String,
) {
    Featured("featured"),
    Following("following"),
    Latest("latest"),
    ;

    companion object {
        fun fromValue(
            value: String?,
        ): SpaceListMode {
            return entries.firstOrNull {
                it.value == value
            } ?: Latest
        }
    }
}

@Serializable
data class MobileSpaceCard(
    val id: String,
    val type: String,

    val name: String,
    val description: String,

    val number: Double? = null,

    val kind: String,

    val cover: String? = null,

    val tags: List<String> = emptyList(),

    val following: Boolean = false,

    val membershipEnabled: Boolean = false,
)

@Serializable
data class MobileSpaceListPage(
    val page: List<MobileSpaceCard> = emptyList(),

    val isDone: Boolean,

    val continueCursor: String,
)