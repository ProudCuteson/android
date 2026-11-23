package top.role.android.feature.content.picker

import kotlinx.serialization.Serializable
import top.role.android.feature.entity.service.EntityServiceReference

@Serializable
data class ContentPickerItem(
    val id: String,
    val type: String,
    val title: String,
    val description: String? = null,
    val updatedAt: Double,
)

@Serializable
data class ContentPickerPageResponse(
    val ok: Boolean,
    val code: String? = null,
    val page: List<ContentPickerItem> = emptyList(),
    val continueCursor: String? = null,
    val isDone: Boolean = true,
)

data class ContentSelection(
    val type: String,
    val id: String,
    val title: String,
)

fun ContentPickerItem.asSelection() =
    ContentSelection(
        type = type,
        id = id,
        title = title,
    )