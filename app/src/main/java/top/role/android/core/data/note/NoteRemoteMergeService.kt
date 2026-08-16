package top.role.android.core.data.note

import top.role.android.core.database.note.LocalNoteEntity
import top.role.android.core.model.note.NoteRemoteStatus
import top.role.android.core.model.note.NoteSyncState
import top.role.android.core.model.note.RemoteNoteSnapshot
import top.role.android.core.database.note.hasUnsyncedChanges
import top.role.android.core.database.note.hasPendingSnapshot

class NoteRemoteMergeService(
    private val noteLocalRepository:
    NoteLocalRepository,
) {

    suspend fun merge(
        ownerUserId: String,
        snapshot: RemoteNoteSnapshot,
    ) {
        val local =
            findMatchingLocalNote(
                ownerUserId =
                    ownerUserId,
                snapshot =
                    snapshot,
            )

        if (local == null) {
            mergeRemoteOnlyNote(
                ownerUserId =
                    ownerUserId,
                snapshot =
                    snapshot,
            )

            return
        }

        mergeExistingNote(
            local =
                local,
            snapshot =
                snapshot,
        )
    }

    private suspend fun findMatchingLocalNote(
        ownerUserId: String,
        snapshot: RemoteNoteSnapshot,
    ): LocalNoteEntity? {

        /*
         * 1. 正式 noteId 优先。
         */
        val byNoteId =
            noteLocalRepository
                .findByNoteId(
                    ownerUserId =
                        ownerUserId,
                    noteId =
                        snapshot.noteId,
                )

        if (byNoteId != null) {
            return byNoteId
        }

        /*
         * 2. 离线创建时生成的 clientKey。
         */
        val clientKey =
            snapshot.clientKey

        if (
            !clientKey.isNullOrBlank()
        ) {
            val byClientKey =
                noteLocalRepository
                    .findByClientKey(
                        ownerUserId =
                            ownerUserId,
                        clientKey =
                            clientKey,
                    )

            if (byClientKey != null) {
                return byClientKey
            }
        }

        /*
         * 3. DayOnly 最后的业务身份兜底。
         *
         * 同一用户同一天最多一个 DayOnly。
         */
        if (
            snapshot.onlyDay &&
            !snapshot.dayKey.isNullOrBlank()
        ) {
            return noteLocalRepository
                .findDayOnlyNote(
                    ownerUserId =
                        ownerUserId,
                    dayKey =
                        snapshot.dayKey,
                )
        }

        return null
    }

    private suspend fun mergeRemoteOnlyNote(
        ownerUserId: String,
        snapshot: RemoteNoteSnapshot,
    ) {
        /*
         * 本地从未见过的 deleted tombstone，
         * 没必要为了“删除”再创建本地缓存。
         */
        if (
            snapshot.remoteStatus ==
            NoteRemoteStatus.Deleted
        ) {
            return
        }

        val remoteVersion =
            snapshot.version.toLong()

        val remoteUpdatedAt =
            snapshot.updatedAt.toLong()

        val note =
            LocalNoteEntity(
                rowId =
                    0,

                localKey =
                    existingLocalKey(
                        ownerUserId =
                            ownerUserId,
                        noteId =
                            snapshot.noteId,
                    ),

                ownerUserId =
                    ownerUserId,

                noteId =
                    snapshot.noteId,

                clientKey =
                    snapshot.clientKey
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?: remoteClientKey(
                            snapshot.noteId,
                        ),

                dayKey =
                    snapshot.dayKey,

                onlyDay =
                    snapshot.onlyDay,

                title =
                    snapshot.title,

                text =
                    snapshot.text,

                pinned =
                    snapshot.pinned,

                remoteStatus =
                    snapshot.remoteStatus,

                pendingRemoteStatus =
                    null,

                syncedDayKey =
                    snapshot.dayKey,

                syncedOnlyDay =
                    snapshot.onlyDay,

                syncedTitle =
                    snapshot.title,

                syncedText =
                    snapshot.text,

                syncedPinned =
                    snapshot.pinned,

                remoteVersion =
                    remoteVersion,

                remoteUpdatedAt =
                    remoteUpdatedAt,

                pendingSnapshotJson =
                    null,

                conflictSnapshotJson =
                    null,
                /*
                 * 远端首次 hydrate 的 Note，
                 * 初始本地排序时间直接使用远端更新时间。
                 */
                localUpdatedAt =
                    remoteUpdatedAt,

                syncState =
                    NoteSyncState.Synced,

                lastSyncError =
                    null,
            )

        noteLocalRepository
            .upsertLocal(
                note,
            )
    }

    private suspend fun mergeExistingNote(
        local: LocalNoteEntity,
        snapshot: RemoteNoteSnapshot,
    ) {
        /*
         * Convex v.number() 在 Android transport 层使用 Double。
         *
         * 从这里开始进入 Android domain，
         * version / timestamp 统一转成 Long。
         */
        val remoteVersion =
            snapshot.version.toLong()

        val remoteUpdatedAt =
            snapshot.updatedAt.toLong()

        val knownRemoteVersion =
            local.remoteVersion

        /*
         * 本地已经见过更高的远端版本，
         * 当前 snapshot 属于旧数据。
         */
        if (
            knownRemoteVersion != null &&
            remoteVersion <
            knownRemoteVersion
        ) {
            return
        }

        val hasLocalWork =
            hasLocalWork(
                local,
            )

        /*
         * deleted tombstone 单独处理。
         */
        if (
            snapshot.remoteStatus ==
            NoteRemoteStatus.Deleted
        ) {
            mergeRemoteDeleted(
                local =
                    local,
                snapshot =
                    snapshot,
                hasLocalWork =
                    hasLocalWork,
            )

            return
        }

        /*
         * ------------------------------------------------
         * Lifecycle goal 已经由远端达成
         * ------------------------------------------------
         *
         * 典型情况：
         *
         * Android:
         *   pendingRemoteStatus = Archived
         *
         * archive mutation 实际已经成功，
         * 但 response 因网络原因没有回来。
         *
         * 下一次 Pull：
         *   remoteStatus = Archived
         *
         * 此时不是 Conflict，
         * 而是 lifecycle 已经成功。
         *
         *
         * 注意：
         *
         * 如果本地同时还有未同步 content，
         * 则不能直接认为 lifecycle 完成。
         *
         * 例如：
         *
         * local content B
         * pending archive
         *
         * 另一设备已经把 remote archive
         *
         * 此时 B 尚未上传，
         * 应进入真正 Conflict。
         */
        val hasContentWork =
            local.hasUnsyncedChanges ||
                    local.hasPendingSnapshot

        val pendingLifecycle =
            local.pendingRemoteStatus

        if (
            pendingLifecycle != null &&
            pendingLifecycle !=
            NoteRemoteStatus.Deleted &&
            pendingLifecycle ==
            snapshot.remoteStatus &&
            !hasContentWork
        ) {
            val ownerUserId =
                local.ownerUserId
                    ?: return

            noteLocalRepository
                .applyLifecycleSuccess(
                    ownerUserId =
                        ownerUserId,

                    localKey =
                        local.localKey,

                    expectedStatus =
                        pendingLifecycle,

                    remote =
                        snapshot,
                )

            return
        }

        /*
         * 本地还有未同步工作：
         *
         * - 不覆盖 title / text
         * - 不覆盖 dayKey / onlyDay
         * - 不覆盖 pinned
         * - 不覆盖 synced baseline
         * - 不清 pending snapshot
         * - 不推进 remoteVersion
         *
         * 这里只允许吸收远端 identity 和 lifecycle 信息。
         */
        if (
            hasLocalWork
        ) {
            val identified =
                adoptRemoteIdentity(
                    local =
                        local,

                    snapshot =
                        snapshot,
                )

            /*
             * ------------------------------------------------
             * 真正的 remote newer Conflict
             * ------------------------------------------------
             *
             * 必须满足：
             *
             * 1. 本地已经知道一个正式 remoteVersion
             * 2. 当前 Pull 回来的 remoteVersion 更高
             *
             * knownRemoteVersion == null 不算 Conflict。
             *
             * 因为这种情况经常是：
             *
             * offline create
             * → create mutation 成功
             * → response 丢失
             * → Pull 找到刚创建的 remote Note
             *
             * 这只是 identity 收敛，
             * 仍然可以依靠 requestId replay 完成同步。
             */
            if (
                knownRemoteVersion != null &&
                remoteVersion >
                knownRemoteVersion
            ) {
                noteLocalRepository
                    .markConflictFromRemote(
                        note =
                            identified,

                        remote =
                            snapshot,

                        message =
                            "云端记录已更新，本机仍有未同步修改",
                    )

                return
            }

            /*
             * 没有看到更高 remoteVersion。
             *
             * 继续保留本地内容和 pending snapshot，
             * 只吸收 identity 以及 remoteUpdatedAt。
             *
             * remoteVersion 仍然不能推进，
             * 因为 frozen pending snapshot
             * 还依赖原来的 baseVersion。
             */
            noteLocalRepository
                .upsertLocal(
                    identified.copy(
                        remoteUpdatedAt =
                            maxOfNullable(
                                identified.remoteUpdatedAt,
                                remoteUpdatedAt,
                            ),
                    )
                )

            return
        }
        /*
         * clean local + 同版本：
         *
         * 内容已经是同一远端版本，
         * 不需要重复覆盖。
         *
         * 但仍允许补齐远端 noteId。
         */
        if (
            knownRemoteVersion != null &&
            remoteVersion ==
            knownRemoteVersion
        ) {
            val identified =
                adoptRemoteIdentity(
                    local =
                        local,
                    snapshot =
                        snapshot,
                )

            if (
                identified !=
                local
            ) {
                noteLocalRepository
                    .upsertLocal(
                        identified,
                    )
            }

            return
        }

        /*
         * clean local + remote newer：
         *
         * 远端 snapshot 成为新的本地基线。
         */
        val identified =
            adoptRemoteIdentity(
                local =
                    local,
                snapshot =
                    snapshot,
            )

        noteLocalRepository
            .upsertLocal(
                identified.copy(
                    dayKey =
                        snapshot.dayKey,

                    onlyDay =
                        snapshot.onlyDay,

                    title =
                        snapshot.title,

                    text =
                        snapshot.text,

                    pinned =
                        snapshot.pinned,

                    remoteStatus =
                        snapshot.remoteStatus,

                    pendingRemoteStatus =
                        null,

                    syncedDayKey =
                        snapshot.dayKey,

                    syncedOnlyDay =
                        snapshot.onlyDay,

                    syncedTitle =
                        snapshot.title,

                    syncedText =
                        snapshot.text,

                    syncedPinned =
                        snapshot.pinned,

                    remoteVersion =
                        remoteVersion,

                    remoteUpdatedAt =
                        remoteUpdatedAt,

                    pendingSnapshotJson =
                        null,

                    conflictSnapshotJson =
                        null,

                    localUpdatedAt =
                        remoteUpdatedAt,

                    syncState =
                        NoteSyncState.Synced,

                    lastSyncError =
                        null,
                )
            )
    }

    private suspend fun mergeRemoteDeleted(
        local: LocalNoteEntity,
        snapshot: RemoteNoteSnapshot,
        hasLocalWork: Boolean,
    ) {
        /*
         * 本地本来就要求删除。
         *
         * 远端 tombstone 出现，
         * lifecycle goal 已达成。
         */
        if (
            local.pendingRemoteStatus ==
            NoteRemoteStatus.Deleted
        ) {
            noteLocalRepository
                .deleteLocal(
                    local,
                )

            return
        }

        /*
         * 本地没有任何用户工作。
         *
         * 远端删除可以直接反映到 cache。
         */
        if (
            !hasLocalWork
        ) {
            noteLocalRepository
                .deleteLocal(
                    local,
                )

            return
        }

        /*
         * Remote Deleted
         * +
         * Local dirty
         *
         * 是真正 Conflict。
         */
        val identified =
            adoptRemoteIdentity(
                local =
                    local,

                snapshot =
                    snapshot,
            )

        noteLocalRepository
            .markConflictFromRemote(
                note =
                    identified,

                remote =
                    snapshot,

                message =
                    "云端记录已删除，本机仍有未同步修改",
            )
    }

    private fun hasLocalWork(
        note: LocalNoteEntity,
    ): Boolean {
        return note.pendingRemoteStatus !=
                null ||
                note.hasUnsyncedChanges ||
                note.hasPendingSnapshot ||
                note.syncState !=
                NoteSyncState.Synced
    }

    /**
     * 将本地临时身份收敛到正式远端 noteId。
     *
     * rowId 不变，因此不会改变 Room 中真正的主键。
     */
    private fun adoptRemoteIdentity(
        local: LocalNoteEntity,
        snapshot: RemoteNoteSnapshot,
    ): LocalNoteEntity {
        if (
            local.noteId ==
            snapshot.noteId
        ) {
            return local
        }

        val ownerUserId =
            local.ownerUserId
                ?: return local

        return local.copy(
            noteId =
                snapshot.noteId,

            localKey =
                existingLocalKey(
                    ownerUserId =
                        ownerUserId,
                    noteId =
                        snapshot.noteId,
                ),
        )
    }

    private fun existingLocalKey(
        ownerUserId: String,
        noteId: String,
    ): String {
        return "user:$ownerUserId:note:$noteId"
    }

    private fun remoteClientKey(
        noteId: String,
    ): String {
        /*
         * 旧远端 Note 可能没有 clientKey。
         *
         * 已经拥有 noteId 后，
         * clientKey 不再承担主要 identity 职责。
         * 这里只生成一个稳定的本地占位值。
         */
        return "remote:$noteId"
    }

    private fun maxOfNullable(
        current: Long?,
        incoming: Long,
    ): Long {
        return if (
            current == null
        ) {
            incoming
        } else {
            maxOf(
                current,
                incoming,
            )
        }
    }
}