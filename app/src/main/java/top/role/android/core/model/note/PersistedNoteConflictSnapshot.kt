package top.role.android.core.model.note

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

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

    // 冲突时的完整远端正文。
    // 可空以兼容旧版已持久化的 Conflict Snapshot。
    val content: JsonElement? = null,
) {

    val parsedRemoteStatus:
            NoteRemoteStatus
        get() =
            NoteRemoteStatus.fromRaw(
                remoteStatus,
            )
}