package top.role.android.core.database.note

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import top.role.android.core.model.note.NoteRemoteStatus
import top.role.android.core.model.note.NoteSyncState

@Entity(
    tableName = "local_notes",
    indices = [
        Index(
            value = ["localKey"],
            unique = true,
        ),
        Index(
            value = ["ownerUserId"],
        ),
        Index(
            value = [
                "ownerUserId",
                "noteId",
            ],
        ),
        Index(
            value = [
                "ownerUserId",
                "clientKey",
            ],
        ),
        Index(
            value = [
                "ownerUserId",
                "dayKey",
            ],
        ),
        Index(
            value = [
                "ownerUserId",
                "onlyDay",
                "dayKey",
            ],
        ),
        Index(
            value = [
                "ownerUserId",
                "syncState",
            ],
        ),
    ],
)
data class LocalNoteEntity(
    @PrimaryKey(autoGenerate = true)
    val rowId: Long = 0,
    val localKey: String,
    val ownerUserId: String?,
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

val LocalNoteEntity.hasOwner: Boolean
    get() =
        !ownerUserId.isNullOrBlank()

val LocalNoteEntity.hasUnsyncedChanges: Boolean
    get() =
        title != syncedTitle ||
                text != syncedText ||
                dayKey != syncedDayKey ||
                onlyDay != syncedOnlyDay ||
                pinned != syncedPinned

val LocalNoteEntity.hasPendingSnapshot: Boolean
    get() =
        !pendingSnapshotJson.isNullOrBlank()

val LocalNoteEntity.hasConflictSnapshot: Boolean
    get() =
        !conflictSnapshotJson.isNullOrBlank()

val LocalNoteEntity.hasContent: Boolean
    get() =
        title.isNotBlank() ||
                text.isNotBlank()

val LocalNoteEntity.effectiveRemoteStatus:
        NoteRemoteStatus
    get() =
        pendingRemoteStatus
            ?: remoteStatus