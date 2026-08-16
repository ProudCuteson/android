package top.role.android.core.data.note

import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.json.Json

import top.role.android.core.database.note.LocalNoteDao
import top.role.android.core.database.note.LocalNoteEntity
import top.role.android.core.database.note.hasUnsyncedChanges
import top.role.android.core.model.note.DayKey
import top.role.android.core.model.note.NoteRemoteStatus
import top.role.android.core.model.note.NoteSyncState
import top.role.android.core.model.note.PersistedNoteSyncSnapshot
import top.role.android.core.model.note.RemoteNoteSnapshot
import top.role.android.core.model.note.NoteSyncFailure
import top.role.android.core.model.note.PersistedNoteConflictSnapshot

class NoteLocalRepository(
    private val dao: LocalNoteDao,
) {
    private val json =
        Json {
            ignoreUnknownKeys = true
        }

    private val _syncRequests =
        MutableSharedFlow<String>(
            extraBufferCapacity = 32,
            onBufferOverflow =
                BufferOverflow.DROP_OLDEST,
        )

    val syncRequests: SharedFlow<String> =
        _syncRequests.asSharedFlow()


    fun requestSync(
        ownerUserId: String,
    ) {
        if (
            ownerUserId.isBlank()
        ) {
            return
        }

        _syncRequests.tryEmit(
            ownerUserId,
        )
    }

    // MARK: - Observe

    fun observeRecentNotes(
        ownerUserId: String,
    ): Flow<List<LocalNoteEntity>> {
        return dao.observeRecentNotes(
            ownerUserId = ownerUserId,
        )
    }

    fun observePinnedNotes(
        ownerUserId: String,
    ): Flow<List<LocalNoteEntity>> {
        return dao.observePinnedNotes(
            ownerUserId = ownerUserId,
        )
    }

    fun observeDiaryNotes(
        ownerUserId: String,
    ): Flow<List<LocalNoteEntity>> {
        return dao.observeDiaryNotes(
            ownerUserId = ownerUserId,
        )
    }

    fun observeMonthNotesForDayHome(
        ownerUserId: String,
        startDayKey: String,
        endDayKeyExclusive: String,
    ): Flow<List<LocalNoteEntity>> {
        return dao.observeMonthNotesForDayHome(
            ownerUserId = ownerUserId,
            startDayKey = startDayKey,
            endDayKeyExclusive = endDayKeyExclusive,
        )
    }

    fun observeNotesForDay(
        ownerUserId: String,
        dayKey: String,
    ): Flow<List<LocalNoteEntity>> {
        return dao.observeNotesForDay(
            ownerUserId = ownerUserId,
            dayKey = dayKey,
        )
    }

    fun observeDayOnlyNote(
        ownerUserId: String,
        dayKey: String,
    ): Flow<LocalNoteEntity?> {
        return dao.observeDayOnlyNote(
            ownerUserId = ownerUserId,
            dayKey = dayKey,
        )
    }

    fun observeByLocalKey(
        ownerUserId: String,
        localKey: String,
    ): Flow<LocalNoteEntity?> {
        return dao.observeByLocalKey(
            ownerUserId =
                ownerUserId,

            localKey =
                localKey,
        )
    }

    fun observeArchivedNotes(
        ownerUserId: String,
    ): Flow<List<LocalNoteEntity>> {
        return dao.observeArchivedNotes(
            ownerUserId =
                ownerUserId,
        )
    }

    fun searchNotes(
        ownerUserId: String,
        query: String,
    ): Flow<List<LocalNoteEntity>> {
        return dao.searchNotes(
            ownerUserId = ownerUserId,
            query = query.trim(),
        )
    }

    // MARK: - Read

    suspend fun findByLocalKey(
        ownerUserId: String,
        localKey: String,
    ): LocalNoteEntity? {
        return dao.findByLocalKey(
            ownerUserId = ownerUserId,
            localKey = localKey,
        )
    }

    suspend fun findByNoteId(
        ownerUserId: String,
        noteId: String,
    ): LocalNoteEntity? {
        return dao.findByNoteId(
            ownerUserId = ownerUserId,
            noteId = noteId,
        )
    }

    suspend fun findByClientKey(
        ownerUserId: String,
        clientKey: String,
    ): LocalNoteEntity? {
        return dao.findByClientKey(
            ownerUserId = ownerUserId,
            clientKey = clientKey,
        )
    }

    suspend fun findDayOnlyNote(
        ownerUserId: String,
        dayKey: String,
    ): LocalNoteEntity? {
        return dao.findDayOnlyNote(
            ownerUserId = ownerUserId,
            dayKey = normalizeDayKey(dayKey),
        )
    }

    // MARK: - Create

    suspend fun createNormalNote(
        ownerUserId: String,
    ): LocalNoteEntity {
        val note =
            makeNewLocalNote(
                ownerUserId = ownerUserId,
                dayKey = null,
                onlyDay = false,
            )

        val rowId =
            dao.insert(
                note = note,
            )

        return note.copy(
            rowId = rowId,
        )
    }

    suspend fun createDatedNote(
        ownerUserId: String,
        dayKey: String,
    ): LocalNoteEntity {
        val normalizedDayKey =
            normalizeDayKey(dayKey)

        require(normalizedDayKey.isNotEmpty()) {
            "dayKey is required"
        }

        val note =
            makeNewLocalNote(
                ownerUserId = ownerUserId,
                dayKey = normalizedDayKey,
                onlyDay = false,
            )

        val rowId =
            dao.insert(
                note = note,
            )

        return note.copy(
            rowId = rowId,
        )
    }

    suspend fun getOrCreateDayOnlyNote(
        ownerUserId: String,
        dayKey: String,
    ): LocalNoteEntity {
        val normalizedDayKey =
            normalizeDayKey(dayKey)

        require(normalizedDayKey.isNotEmpty()) {
            "dayKey is required"
        }

        val existing =
            dao.findDayOnlyNote(
                ownerUserId = ownerUserId,
                dayKey = normalizedDayKey,
            )

        if (existing != null) {
            return existing
        }

        val note =
            makeNewLocalNote(
                ownerUserId = ownerUserId,
                dayKey = normalizedDayKey,
                onlyDay = true,
                localKey =
                    LocalNoteKeys.day(
                        ownerUserId = ownerUserId,
                        dayKey = normalizedDayKey,
                    ),
            )

        val rowId =
            dao.insert(
                note = note,
            )

        return note.copy(
            rowId = rowId,
        )
    }

    // MARK: - Edit

    suspend fun upsertLocal(
        note: LocalNoteEntity,
    ) {
        dao.upsert(
            note,
        )
    }

    suspend fun updateContent(
        note: LocalNoteEntity,
        title: String,
        text: String,
    ): LocalNoteEntity {
        val updated =
            note.copy(
                title = title,
                text = text,
                localUpdatedAt =
                    System.currentTimeMillis(),
                syncState =
                    nextPendingState(
                        note = note,
                    ),
                lastSyncError = null,
            )

        dao.update(
            note = updated,
        )

        return updated
    }

    suspend fun setPinned(
        note: LocalNoteEntity,
        pinned: Boolean,
    ): LocalNoteEntity {
        if (note.pinned == pinned) {
            return note
        }

        val updated =
            note.copy(
                pinned = pinned,
                localUpdatedAt =
                    System.currentTimeMillis(),
                syncState =
                    nextPendingState(
                        note = note,
                    ),
                lastSyncError = null,
            )

        dao.update(
            note = updated,
        )

        updated.ownerUserId
            ?.let {
                requestSync(
                    ownerUserId = it,
                )
            }

        return updated
    }

    suspend fun setPlacement(
        note: LocalNoteEntity,
        dayKey: String?,
        onlyDay: Boolean,
    ): LocalNoteEntity {
        val normalizedDayKey =
            dayKey
                ?.trim()
                ?.takeIf {
                    it.isNotEmpty()
                }
                ?.let {
                    normalizeDayKey(it)
                }

        require(
            !onlyDay ||
                    normalizedDayKey != null
        ) {
            "dayKey is required for day-only note"
        }

        if (
            onlyDay &&
            normalizedDayKey != null
        ) {
            val existingDayNote =
                dao.findDayOnlyNote(
                    ownerUserId =
                        requireNotNull(
                            note.ownerUserId,
                        ) {
                            "ownerUserId is required"
                        },
                    dayKey =
                        normalizedDayKey,
                )

            require(
                existingDayNote == null ||
                        existingDayNote.rowId ==
                        note.rowId
            ) {
                "A day-only note already exists for $normalizedDayKey"
            }
        }

        val updated =
            note.copy(
                dayKey = normalizedDayKey,
                onlyDay = onlyDay,
                localUpdatedAt =
                    System.currentTimeMillis(),
                syncState =
                    nextPendingState(
                        note = note,
                    ),
                lastSyncError = null,
            )

        dao.update(
            note = updated,
        )

        updated.ownerUserId
            ?.let {
                requestSync(
                    ownerUserId = it,
                )
            }

        return updated
    }

    // MARK: - Lifecycle

    suspend fun markForArchive(
        note: LocalNoteEntity,
    ): LocalNoteEntity? {
        return markForLifecycle(
            note = note,
            targetStatus =
                NoteRemoteStatus.Archived,
        )
    }

    suspend fun markForRestore(
        note: LocalNoteEntity,
    ): LocalNoteEntity? {
        return markForLifecycle(
            note = note,
            targetStatus =
                NoteRemoteStatus.Active,
        )
    }

    suspend fun markForDelete(
        note: LocalNoteEntity,
    ): LocalNoteEntity? {
        /*
         * 从未存在于远端的新 Note。
         *
         * Delete 不需要为它制造一个远端 tombstone，
         * 直接删掉 Room 即可。
         */
        if (
            note.noteId == null &&
            note.remoteVersion == null
        ) {
            dao.delete(
                note = note,
            )

            return null
        }

        /*
         * 服务器已经确认 deleted。
         */
        if (
            note.remoteStatus ==
            NoteRemoteStatus.Deleted
        ) {
            dao.delete(
                note = note,
            )

            return null
        }

        val updated =
            note.copy(
                pendingRemoteStatus =
                    NoteRemoteStatus.Deleted,

                localUpdatedAt =
                    System.currentTimeMillis(),

                syncState =
                    NoteSyncState.Pending,

                /*
                 * Delete 是终止性操作。
                 *
                 * 用户已经决定删除 Note，
                 * 尚未上传的 content snapshot
                 * 不再需要继续上传。
                 */
                pendingSnapshotJson =
                    null,

                lastSyncError =
                    null,
            )

        dao.update(
            note = updated,
        )

        return updated
    }

    /**
     * Archive / Restore 共用的 lifecycle target。
     *
     * Delete 有特殊语义，因此不走这里。
     */
    private suspend fun markForLifecycle(
        note: LocalNoteEntity,
        targetStatus: NoteRemoteStatus,
    ): LocalNoteEntity? {
        require(
            targetStatus ==
                    NoteRemoteStatus.Active ||
                    targetStatus ==
                    NoteRemoteStatus.Archived
        ) {
            "Unsupported lifecycle target: $targetStatus"
        }

        /*
         * Deleted 是终态。
         */
        require(
            note.remoteStatus !=
                    NoteRemoteStatus.Deleted &&
                    note.pendingRemoteStatus !=
                    NoteRemoteStatus.Deleted
        ) {
            "Deleted Note cannot change lifecycle state"
        }

        /*
         * 用户要求的目标状态，
         * 本来就是服务器当前确认状态。
         *
         * 典型例子：
         *
         * remoteStatus = Active
         * pendingRemoteStatus = Archived
         *
         * 用户又点“取消归档”
         *
         * → 其实不需要发送 restore，
         *   只需要取消尚未执行的 archive intent。
         */
        if (
            note.remoteStatus ==
            targetStatus
        ) {
            if (
                note.pendingRemoteStatus ==
                null
            ) {
                return note
            }

            return cancelPendingLifecycle(
                note = note,
            )
        }

        /*
         * Archive 与 Restore 不能丢掉
         * 已冻结的 content snapshot。
         *
         * 尤其 Archive：
         *
         * Pending content
         * → upsert
         * → archive
         */
        val updated =
            note.copy(
                pendingRemoteStatus =
                    targetStatus,

                localUpdatedAt =
                    System.currentTimeMillis(),

                syncState =
                    NoteSyncState.Pending,

                lastSyncError =
                    null,
            )

        dao.update(
            note = updated,
        )

        return updated
    }

    suspend fun cancelPendingLifecycle(
        note: LocalNoteEntity,
    ): LocalNoteEntity {
        if (
            note.pendingRemoteStatus ==
            null
        ) {
            return note
        }

        val updated =
            note.copy(
                pendingRemoteStatus =
                    null,

                syncState =
                    if (
                        note.hasUnsyncedChanges ||
                        note.pendingSnapshotJson !=
                        null
                    ) {
                        NoteSyncState.Pending
                    } else {
                        NoteSyncState.Synced
                    },

                lastSyncError =
                    null,
            )

        dao.update(
            note = updated,
        )

        return updated
    }

    // MARK: - Sync support

    suspend fun preparePendingSnapshot(
        ownerUserId: String,
        localKey: String,
    ): PreparedNotePush? {
        /*
         * 一定重新从 Room 读取最新 Note。
         *
         * 不使用 UI / ViewModel 手里可能已经过期的 entity。
         */
        val note =
            findByLocalKey(
                ownerUserId =
                    ownerUserId,
                localKey =
                    localKey,
            )
                ?: return null

        /*
         * Delete 是终止性 lifecycle：
         * 不再上传 content。
         *
         * Restore 针对 archived Note，
         * 也不能通过普通 upsert 修改内容。
         *
         * Archive 则不同：
         * 如果本地还有内容变化，
         * 必须先完成 content upsert，
         * 然后才能 archive。
         */
        if (
            note.pendingRemoteStatus ==
            NoteRemoteStatus.Deleted ||
            note.pendingRemoteStatus ==
            NoteRemoteStatus.Active
        ) {
            return null
        }

        /*
         * 如果已经冻结过 pending snapshot，
         * 就必须复用。
         *
         * 这样网络重试仍然使用：
         * - 同一个 requestId
         * - 同一个 baseVersion
         * - 同一份发送内容
         */
        val existingSnapshot =
            decodePendingSnapshot(
                note.pendingSnapshotJson,
            )

        if (
            existingSnapshot != null
        ) {
            val syncing =
                note.copy(
                    syncState =
                        NoteSyncState.Syncing,

                    lastSyncError =
                        null,
                )

            if (
                syncing != note
            ) {
                dao.update(
                    syncing,
                )
            }

            return PreparedNotePush(
                note =
                    syncing,

                snapshot =
                    existingSnapshot,
            )
        }

        /*
         * 第一次准备这一轮 push。
         *
         * 把当前 Note 状态冻结下来。
         */
        val snapshot =
            PersistedNoteSyncSnapshot(
                requestId =
                    UUID
                        .randomUUID()
                        .toString(),

                noteId =
                    note.noteId,

                clientKey =
                    note.clientKey,

                /*
                 * 新 Note：
                 * remoteVersion == null
                 * → baseVersion = null
                 *
                 * 已同步过的 Note：
                 * remoteVersion = N
                 * → baseVersion = N
                 */
                baseVersion =
                    note.remoteVersion,

                dayKey =
                    note.dayKey,

                onlyDay =
                    note.onlyDay,

                title =
                    note.title,

                text =
                    note.text,

                pinned =
                    note.pinned,
            )

        val encoded =
            json.encodeToString(
                snapshot,
            )

        val syncing =
            note.copy(
                pendingSnapshotJson =
                    encoded,

                syncState =
                    NoteSyncState.Syncing,

                lastSyncError =
                    null,
            )

        dao.update(
            syncing,
        )

        return PreparedNotePush(
            note =
                syncing,

            snapshot =
                snapshot,
        )
    }

    suspend fun applyPushSuccess(
        ownerUserId: String,
        localKey: String,
        sentSnapshot: PersistedNoteSyncSnapshot,
        outcome: String,
        remote: RemoteNoteSnapshot,
    ) {
        /*
         * 请求回来以后必须重新读取 Room。
         *
         * 因为在网络请求期间，
         * 用户完全可能继续编辑这条 Note。
         */
        val current =
            findByLocalKey(
                ownerUserId =
                    ownerUserId,
                localKey =
                    localKey,
            )
                ?: return

        val currentPending =
            decodePendingSnapshot(
                current.pendingSnapshotJson,
            )

        /*
         * 旧请求晚到。
         *
         * 如果 Room 中保存的 pending requestId
         * 已经不是这次发出去的 requestId，
         * 说明本地状态已经进入另一轮同步。
         *
         * 旧响应不能再修改它。
         */
        if (
            currentPending?.requestId !=
            sentSnapshot.requestId
        ) {
            return
        }

        val remoteVersion =
            remote.version.toLong()

        val remoteUpdatedAt =
            remote.updatedAt.toLong()

        /*
         * 判断用户在这一轮 push 期间
         * 有没有继续改变真正的 Note 内容。
         *
         * 注意：
         * noteId / clientKey 不参与这个判断，
         * 它们是 identity，不是用户内容。
         */
        val unchangedSinceSend =
            contentMatchesSnapshot(
                note =
                    current,
                snapshot =
                    sentSnapshot,
            )

        val requestWasApplied =
            outcome != "identity_resolved"

        /*
         * 一旦服务端返回正式 noteId，
         * 本地 identity 就收敛到正式 identity。
         *
         * rowId 不变。
         */
        val officialLocalKey =
            remoteLocalKey(
                ownerUserId =
                    ownerUserId,
                noteId =
                    remote.noteId,
            )

        val remoteClientKey =
            remote.clientKey
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: current.clientKey

        if (
            unchangedSinceSend &&
            requestWasApplied
        ) {
            /*
             * -------------------------------
             * Case A
             *
             * 发出去的是 A
             * 当前本地还是 A
             *
             * → 整条 Note 可以进入 Synced
             * -------------------------------
             */
            val synced =
                current.copy(
                    localKey =
                        officialLocalKey,

                    noteId =
                        remote.noteId,

                    clientKey =
                        remoteClientKey,

                    /*
                     * 当前内容和服务器确认内容一致，
                     * 所以直接采用服务器返回的标准状态。
                     */
                    dayKey =
                        remote.dayKey,

                    onlyDay =
                        remote.onlyDay,

                    title =
                        remote.title,

                    text =
                        remote.text,

                    pinned =
                        remote.pinned,

                    remoteStatus =
                        remote.remoteStatus,

                    /*
                     * 服务器确认后的内容
                     * 成为新的 synced baseline。
                     */
                    syncedDayKey =
                        remote.dayKey,

                    syncedOnlyDay =
                        remote.onlyDay,

                    syncedTitle =
                        remote.title,

                    syncedText =
                        remote.text,

                    syncedPinned =
                        remote.pinned,

                    remoteVersion =
                        remoteVersion,

                    remoteUpdatedAt =
                        remoteUpdatedAt,

                    /*
                     * 这一轮已经确认完成。
                     */
                    pendingSnapshotJson =
                        null,

                    syncState =
                        if (
                            current.pendingRemoteStatus !=
                            null
                        ) {
                            NoteSyncState.Pending
                        } else {
                            NoteSyncState.Synced
                        },

                    lastSyncError =
                        null,
                )

            dao.update(
                synced,
            )

            if (
                synced.pendingRemoteStatus !=
                null
            ) {
                requestSync(
                    ownerUserId =
                        ownerUserId,
                )
            }

            return
        }

        /*
         * -------------------------------
         * Case B
         *
         * 发出去的是 A
         * 用户期间已经编辑成 B
         *
         * 服务端只确认了 A。
         *
         * 所以：
         *
         * current 内容 B 保留
         * baseline 更新为远端 A
         * remoteVersion 更新
         * 旧 pending snapshot A 清掉
         * 状态重新 Pending
         *
         * 下一轮再冻结 B。
         * -------------------------------
         */
        val stillPending =
            current.copy(
                localKey =
                    officialLocalKey,

                noteId =
                    remote.noteId,

                clientKey =
                    remoteClientKey,

                remoteStatus =
                    remote.remoteStatus,

                syncedDayKey =
                    remote.dayKey,

                syncedOnlyDay =
                    remote.onlyDay,

                syncedTitle =
                    remote.title,

                syncedText =
                    remote.text,

                syncedPinned =
                    remote.pinned,

                remoteVersion =
                    remoteVersion,

                remoteUpdatedAt =
                    remoteUpdatedAt,

                pendingSnapshotJson =
                    null,

                syncState =
                    NoteSyncState.Pending,

                lastSyncError =
                    null,
            )

        dao.update(
            stillPending,
        )
    }

    suspend fun applyLifecycleSuccess(
        ownerUserId: String,
        localKey: String,
        expectedStatus: NoteRemoteStatus,
        remote: RemoteNoteSnapshot,
    ) {
        val current =
            findByLocalKey(
                ownerUserId =
                    ownerUserId,

                localKey =
                    localKey,
            )
                ?: return

        /*
         * lifecycle response 晚到了。
         *
         * 当前用户已经改变了 lifecycle target，
         * 旧 response 不能再修改当前 Note。
         *
         * 例如：
         *
         * pending = Archived
         * archive request 发出
         *
         * 用户随后又取消归档：
         * pending = null / Active
         *
         * 此时旧 archive response 回来，
         * 必须忽略。
         */
        if (
            current.pendingRemoteStatus !=
            expectedStatus
        ) {
            return
        }

        /*
         * 服务端最终状态必须符合
         * 当前 lifecycle mutation 的目标。
         *
         * 如果不一致，
         * 现在已经有完整 remote snapshot，
         * 应保存为真正的 Conflict，
         * 而不是只留下一个错误字符串。
         */
        if (
            remote.remoteStatus !=
            expectedStatus
        ) {
            markConflictFromRemote(
                note =
                    current,

                remote =
                    remote,

                message =
                    "生命周期操作返回的远端状态不符合预期：" +
                            "expected=${expectedStatus.rawValue}, " +
                            "actual=${remote.remoteStatus.rawValue}",
            )

            return
        }

        /*
         * ------------------------------------------------
         * Delete
         * ------------------------------------------------
         *
         * Deleted 是终态。
         *
         * 服务端 tombstone 已确认，
         * Room cache 可以直接删除。
         */
        if (
            expectedStatus ==
            NoteRemoteStatus.Deleted
        ) {
            dao.delete(
                note =
                    current,
            )

            return
        }

        val remoteVersion =
            remote.version.toLong()

        val remoteUpdatedAt =
            remote.updatedAt.toLong()

        /*
         * lifecycle mutation 本身不会修改 Note 内容。
         *
         * 比较 lifecycle response 返回的 remote snapshot
         * 与当前本地内容，
         * 判断 lifecycle 请求期间是否仍存在本地内容差异。
         */
        val hasLocalContentChanges =
            current.dayKey !=
                    remote.dayKey ||
                    current.onlyDay !=
                    remote.onlyDay ||
                    current.title !=
                    remote.title ||
                    current.text !=
                    remote.text ||
                    current.pinned !=
                    remote.pinned

        /*
         * ------------------------------------------------
         * Archive safety
         * ------------------------------------------------
         *
         * Archive 的正常 pipeline 是：
         *
         * local content dirty
         * → 先 upsert
         * → content clean
         * → 再 archive
         *
         * 所以 archive 成功时，
         * current 和 remote 理论上必须一致。
         *
         * 如果这里仍然发现内容不同，
         * 说明 lifecycle 请求期间发生了异常竞态，
         * 不能直接标记 Synced，
         * 因为 Archived Note 又不能继续普通 upsert。
         *
         * 这种情况进入 Conflict 最安全。
         */
        if (
            expectedStatus ==
            NoteRemoteStatus.Archived &&
            hasLocalContentChanges
        ) {
            markConflictFromRemote(
                note =
                    current,

                remote =
                    remote,

                message =
                    "记录已在云端归档，但本机仍存在未同步修改",
            )

            return
        }

        /*
         * ------------------------------------------------
         * Archive / Restore success
         * ------------------------------------------------
         *
         * 这里有一个非常重要的原则：
         *
         * lifecycle mutation 只负责 lifecycle，
         * 不负责覆盖当前用户正在保留的本地内容。
         *
         * 所以：
         *
         * current title/text/dayKey/pinned
         * → 全部保持不动
         *
         * remote snapshot
         * → 更新 synced baseline
         *
         * 这对 Conflict 的：
         *
         * Remote Archived
         * → Keep Local
         * → Restore
         *
         * 尤其重要。
         */
        val nextSyncState =
            if (
                expectedStatus ==
                NoteRemoteStatus.Active &&
                hasLocalContentChanges
            ) {
                /*
                 * Restore 成功，
                 * 但 current local 内容仍不同于 remote。
                 *
                 * 说明这是：
                 *
                 * restore 完成
                 * → 接下来还要继续 push 本地内容。
                 */
                NoteSyncState.Pending
            } else {
                NoteSyncState.Synced
            }

        val updated =
            current.copy(
                noteId =
                    remote.noteId,

                clientKey =
                    remote.clientKey
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?: current.clientKey,

                /*
                 * lifecycle 已经由服务器确认。
                 */
                remoteStatus =
                    remote.remoteStatus,

                pendingRemoteStatus =
                    null,

                /*
                 * current 内容故意不覆盖。
                 *
                 * 服务器 snapshot 只成为
                 * 新的 synced baseline。
                 */
                syncedDayKey =
                    remote.dayKey,

                syncedOnlyDay =
                    remote.onlyDay,

                syncedTitle =
                    remote.title,

                syncedText =
                    remote.text,

                syncedPinned =
                    remote.pinned,

                remoteVersion =
                    remoteVersion,

                remoteUpdatedAt =
                    remoteUpdatedAt,

                /*
                 * lifecycle 成功以后，
                 * 旧 content frozen request
                 * 不应该继续保留。
                 *
                 * 如果仍有本地修改，
                 * 下一轮会基于新的 remoteVersion
                 * 重新冻结 snapshot。
                 */
                pendingSnapshotJson =
                    null,

                /*
                 * 当前 lifecycle 已经明确成功，
                 * 之前的 conflict remote snapshot
                 * 也已经失效。
                 */
                conflictSnapshotJson =
                    null,

                syncState =
                    nextSyncState,

                lastSyncError =
                    null,
            )

        dao.update(
            note =
                updated,
        )

        /*
         * Restore 后如果仍有本地内容，
         * 立即唤醒下一轮 content sync。
         *
         * 例如：
         *
         * remote Archived C / v6
         * local B
         *
         * 用户选择“保留本地”
         *
         * → restore v7 Active
         * → current B 保留
         * → baseline = remote C
         * → Pending
         * → requestSync
         * → upsert B based on v7
         */
        if (
            updated.syncState ==
            NoteSyncState.Pending
        ) {
            requestSync(
                ownerUserId =
                    ownerUserId,
            )
        }
    }

    suspend fun applyPushFailure(
        ownerUserId: String,
        localKey: String,
        sentSnapshot: PersistedNoteSyncSnapshot,
        failure: NoteSyncFailure,
    ) {
        val current =
            findByLocalKey(
                ownerUserId = ownerUserId,
                localKey = localKey,
            )
                ?: return

        val currentPending =
            decodePendingSnapshot(
                current.pendingSnapshotJson,
            )

        /*
         * 迟到的旧请求不能修改新一轮状态。
         */
        if (
            currentPending?.requestId !=
            sentSnapshot.requestId
        ) {
            return
        }

        val updated =
            when (failure) {
                is NoteSyncFailure.Retryable ->
                    current.copy(
                        syncState =
                            NoteSyncState.Waiting,

                        /*
                         * 保留 pendingSnapshotJson。
                         * 下一轮继续使用相同 requestId。
                         */
                        lastSyncError =
                            failure.message,
                    )

                is NoteSyncFailure.Conflict ->
                    current.copy(
                        syncState =
                            NoteSyncState.Conflict,

                        /*
                         * Conflict 暂时也保留 snapshot，
                         * 后续 conflict resolver 会用到。
                         */
                        lastSyncError =
                            failure.message,
                    )

                is NoteSyncFailure.RemoteDeleted ->
                    current.copy(
                        remoteStatus =
                            NoteRemoteStatus.Deleted,

                        syncState =
                            NoteSyncState.Conflict,

                        lastSyncError =
                            failure.message,
                    )

                is NoteSyncFailure.RemoteArchived ->
                    current.copy(
                        remoteStatus =
                            NoteRemoteStatus.Archived,

                        syncState =
                            NoteSyncState.Conflict,

                        lastSyncError =
                            failure.message,
                    )
            }

        dao.update(
            updated,
        )
    }

    suspend fun applyLifecycleFailure(
        ownerUserId: String,
        localKey: String,
        expectedStatus: NoteRemoteStatus,
        failure: NoteSyncFailure,
    ) {
        val current =
            findByLocalKey(
                ownerUserId =
                    ownerUserId,

                localKey =
                    localKey,
            )
                ?: return

        /*
         * 只处理仍然对应当前 lifecycle intent 的响应。
         */
        if (
            current.pendingRemoteStatus !=
            expectedStatus
        ) {
            return
        }

        val updated =
            when (failure) {
                is NoteSyncFailure.Retryable ->
                    current.copy(
                        syncState =
                            NoteSyncState.Waiting,

                        lastSyncError =
                            failure.message,
                    )

                is NoteSyncFailure.Conflict ->
                    current.copy(
                        syncState =
                            NoteSyncState.Conflict,

                        lastSyncError =
                            failure.message,
                    )

                is NoteSyncFailure.RemoteDeleted -> {
                    /*
                     * 本地本来就是要删除，
                     * 服务端已经 deleted，
                     * lifecycle goal 已经达成。
                     */
                    if (
                        expectedStatus ==
                        NoteRemoteStatus.Deleted
                    ) {
                        dao.delete(
                            note =
                                current,
                        )

                        return
                    }

                    current.copy(
                        remoteStatus =
                            NoteRemoteStatus.Deleted,

                        syncState =
                            NoteSyncState.Conflict,

                        lastSyncError =
                            failure.message,
                    )
                }

                is NoteSyncFailure.RemoteArchived ->
                    current.copy(
                        remoteStatus =
                            NoteRemoteStatus.Archived,

                        syncState =
                            NoteSyncState.Conflict,

                        lastSyncError =
                            failure.message,
                    )
            }

        dao.update(
            note =
                updated,
        )
    }

    // MARK: - Conflict resolution

    suspend fun resolveConflictUsingRemote(
        note: LocalNoteEntity,
    ): LocalNoteEntity? {
        val conflict =
            decodeConflictSnapshot(
                note.conflictSnapshotJson,
            )
                ?: return note

        /*
         * Remote Deleted：
         *
         * 用户选择使用云端版本，
         * 云端版本就是“这条 Note 已删除”。
         *
         * 因此直接清掉本地缓存。
         */
        if (
            conflict.parsedRemoteStatus ==
            NoteRemoteStatus.Deleted
        ) {
            dao.delete(
                note = note,
            )

            return null
        }

        /*
         * Active / Archived：
         *
         * current + baseline
         * 全部收敛到 conflict remote snapshot。
         */
        val updated =
            note.copy(
                noteId =
                    conflict.noteId,

                clientKey =
                    conflict.clientKey
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?: note.clientKey,

                dayKey =
                    conflict.dayKey,

                onlyDay =
                    conflict.onlyDay,

                title =
                    conflict.title,

                text =
                    conflict.text,

                pinned =
                    conflict.pinned,

                remoteStatus =
                    conflict.parsedRemoteStatus,

                pendingRemoteStatus =
                    null,

                syncedDayKey =
                    conflict.dayKey,

                syncedOnlyDay =
                    conflict.onlyDay,

                syncedTitle =
                    conflict.title,

                syncedText =
                    conflict.text,

                syncedPinned =
                    conflict.pinned,

                remoteVersion =
                    conflict.version,

                remoteUpdatedAt =
                    conflict.updatedAt,

                pendingSnapshotJson =
                    null,

                conflictSnapshotJson =
                    null,

                /*
                 * 使用云端版本属于一次本地状态切换，
                 * 这里更新时间即可。
                 */
                localUpdatedAt =
                    System.currentTimeMillis(),

                syncState =
                    NoteSyncState.Synced,

                lastSyncError =
                    null,
            )

        dao.update(
            note =
                updated,
        )

        return updated
    }

    suspend fun resolveConflictKeepingLocal(
        note: LocalNoteEntity,
    ): LocalNoteEntity? {
        val conflict =
            decodeConflictSnapshot(
                note.conflictSnapshotJson,
            )
                ?: return note

        return when (
            conflict.parsedRemoteStatus
        ) {
            NoteRemoteStatus.Active ->
                resolveKeepLocalAgainstActiveRemote(
                    note =
                        note,

                    conflict =
                        conflict,
                )

            NoteRemoteStatus.Archived ->
                resolveKeepLocalAgainstArchivedRemote(
                    note =
                        note,

                    conflict =
                        conflict,
                )

            NoteRemoteStatus.Deleted ->
                resolveKeepLocalAgainstDeletedRemote(
                    note =
                        note,

                    conflict =
                        conflict,
                )
        }
    }

    private suspend fun resolveKeepLocalAgainstActiveRemote(
        note: LocalNoteEntity,
        conflict: PersistedNoteConflictSnapshot,
    ): LocalNoteEntity {
        /*
         * 当前 title/text/dayKey/pinned
         * 全部保留。
         *
         * 但 synced baseline 必须推进到
         * 最新 remote snapshot。
         *
         * 这样下一轮：
         *
         * current local B
         * baseline remote C
         *
         * → hasUnsyncedChanges = true
         * → baseVersion = conflict.version
         * → 正常 push。
         */
        val updated =
            note.copy(
                noteId =
                    conflict.noteId,

                clientKey =
                    conflict.clientKey
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?: note.clientKey,

                remoteStatus =
                    NoteRemoteStatus.Active,

                pendingRemoteStatus =
                    null,

                syncedDayKey =
                    conflict.dayKey,

                syncedOnlyDay =
                    conflict.onlyDay,

                syncedTitle =
                    conflict.title,

                syncedText =
                    conflict.text,

                syncedPinned =
                    conflict.pinned,

                remoteVersion =
                    conflict.version,

                remoteUpdatedAt =
                    conflict.updatedAt,

                /*
                 * 原来的 frozen request 基于旧 version，
                 * 已经不能再用。
                 */
                pendingSnapshotJson =
                    null,

                conflictSnapshotJson =
                    null,

                localUpdatedAt =
                    System.currentTimeMillis(),

                syncState =
                    NoteSyncState.Pending,

                lastSyncError =
                    null,
            )

        dao.update(
            note =
                updated,
        )

        updated.ownerUserId
            ?.let { owner ->
                requestSync(
                    ownerUserId =
                        owner,
                )
            }

        return updated
    }

    private suspend fun resolveKeepLocalAgainstArchivedRemote(
        note: LocalNoteEntity,
        conflict: PersistedNoteConflictSnapshot,
    ): LocalNoteEntity {
        /*
         * 当前本地内容继续保留。
         *
         * 服务器真实状态先收敛为 Archived，
         * 然后留下：
         *
         * pendingRemoteStatus = Active
         *
         * 表示先恢复。
         */
        val updated =
            note.copy(
                noteId =
                    conflict.noteId,

                clientKey =
                    conflict.clientKey
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?: note.clientKey,

                remoteStatus =
                    NoteRemoteStatus.Archived,

                pendingRemoteStatus =
                    NoteRemoteStatus.Active,

                /*
                 * baseline 使用最新云端 Archived snapshot。
                 *
                 * current 本地内容不动，
                 * 所以本地修改仍然存在。
                 */
                syncedDayKey =
                    conflict.dayKey,

                syncedOnlyDay =
                    conflict.onlyDay,

                syncedTitle =
                    conflict.title,

                syncedText =
                    conflict.text,

                syncedPinned =
                    conflict.pinned,

                remoteVersion =
                    conflict.version,

                remoteUpdatedAt =
                    conflict.updatedAt,

                pendingSnapshotJson =
                    null,

                conflictSnapshotJson =
                    null,

                localUpdatedAt =
                    System.currentTimeMillis(),

                syncState =
                    NoteSyncState.Pending,

                lastSyncError =
                    null,
            )

        dao.update(
            note =
                updated,
        )

        updated.ownerUserId
            ?.let { owner ->
                requestSync(
                    ownerUserId =
                        owner,
                )
            }

        return updated
    }

    private suspend fun resolveKeepLocalAgainstDeletedRemote(
        note: LocalNoteEntity,
        conflict: PersistedNoteConflictSnapshot,
    ): LocalNoteEntity {
        val owner =
            requireNotNull(
                note.ownerUserId,
            ) {
                "ownerUserId is required"
            }

        /*
         * Deleted remote identity 是终态。
         *
         * “保留本地”不是复活旧 Note，
         * 而是把当前本地内容转成一条全新的 Note。
         */
        val newLocalKey =
            LocalNoteKeys.new(
                ownerUserId =
                    owner,
            )

        val updated =
            note.copy(
                localKey =
                    newLocalKey,

                noteId =
                    null,

                clientKey =
                    UUID
                        .randomUUID()
                        .toString(),

                remoteStatus =
                    NoteRemoteStatus.Active,

                pendingRemoteStatus =
                    null,

                /*
                 * 新 Note 尚未有 server baseline。
                 *
                 * 这里用空 baseline，
                 * 确保当前内容会被识别为 dirty。
                 */
                syncedDayKey =
                    null,

                syncedOnlyDay =
                    false,

                syncedTitle =
                    "",

                syncedText =
                    "",

                syncedPinned =
                    false,

                remoteVersion =
                    null,

                remoteUpdatedAt =
                    null,

                pendingSnapshotJson =
                    null,

                conflictSnapshotJson =
                    null,

                localUpdatedAt =
                    System.currentTimeMillis(),

                syncState =
                    NoteSyncState.Pending,

                lastSyncError =
                    null,
            )

        /*
         * rowId 保持不变。
         *
         * Room 里还是同一行，
         * 只是从旧 remote identity
         * 转成新的本地 identity。
         */
        dao.update(
            note =
                updated,
        )

        requestSync(
            ownerUserId =
                owner,
        )

        return updated
    }


    suspend fun markConflictFromRemote(
        note: LocalNoteEntity,
        remote: RemoteNoteSnapshot,
        message: String,
    ): LocalNoteEntity {
        val remoteVersion =
            remote.version.toLong()

        val remoteUpdatedAt =
            remote.updatedAt.toLong()

        /*
         * 如果当前已经保存过更高版本的 conflict，
         * 晚到的旧 Pull 不能覆盖它。
         */
        val existingConflict =
            decodeConflictSnapshot(
                note.conflictSnapshotJson,
            )

        if (
            existingConflict != null &&
            existingConflict.version >
            remoteVersion
        ) {
            return note
        }

        val conflictSnapshot =
            PersistedNoteConflictSnapshot(
                noteId =
                    remote.noteId,

                clientKey =
                    remote.clientKey,

                dayKey =
                    remote.dayKey,

                onlyDay =
                    remote.onlyDay,

                title =
                    remote.title,

                text =
                    remote.text,

                pinned =
                    remote.pinned,

                remoteStatus =
                    remote.remoteStatus.rawValue,

                version =
                    remoteVersion,

                updatedAt =
                    remoteUpdatedAt,
            )

        val encoded =
            json.encodeToString(
                conflictSnapshot,
            )

        /*
         * 注意这里故意不更新：
         *
         * - current title/text
         * - synced baseline
         * - remoteVersion
         * - remoteStatus
         * - pendingSnapshotJson
         *
         * 因为冲突尚未解决。
         *
         * remote 的真正状态保存在
         * conflictSnapshotJson 中。
         */
        val updated =
            note.copy(
                conflictSnapshotJson =
                    encoded,

                remoteUpdatedAt =
                    maxOf(
                        note.remoteUpdatedAt
                            ?: 0L,
                        remoteUpdatedAt,
                    ),

                syncState =
                    NoteSyncState.Conflict,

                lastSyncError =
                    message,
            )

        dao.upsert(
            updated,
        )

        return updated
    }

    fun decodeConflictSnapshot(
        value: String?,
    ): PersistedNoteConflictSnapshot? {
        if (
            value.isNullOrBlank()
        ) {
            return null
        }

        return json.decodeFromString<
                PersistedNoteConflictSnapshot
                >(
            value,
        )
    }

    private fun decodePendingSnapshot(
        value: String?,
    ): PersistedNoteSyncSnapshot? {
        if (
            value.isNullOrBlank()
        ) {
            return null
        }

        return json.decodeFromString<
                PersistedNoteSyncSnapshot
                >(
            value
        )
    }

    private fun contentMatchesSnapshot(
        note:
        LocalNoteEntity,
        snapshot:
        PersistedNoteSyncSnapshot,
    ): Boolean {
        return note.dayKey ==
                snapshot.dayKey &&
                note.onlyDay ==
                snapshot.onlyDay &&
                note.title ==
                snapshot.title &&
                note.text ==
                snapshot.text &&
                note.pinned ==
                snapshot.pinned
    }

    suspend fun findNotesNeedingSync(
        ownerUserId: String,
    ): List<LocalNoteEntity> {
        return dao.findNotesNeedingSync(
            ownerUserId = ownerUserId,
        )
    }

    private fun remoteLocalKey(
        ownerUserId: String,
        noteId: String,
    ): String {
        return "user:$ownerUserId:note:$noteId"
    }

    suspend fun updateForSync(
        note: LocalNoteEntity,
    ) {
        dao.update(
            note = note,
        )
    }

    suspend fun deleteLocal(
        note: LocalNoteEntity,
    ) {
        dao.delete(
            note = note,
        )
    }

    // MARK: - Factory

    private fun makeNewLocalNote(
        ownerUserId: String,
        dayKey: String?,
        onlyDay: Boolean,
        localKey: String =
            LocalNoteKeys.new(
                ownerUserId = ownerUserId,
            ),
    ): LocalNoteEntity {
        val now =
            System.currentTimeMillis()

        return LocalNoteEntity(
            localKey = localKey,

            ownerUserId = ownerUserId,

            noteId = null,
            clientKey =
                UUID.randomUUID().toString(),

            dayKey = dayKey,
            onlyDay = onlyDay,

            title = "",
            text = "",
            pinned = false,

            remoteStatus =
                NoteRemoteStatus.Active,

            pendingRemoteStatus =
                null,

            syncedDayKey = null,
            syncedOnlyDay = false,

            syncedTitle = "",
            syncedText = "",
            syncedPinned = false,

            remoteVersion = null,
            remoteUpdatedAt = null,

            pendingSnapshotJson =
                null,

            conflictSnapshotJson =
                null,

            localUpdatedAt = now,

            syncState =
                NoteSyncState.Pending,

            lastSyncError = null,
        )
    }

    // MARK: - Helpers

    private fun nextPendingState(
        note: LocalNoteEntity,
    ): NoteSyncState {
        return when (
            note.syncState
        ) {
            NoteSyncState.Syncing ->
                NoteSyncState.Syncing

            NoteSyncState.Conflict ->
                NoteSyncState.Conflict

            else ->
                NoteSyncState.Pending
        }
    }

    private fun normalizeDayKey(
        value: String,
    ): String {
        val normalized =
            value.trim()

        require(
            DayKey.isValid(
                normalized,
            )
        ) {
            "Invalid dayKey: $value"
        }

        return normalized
    }
}