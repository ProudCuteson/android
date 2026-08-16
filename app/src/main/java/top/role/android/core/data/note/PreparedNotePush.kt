package top.role.android.core.data.note

import top.role.android.core.database.note.LocalNoteEntity
import top.role.android.core.model.note.PersistedNoteSyncSnapshot

data class PreparedNotePush(
    val note: LocalNoteEntity,
    val snapshot: PersistedNoteSyncSnapshot,
)