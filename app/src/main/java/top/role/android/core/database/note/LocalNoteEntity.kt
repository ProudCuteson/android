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
    val pinned: Boolean,

    /*
     * 服务端最后确认的 lifecycle 状态。
     */
    val remoteStatus: NoteRemoteStatus,

    /*
     * 本地希望服务端最终进入的 lifecycle 状态。
     *
     * null:
     * 没有 lifecycle mutation 等待同步。
     */
    val pendingRemoteStatus: NoteRemoteStatus?,

    /*
     * 最后一次服务端确认的内容 baseline。
     *
     * hasUnsyncedChanges 通过 current 与这里比较。
     */
    val syncedDayKey: String?,
    val syncedOnlyDay: Boolean,

    val syncedTitle: String,
    val syncedText: String,
    val syncedPinned: Boolean,

    /*
     * 服务端最后确认的版本。
     */
    val remoteVersion: Long?,
    val remoteUpdatedAt: Long?,

    /*
     * 当前 content push 已冻结的请求 snapshot。
     *
     * 用于：
     * - requestId 幂等
     * - baseVersion
     * - 网络失败后的安全重试
     */
    val pendingSnapshotJson: String?,

    /*
     * Conflict 时保存的最新远端 snapshot。
     *
     * pendingSnapshotJson:
     *   本地准备上传什么
     *
     * conflictSnapshotJson:
     *   冲突发生时云端实际是什么
     *
     * Conflict resolver 依赖这个字段，
     * 因此必须持久化到 Room，
     * 不能只存在 ViewModel 内存。
     */
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