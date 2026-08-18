package top.role.android.core.model.note

import kotlinx.serialization.Serializable

@Serializable
data class PersistedNoteMediaSyncSnapshot(
    val requestId: String,

    val noteId: String,

    val baseVersion: Long?,

    val media:
    List<PersistedNoteMediaItem>,
)

@Serializable
data class PersistedNoteMediaItem(
    val imageId: String,

    val order: Int,
)

@Serializable
data class PersistedNoteMediaConflictSnapshot(
    val noteId: String,
    val version: Long,
    val updatedAt: Long,
    val media: List<PersistedNoteMediaItem>,
)