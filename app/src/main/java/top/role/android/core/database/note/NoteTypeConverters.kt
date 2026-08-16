package top.role.android.core.database.note

import androidx.room.TypeConverter
import top.role.android.core.model.note.NoteRemoteStatus
import top.role.android.core.model.note.NoteSyncState

class NoteTypeConverters {

    @TypeConverter
    fun noteRemoteStatusToString(
        value: NoteRemoteStatus?,
    ): String? {
        return value?.rawValue
    }

    @TypeConverter
    fun stringToNoteRemoteStatus(
        value: String?,
    ): NoteRemoteStatus? {
        return value?.let {
            NoteRemoteStatus.fromRaw(
                value = it,
            )
        }
    }

    @TypeConverter
    fun noteSyncStateToString(
        value: NoteSyncState,
    ): String {
        return value.rawValue
    }

    @TypeConverter
    fun stringToNoteSyncState(
        value: String,
    ): NoteSyncState {
        return NoteSyncState.fromRaw(
            value = value,
        )
    }
}