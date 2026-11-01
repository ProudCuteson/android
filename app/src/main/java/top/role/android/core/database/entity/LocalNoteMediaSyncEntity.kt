package top.role.android.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import top.role.android.core.model.note.NoteMediaSyncState

@Entity(
    tableName = "local_note_media_sync",
    indices = [
        Index(
            value = ["localOwnerUserId", "noteLocalKey"],
            unique = true,
        ),
        Index(
            value = [ "localOwnerUserId", "noteId"],
        ),
        Index(
            value = ["localOwnerUserId", "syncStateRaw"],
        ),
    ],
)

data class LocalNoteMediaSyncEntity(
    @PrimaryKey
    val syncKey: String,
    val localOwnerUserId: String,
    val noteLocalKey: String,
    val noteId: String?,
    val remoteVersion: Long?,
    val remoteUpdatedAt: Long?,
    val syncStateRaw: String,
    val pendingSnapshotJson: String?,
    val conflictSnapshotJson: String?,
    val lastSyncError: String?,
    val localUpdatedAt: Long,
) {

    val syncState: NoteMediaSyncState
        get() = NoteMediaSyncState.fromRawValue(syncStateRaw)
}