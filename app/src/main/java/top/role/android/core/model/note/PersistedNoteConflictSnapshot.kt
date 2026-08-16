package top.role.android.core.model.note

import kotlinx.serialization.Serializable

@Serializable
data class PersistedNoteConflictSnapshot(
    val noteId: String,

    val clientKey: String? = null,

    val dayKey: String? = null,
    val onlyDay: Boolean,

    val title: String,
    val text: String,
    val pinned: Boolean,

    /*
     * 使用 raw value：
     *
     * active
     * archived
     * deleted
     */
    val remoteStatus: String,

    val version: Long,
    val updatedAt: Long,
) {

    val parsedRemoteStatus:
            NoteRemoteStatus
        get() =
            NoteRemoteStatus.fromRaw(
                remoteStatus,
            )
}