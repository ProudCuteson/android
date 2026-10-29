package top.role.android.core.model.note

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class PersistedNoteSyncSnapshot(
    val requestId: String,
    val noteId: String?,
    val clientKey: String,
    val baseVersion: Long?,
    val dayKey: String?,
    val onlyDay: Boolean,
    val title: String,
    val text: String,
    val pinned: Boolean,

    // 兼容旧版 Room pendingSnapshotJson（没有 content 字段）。
    val content: JsonElement? = null,
)