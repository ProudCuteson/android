package top.role.android.core.database.note

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import top.role.android.core.model.note.NoteRemoteStatus
import top.role.android.core.model.note.NoteSyncState
import top.role.android.core.model.note.content.NoteContentCodec

@Entity(
    tableName = "local_notes",
    indices = [
        Index(
            value = ["localKey"],
            unique = true,
        ),
        Index(
            value = ["localOwnerUserId"],
        ),
        Index(
            value = ["localOwnerUserId", "noteId"],
        ),
        Index(
            value = ["localOwnerUserId", "clientKey"],
        ),
        Index(
            value = ["localOwnerUserId", "dayKey"],
        ),
        Index(
            value = ["localOwnerUserId", "onlyDay", "dayKey"],
        ),
        Index(
            value = ["localOwnerUserId", "syncState"],
        ),
    ],
)

data class LocalNoteEntity(
    @PrimaryKey(autoGenerate = true)
    val rowId: Long = 0,
    val localKey: String,
    val localOwnerUserId: String,
    val noteId: String?,
    val clientKey: String,
    val dayKey: String?,
    val onlyDay: Boolean,
    val title: String,
    val text: String,

    // 对接Json
    val contentJson: String? = null,

    val pinned: Boolean,

    val remoteStatus: NoteRemoteStatus,
    val pendingRemoteStatus: NoteRemoteStatus?,

    val syncedDayKey: String?,
    val syncedOnlyDay: Boolean,
    val syncedTitle: String,
    val syncedText: String,

    // 服务端最后确认的正文 Content baseline
    val syncedContentJson: String? = null,
    val syncedPinned: Boolean,

    val remoteVersion: Long?,
    val remoteUpdatedAt: Long?,

    val pendingSnapshotJson: String?,
    val conflictSnapshotJson: String?,

    val localUpdatedAt: Long,

    val syncState: NoteSyncState,

    val lastSyncError: String?,
)

val LocalNoteEntity.hasUnsyncedContentChanges: Boolean
    get() {
        if (
            title != syncedTitle ||
            text != syncedText ||
            dayKey != syncedDayKey ||
            onlyDay != syncedOnlyDay
        ) return true

        val current = NoteContentCodec.parse(contentJson)
        val baseline = NoteContentCodec.parse(syncedContentJson)

        return when {
            current == null && baseline == null -> false
            current == null || baseline == null -> true
            else -> current != baseline
        }
    }

val LocalNoteEntity.hasUnsyncedChanges: Boolean
    get() = hasUnsyncedContentChanges || pinned != syncedPinned

val LocalNoteEntity.hasPendingSnapshot: Boolean
    get() = !pendingSnapshotJson.isNullOrBlank()

val LocalNoteEntity.hasConflictSnapshot: Boolean
    get() = !conflictSnapshotJson.isNullOrBlank()

val LocalNoteEntity.hasTextContent: Boolean
    get() = title.isNotBlank() || text.isNotBlank()

val LocalNoteEntity.effectiveRemoteStatus: NoteRemoteStatus
    get() = pendingRemoteStatus ?: remoteStatus