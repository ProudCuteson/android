package top.role.android.core.model.note

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.JsonElement

@Serializable
data class RemoteNoteSnapshot(
    @SerialName("_id")
    val noteId: String,

    val clientKey: String? = null,
    val dayKey: String? = null,
    val onlyDay: Boolean,

    val title: String = "",
    val content: JsonElement? = null,
    val text: String = "",

    val pinned: Boolean = false,
    val status: String,
    val version: Double,
    val createdAt: Double,
    val updatedAt: Double,
) {
    val remoteStatus: NoteRemoteStatus
        get() = NoteRemoteStatus.fromRaw(status)
}
