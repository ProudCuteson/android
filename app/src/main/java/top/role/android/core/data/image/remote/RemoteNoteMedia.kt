package top.role.android.core.data.image.remote

import kotlinx.serialization.Serializable
import top.role.android.core.model.note.PersistedNoteMediaItem

@Serializable
data class RemoteNoteMedia(
    val type: String,

    val imageId: String,

    val order: Double,
)

@Serializable
data class RemoteNoteMediaResponse(
    val noteId: String,

    val version: Double,

    val updatedAt: Double,

    val media:
    List<RemoteNoteMedia> =
        emptyList(),
)

@Serializable
data class RemoteReplaceNoteMediaResponse(
    val ok: Boolean,

    val noteId: String,

    val version: Double,

    val updatedAt: Double,

    val media:
    List<RemoteNoteMedia> =
        emptyList(),
)