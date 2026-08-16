package top.role.android.feature.note.editor

import top.role.android.core.model.note.NoteRemoteStatus

data class NoteConflictUiModel(
    val title: String,
    val text: String,

    val dayKey: String?,
    val onlyDay: Boolean,

    val pinned: Boolean,

    val remoteStatus: NoteRemoteStatus,

    val version: Long,
    val updatedAt: Long,
)