package top.role.android.core.data.note

import top.role.android.core.model.note.NotePullCheckpoint
import top.role.android.core.model.note.RemoteNoteSnapshot

data class NotePullRoundResult(
    val changes:
    List<RemoteNoteSnapshot>,

    val nextCheckpoint:
    NotePullCheckpoint,
)