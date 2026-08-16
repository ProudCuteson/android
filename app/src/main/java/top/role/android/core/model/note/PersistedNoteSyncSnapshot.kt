package top.role.android.core.model.note

import kotlinx.serialization.Serializable

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
)