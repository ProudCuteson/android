package top.role.android.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import top.role.android.core.model.note.NoteMediaSyncState

@Entity(
    tableName =
        "local_note_media_sync",

    indices = [
        Index(
            value = [
                "ownerUserId",
                "noteLocalKey",
            ],
            unique = true,
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
                "syncStateRaw",
            ],
        ),
    ],
)
data class LocalNoteMediaSyncEntity(
    @PrimaryKey
    val syncKey: String,

    val ownerUserId: String,

    /*
     * 当前绑定的本地 Note identity。
     *
     * 新 Note：
     * temporary localKey
     *
     * 首次 Note push 后：
     * official localKey
     */
    val noteLocalKey: String,

    /*
     * 新 Note 尚未完成 Content push 时为空。
     */
    val noteId: String?,

    /*
     * Media 自己的 server version。
     *
     * 与 LocalNoteEntity.remoteVersion
     * 完全独立。
     */
    val remoteVersion: Long?,

    val remoteUpdatedAt: Long?,

    val syncStateRaw: String,

    /*
     * 当前正在发送的 frozen snapshot。
     */
    val pendingSnapshotJson: String?,

    /*
     * 后续 Media version conflict 使用。
     */
    val conflictSnapshotJson: String?,

    val lastSyncError: String?,

    val localUpdatedAt: Long,
) {

    val syncState:
            NoteMediaSyncState
        get() =
            NoteMediaSyncState
                .fromRawValue(
                    syncStateRaw,
                )
}