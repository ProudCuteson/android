package top.role.android.core.model.note

import kotlinx.serialization.Serializable

@Serializable
data class PullNoteChangesResponse(
    val changes:
    List<RemoteNoteSnapshot> =
        emptyList(),

    val continueCursor:
    String? =
        null,

    val isDone:
    Boolean,

    val nextCheckpoint:
    NotePullCheckpoint? =
        null,
)