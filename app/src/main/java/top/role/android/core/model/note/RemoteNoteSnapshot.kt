package top.role.android.core.model.note

import kotlinx.serialization.Serializable

@Serializable
data class RemoteNoteSnapshot(
    val noteId: String,
    val clientKey: String? = null,

    val dayKey: String? = null,
    val onlyDay: Boolean,

    val title: String,
    val text: String,

    val pinned: Boolean,

    val status: String,

    val version: Double,

    val createdAt: Double,
    val updatedAt: Double,
) {
    val remoteStatus: NoteRemoteStatus
        get() =
            NoteRemoteStatus.fromRaw(
                status
            )
}