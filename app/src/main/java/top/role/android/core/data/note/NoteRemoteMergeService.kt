package top.role.android.core.data.note

import top.role.android.core.database.note.LocalNoteEntity
import top.role.android.core.model.note.NoteRemoteStatus
import top.role.android.core.model.note.NoteSyncState
import top.role.android.core.model.note.RemoteNoteSnapshot
import top.role.android.core.model.note.content.NoteContentCodec

class NoteRemoteMergeService(
    private val noteLocalRepository:
    NoteLocalRepository,
) {
    suspend fun merge(
        ownerUserId: String,
        snapshot: RemoteNoteSnapshot,
    ) {
        val local = findMatchingLocalNote(
            ownerUserId = ownerUserId,
            snapshot = snapshot,
        )

        if (local == null) {
            mergeRemoteOnlyNote(
                ownerUserId = ownerUserId,
                snapshot = snapshot,
            )
            return
        }

        mergeExistingNote(
            local = local,
            snapshot = snapshot,
        )
    }

    private suspend fun findMatchingLocalNote(
        ownerUserId: String,
        snapshot: RemoteNoteSnapshot,
    ): LocalNoteEntity? {

        /*
         * 1. 正式 noteId 优先。
         */
        val byNoteId = noteLocalRepository
            .findByNoteId(
                ownerUserId = ownerUserId,
                noteId = snapshot.noteId,
            )

        if (byNoteId != null) {
            return byNoteId
        }

        /*
         * 2. 离线创建时生成的 clientKey。
         */
        val clientKey = snapshot.clientKey

        if (
            !clientKey.isNullOrBlank()
        ) {
            val byClientKey = noteLocalRepository
                .findByClientKey(
                    ownerUserId = ownerUserId,
                    clientKey = clientKey,
                )

            if (byClientKey != null) {
                return byClientKey
            }
        }

        /*
         * 3. DayOnly 最后的业务身份兜底。
         */
        if (
            snapshot.onlyDay &&
            !snapshot.dayKey.isNullOrBlank()
        ) {
            return noteLocalRepository
                .findDayOnlyNote(
                    ownerUserId = ownerUserId,
                    dayKey = snapshot.dayKey,
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
         */
        if (
            snapshot.remoteStatus == NoteRemoteStatus.Deleted
        ) {
            return
        }

        val remoteVersion = snapshot.version.toLong()
        val remoteUpdatedAt = snapshot.updatedAt.toLong()
        val contentJson = remoteContentJson(snapshot)
        val plainText = remotePlainText(snapshot)

        val note = LocalNoteEntity(
            rowId = 0,

            localKey = existingLocalKey(
                ownerUserId = ownerUserId,
                noteId = snapshot.noteId,
            ),

            ownerUserId = ownerUserId,
            noteId = snapshot.noteId,

            clientKey = snapshot.clientKey
                ?.takeIf { it.isNotBlank() }
                ?: remoteClientKey( snapshot.noteId),

            dayKey = snapshot.dayKey,
            onlyDay = snapshot.onlyDay,
            title = snapshot.title,
            text = plainText,
            contentJson = contentJson,
            pinned = snapshot.pinned,
            remoteStatus = snapshot.remoteStatus,
            pendingRemoteStatus = null,

            syncedDayKey = snapshot.dayKey,
            syncedOnlyDay = snapshot.onlyDay,
            syncedTitle = snapshot.title,
            syncedText = plainText,
            syncedContentJson = contentJson,
            syncedPinned = snapshot.pinned,
            remoteVersion = remoteVersion,
            remoteUpdatedAt = remoteUpdatedAt,
            pendingSnapshotJson = null,
            conflictSnapshotJson = null,
            /*
             * 远端首次 hydrate 的 Note，
             * 初始本地排序时间直接使用远端更新时间。
             */
            localUpdatedAt = remoteUpdatedAt,
            syncState = NoteSyncState.Synced,
            lastSyncError = null,
        )

        noteLocalRepository
            .upsertLocal(note)
    }

    private suspend fun mergeExistingNote(
        local: LocalNoteEntity,
        snapshot: RemoteNoteSnapshot,
    ) {
        val remoteVersion = snapshot.version.toLong()
        val remoteUpdatedAt = snapshot.updatedAt.toLong()
        val knownVersion = local.remoteVersion
        val knownUpdatedAt = local.remoteUpdatedAt ?: 0L
        val incomingText = remotePlainText(snapshot)
        val incomingContentJson = remoteContentJson(snapshot)

        // A late Pull must never roll back an already known content version.
        if (knownVersion != null && remoteVersion < knownVersion) return

        val contentDirty = hasLocalContentWork(local)
        val pinnedDirty = local.pinned != local.syncedPinned
        val lifecycleDirty = local.pendingRemoteStatus != null
        val otherUnsyncedState = local.syncState != NoteSyncState.Synced &&
                !contentDirty && !pinnedDirty && !lifecycleDirty
        val hasLocalWork = contentDirty || pinnedDirty ||
                lifecycleDirty || otherUnsyncedState

        if (snapshot.remoteStatus == NoteRemoteStatus.Deleted) {
            mergeRemoteDeleted(local, snapshot, hasLocalWork)
            return
        }

        val pendingLifecycle = local.pendingRemoteStatus
        if (
            pendingLifecycle != null &&
            pendingLifecycle != NoteRemoteStatus.Deleted &&
            pendingLifecycle == snapshot.remoteStatus &&
            !contentDirty && !pinnedDirty
        ) {
            val owner = local.ownerUserId ?: return
            noteLocalRepository.applyLifecycleSuccess(
                ownerUserId = owner,
                localKey = local.localKey,
                expectedStatus = pendingLifecycle,
                remote = snapshot,
            )
            return
        }

        // Content/lifecycle conflict protection is independent of pinned.
        // Do not overwrite a pending rich-text edit or its baseline.
        if (contentDirty || lifecycleDirty || otherUnsyncedState) {
            val identified = adoptRemoteIdentity(local, snapshot)
            if (
                knownVersion != null && remoteVersion > knownVersion
            ) {
                noteLocalRepository.markConflictFromRemote(
                    note = identified,
                    remote = snapshot,
                    message = "云端正文已更新，本机仍有未同步修改",
                )
                return
            }

            // Pinned can hydrate independently while content is pending,
            // provided this device has no pending pinned change.
            val newerState = remoteUpdatedAt > knownUpdatedAt
            val updated = identified.copy(
                pinned = if (newerState && !pinnedDirty) {
                    snapshot.pinned
                } else {
                    identified.pinned
                },
                syncedPinned = if (newerState && !pinnedDirty) {
                    snapshot.pinned
                } else {
                    identified.syncedPinned
                },
                remoteUpdatedAt = maxOfNullable(
                    identified.remoteUpdatedAt,
                    remoteUpdatedAt,
                ),
            )
            if (updated != local) noteLocalRepository.upsertLocal(updated)
            return
        }

        val identified = adoptRemoteIdentity(local, snapshot)
        val newerState = remoteUpdatedAt > knownUpdatedAt
        val newerContent = knownVersion == null || remoteVersion > knownVersion
        val missingContent = identified.contentJson == null ||
                identified.syncedContentJson == null

        // A pinned-only local change does not block remote Content updates.
        // Preserve the local pinned choice until setPinned or a newer Pull
        // confirms that same choice on the server.
        if (pinnedDirty) {
            val confirmedPinned = if (newerState) {
                snapshot.pinned
            } else {
                identified.syncedPinned
            }
            val stillPinnedDirty = identified.pinned != confirmedPinned

            val updated = identified.copy(
                dayKey = if (newerContent) snapshot.dayKey else identified.dayKey,
                onlyDay = if (newerContent) snapshot.onlyDay else identified.onlyDay,
                title = if (newerContent) snapshot.title else identified.title,
                text = if (newerContent) incomingText else identified.text,
                contentJson = if (newerContent || missingContent) {
                    incomingContentJson
                } else {
                    identified.contentJson
                },
                syncedDayKey = if (newerContent) snapshot.dayKey else identified.syncedDayKey,
                syncedOnlyDay = if (newerContent) snapshot.onlyDay else identified.syncedOnlyDay,
                syncedTitle = if (newerContent) snapshot.title else identified.syncedTitle,
                syncedText = if (newerContent) incomingText else identified.syncedText,
                syncedContentJson = if (newerContent || missingContent) {
                    incomingContentJson
                } else {
                    identified.syncedContentJson
                },
                // Do not write pinned: keep the local desired value.
                syncedPinned = confirmedPinned,
                remoteStatus = if (newerContent || newerState) {
                    snapshot.remoteStatus
                } else {
                    identified.remoteStatus
                },
                remoteVersion = remoteVersion,
                remoteUpdatedAt = maxOfNullable(
                    identified.remoteUpdatedAt,
                    remoteUpdatedAt,
                ),
                localUpdatedAt = maxOf(identified.localUpdatedAt, remoteUpdatedAt),
                syncState = if (stillPinnedDirty) NoteSyncState.Pending else NoteSyncState.Synced,
                lastSyncError = if (stillPinnedDirty) identified.lastSyncError else null,
            )
            if (updated != local) noteLocalRepository.upsertLocal(updated)
            return
        }

        // No local changes: a newer remote version fully replaces Content.
        // With the same content version, only hydrate missing content and
        // absorb updatedAt-based pinned/status changes.
        val updated = identified.copy(
            dayKey = if (newerContent) snapshot.dayKey else identified.dayKey,
            onlyDay = if (newerContent) snapshot.onlyDay else identified.onlyDay,
            title = if (newerContent) snapshot.title else identified.title,
            text = if (newerContent) incomingText else identified.text,
            contentJson = if (newerContent || missingContent) {
                incomingContentJson
            } else {
                identified.contentJson
            },
            syncedDayKey = if (newerContent) snapshot.dayKey else identified.syncedDayKey,
            syncedOnlyDay = if (newerContent) snapshot.onlyDay else identified.syncedOnlyDay,
            syncedTitle = if (newerContent) snapshot.title else identified.syncedTitle,
            syncedText = if (newerContent) incomingText else identified.syncedText,
            syncedContentJson = if (newerContent || missingContent) {
                incomingContentJson
            } else {
                identified.syncedContentJson
            },
            pinned = if (newerState) snapshot.pinned else identified.pinned,
            syncedPinned = if (newerState) snapshot.pinned else identified.syncedPinned,
            remoteStatus = if (newerContent || newerState) {
                snapshot.remoteStatus
            } else {
                identified.remoteStatus
            },
            remoteVersion = remoteVersion,
            remoteUpdatedAt = maxOfNullable(identified.remoteUpdatedAt, remoteUpdatedAt),
            localUpdatedAt = if (newerContent || newerState) {
                maxOf(identified.localUpdatedAt, remoteUpdatedAt)
            } else {
                identified.localUpdatedAt
            },
            pendingRemoteStatus = null,
            pendingSnapshotJson = null,
            conflictSnapshotJson = null,
            syncState = NoteSyncState.Synced,
            lastSyncError = null,
        )
        if (updated != local) noteLocalRepository.upsertLocal(updated)
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

    // Content and pinned have independent sync baselines.
    private fun hasLocalContentWork(note: LocalNoteEntity): Boolean {
        return note.noteId == null ||
                note.pendingSnapshotJson != null ||
                note.dayKey != note.syncedDayKey ||
                note.onlyDay != note.syncedOnlyDay ||
                note.title != note.syncedTitle ||
                note.text != note.syncedText ||
                !sameContentBaseline(note)
    }

    private fun sameContentBaseline(note: LocalNoteEntity): Boolean {
        val current = NoteContentCodec.parse(note.contentJson)
            ?: NoteContentCodec.fromPlainText(note.text)
        val synced = NoteContentCodec.parse(note.syncedContentJson)
            ?: NoteContentCodec.fromPlainText(note.syncedText)
        return current == synced
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

    private fun remotePlainText(snapshot: RemoteNoteSnapshot): String {
        return snapshot.content?.let(NoteContentCodec::extractText)
            ?: snapshot.text
    }

    private fun remoteContentJson(snapshot: RemoteNoteSnapshot): String {
        val content = snapshot.content
            ?: NoteContentCodec.fromPlainText(snapshot.text)
        return NoteContentCodec.stringify(content)
    }
}
