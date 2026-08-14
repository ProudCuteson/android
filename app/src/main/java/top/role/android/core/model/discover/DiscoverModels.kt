package top.role.android.core.model.discover

import kotlinx.serialization.Serializable
import top.role.android.core.model.content.MobileContentCard
import top.role.android.core.model.space.MobileSpaceCard
import top.role.android.core.model.topic.MobileTopicCard

@Serializable
data class MobileDiscoverPage(
    val featuredSpaces:
    List<MobileSpaceCard> =
        emptyList(),

    val followingSpaces:
    List<MobileSpaceCard> =
        emptyList(),

    val latestSpaces:
    List<MobileSpaceCard> =
        emptyList(),

    val topics:
    List<MobileTopicCard> =
        emptyList(),

    val contents:
    List<MobileContentCard> =
        emptyList(),
)