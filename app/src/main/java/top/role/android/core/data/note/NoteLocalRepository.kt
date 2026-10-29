package top.role.android.core.data.note

import java.util.UUID
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import top.role.android.core.database.note.LocalNoteDao
import top.role.android.core.database.note.LocalNoteEntity
import top.role.android.core.database.note.hasPendingSnapshot
import top.role.android.core.database.note.hasUnsyncedChanges
import top.role.android.core.database.note.hasUnsyncedContentChanges
import top.role.android.core.model.note.DayKey
import top.role.android.core.model.note.NoteRemoteStatus
import top.role.android.core.model.note.NoteSyncFailure
import top.role.android.core.model.note.NoteSyncState
import top.role.android.core.model.note.PersistedNoteConflictSnapshot
import top.role.android.core.model.note.PersistedNoteSyncSnapshot
import top.role.android.core.model.note.RemoteNoteSnapshot
import top.role.android.core.model.note.content.NoteContentCodec

class NoteLocalRepository(private val dao: LocalNoteDao) {
    private val json = Json { ignoreUnknownKeys = true }
    private val _syncRequests = MutableSharedFlow<String>(
        extraBufferCapacity = 32,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val syncRequests: SharedFlow<String> = _syncRequests.asSharedFlow()

    fun requestSync(ownerUserId: String) {
        if (ownerUserId.isNotBlank()) _syncRequests.tryEmit(ownerUserId)
    }

    // MARK: - Observe / Read

    fun observeRecentNotes(ownerUserId: String): Flow<List<LocalNoteEntity>> = dao.observeRecentNotes(ownerUserId)
    fun observePinnedNotes(ownerUserId: String): Flow<List<LocalNoteEntity>> = dao.observePinnedNotes(ownerUserId)
    fun observeDiaryNotes(ownerUserId: String): Flow<List<LocalNoteEntity>> = dao.observeDiaryNotes(ownerUserId)
    fun observeArchivedNotes(ownerUserId: String): Flow<List<LocalNoteEntity>> = dao.observeArchivedNotes(ownerUserId)
    fun observeMonthNotesForDayHome(
        ownerUserId: String,
        startDayKey: String,
        endDayKeyExclusive: String,
    ): Flow<List<LocalNoteEntity>> = dao.observeMonthNotesForDayHome(
        ownerUserId, startDayKey, endDayKeyExclusive
    )
    fun observeNotesForDay(ownerUserId: String, dayKey: String): Flow<List<LocalNoteEntity>> =
        dao.observeNotesForDay(ownerUserId, dayKey)
    fun observeDayOnlyNote(ownerUserId: String, dayKey: String): Flow<LocalNoteEntity?> =
        dao.observeDayOnlyNote(ownerUserId, dayKey)
    fun observeByLocalKey(ownerUserId: String, localKey: String): Flow<LocalNoteEntity?> =
        dao.observeByLocalKey(ownerUserId, localKey)
    fun searchNotes(ownerUserId: String, query: String): Flow<List<LocalNoteEntity>> =
        dao.searchNotes(ownerUserId, query.trim())

    suspend fun findByLocalKey(ownerUserId: String, localKey: String): LocalNoteEntity? =
        dao.findByLocalKey(ownerUserId, localKey)
    suspend fun findByNoteId(ownerUserId: String, noteId: String): LocalNoteEntity? =
        dao.findByNoteId(ownerUserId, noteId)
    suspend fun findByClientKey(ownerUserId: String, clientKey: String): LocalNoteEntity? =
        dao.findByClientKey(ownerUserId, clientKey)
    suspend fun findDayOnlyNote(ownerUserId: String, dayKey: String): LocalNoteEntity? =
        dao.findDayOnlyNote(ownerUserId, normalizeDayKey(dayKey))
    suspend fun findRemoteBackedNotes(ownerUserId: String): List<LocalNoteEntity> {
        require(ownerUserId.isNotBlank()) { "OWNER_USER_ID_REQUIRED" }
        return dao.findRemoteBackedNotes(ownerUserId)
    }
    suspend fun findNotesNeedingSync(ownerUserId: String): List<LocalNoteEntity> =
        dao.findNotesNeedingSync(ownerUserId)

    // MARK: - Create / Edit

    suspend fun createNormalNote(ownerUserId: String): LocalNoteEntity =
        insertNewNote(ownerUserId, dayKey = null, onlyDay = false)

    suspend fun createDatedNote(ownerUserId: String, dayKey: String): LocalNoteEntity =
        insertNewNote(ownerUserId, dayKey = normalizeDayKey(dayKey), onlyDay = false)

    suspend fun getOrCreateDayOnlyNote(ownerUserId: String, dayKey: String): LocalNoteEntity {
        val key = normalizeDayKey(dayKey)
        dao.findDayOnlyNote(ownerUserId, key)?.let { return it }
        return insertNewNote(
            ownerUserId, key, onlyDay = true,
            localKey = LocalNoteKeys.day(ownerUserId = ownerUserId, dayKey = key),
        )
    }

    private suspend fun insertNewNote(
        ownerUserId: String,
        dayKey: String?,
        onlyDay: Boolean,
        localKey: String = LocalNoteKeys.new(ownerUserId = ownerUserId),
    ): LocalNoteEntity {
        val note = makeNewLocalNote(ownerUserId, dayKey, onlyDay, localKey)
        return note.copy(rowId = dao.insert(note))
    }

    suspend fun upsertLocal(note: LocalNoteEntity) = dao.upsert(note)

    /** 旧的纯文本入口（如 AI 替换）：正文变化时同步重建 BlockContent，不留下旧格式。 */
    suspend fun updateContent(note: LocalNoteEntity, title: String, text: String): LocalNoteEntity {
        if (note.title == title && note.text == text) return note
        val nextContentJson = if (note.text != text) {
            NoteContentCodec.stringify(NoteContentCodec.fromPlainText(text))
        } else {
            note.contentJson
        }
        val updated = note.copy(
            title = title, text = text, contentJson = nextContentJson,
            localUpdatedAt = System.currentTimeMillis(),
            syncState = nextPendingState(note), lastSyncError = null,
        )
        dao.update(updated)
        return updated
    }

    /** 富文本是正文权威；text 始终由 BlockContent 提取。 */
    suspend fun updateRichContent(
        note: LocalNoteEntity,
        title: String,
        text: String,
        contentJson: String,
    ): LocalNoteEntity {
        val content = requireNotNull(NoteContentCodec.parse(contentJson)) { "NOTE_CONTENT_REQUIRED" }
        val extractedText = NoteContentCodec.extractText(content)
        val normalizedText = if (text == extractedText) text else extractedText
        val normalizedJson = NoteContentCodec.stringify(content)
        if (note.title == title && note.text == normalizedText &&
            NoteContentCodec.sameContent(note.contentJson, normalizedJson)
        ) return note

        val updated = note.copy(
            title = title, text = normalizedText, contentJson = normalizedJson,
            localUpdatedAt = System.currentTimeMillis(),
            syncState = nextPendingState(note), lastSyncError = null,
        )
        dao.update(updated)
        return updated
    }

    suspend fun setPinned(note: LocalNoteEntity, pinned: Boolean): LocalNoteEntity {
        if (note.pinned == pinned) return note
        val updated = note.copy(
            pinned = pinned, localUpdatedAt = System.currentTimeMillis(),
            syncState = nextPendingState(note), lastSyncError = null,
        )
        dao.update(updated)
        updated.ownerUserId?.let(::requestSync)
        return updated
    }

    suspend fun setPlacement(note: LocalNoteEntity, dayKey: String?, onlyDay: Boolean): LocalNoteEntity {
        val key = dayKey?.trim()?.takeIf(String::isNotEmpty)?.let(::normalizeDayKey)
        require(!onlyDay || key != null) { "dayKey is required for day-only note" }
        if (onlyDay && key != null) {
            val owner = requireNotNull(note.ownerUserId) { "ownerUserId is required" }
            val existing = dao.findDayOnlyNote(owner, key)
            require(existing == null || existing.rowId == note.rowId) {
                "A day-only note already exists for $key"
            }
        }
        if (note.dayKey == key && note.onlyDay == onlyDay) return note
        val updated = note.copy(
            dayKey = key, onlyDay = onlyDay, localUpdatedAt = System.currentTimeMillis(),
            syncState = nextPendingState(note), lastSyncError = null,
        )
        dao.update(updated)
        updated.ownerUserId?.let(::requestSync)
        return updated
    }

    // MARK: - Pinned confirmation

    suspend fun applyPinnedSuccess(
        ownerUserId: String, localKey: String, noteId: String, sentPinned: Boolean,
    ) {
        val current = findByLocalKey(ownerUserId, localKey) ?: return
        if (current.noteId != noteId || current.syncState == NoteSyncState.Conflict) return

        // 本次 mutation 确认的是 sentPinned，而不是请求返回时的 current.pinned。
        val stillPending = current.pinned != sentPinned || hasPendingContentWork(current) ||
                current.pendingRemoteStatus != null
        dao.update(current.copy(
            syncedPinned = sentPinned,
            syncState = if (stillPending) NoteSyncState.Pending else NoteSyncState.Synced,
            lastSyncError = null,
        ))
        if (stillPending) requestSync(ownerUserId)
    }

    suspend fun applyPinnedFailure(
        ownerUserId: String, localKey: String, noteId: String,
        sentPinned: Boolean, failure: NoteSyncFailure,
    ) {
        val current = findByLocalKey(ownerUserId, localKey) ?: return
        if (current.noteId != noteId || current.pinned != sentPinned ||
            current.syncState == NoteSyncState.Conflict
        ) return
        dao.update(applyFailure(current, failure))
    }

    // MARK: - Lifecycle intent

    suspend fun markForArchive(note: LocalNoteEntity): LocalNoteEntity? =
        markForLifecycle(note, NoteRemoteStatus.Archived)

    suspend fun markForRestore(note: LocalNoteEntity): LocalNoteEntity? =
        markForLifecycle(note, NoteRemoteStatus.Active)

    suspend fun markForDelete(note: LocalNoteEntity): LocalNoteEntity? {
        if (note.noteId == null && note.remoteVersion == null) {
            dao.delete(note)
            return null
        }
        if (note.remoteStatus == NoteRemoteStatus.Deleted) {
            dao.delete(note)
            return null
        }
        // 删除是终止操作，旧 Content snapshot 不再需要上传。
        val updated = note.copy(
            pendingRemoteStatus = NoteRemoteStatus.Deleted,
            pendingSnapshotJson = null,
            localUpdatedAt = System.currentTimeMillis(),
            syncState = NoteSyncState.Pending, lastSyncError = null,
        )
        dao.update(updated)
        updated.ownerUserId?.let(::requestSync)
        return updated
    }

    private suspend fun markForLifecycle(
        note: LocalNoteEntity, targetStatus: NoteRemoteStatus,
    ): LocalNoteEntity? {
        require(targetStatus == NoteRemoteStatus.Active || targetStatus == NoteRemoteStatus.Archived) {
            "Unsupported lifecycle target: $targetStatus"
        }
        require(note.remoteStatus != NoteRemoteStatus.Deleted &&
                note.pendingRemoteStatus != NoteRemoteStatus.Deleted
        ) { "Deleted Note cannot change lifecycle state" }

        if (note.remoteStatus == targetStatus) {
            if (note.pendingRemoteStatus == null) return note
            // 已发出的归档可能先于本地确认到达服务器。对于有远端身份的 Note，
            // “恢复”保留为 Active 目标，而不只是取消本地 Archived Pending。
            if (targetStatus == NoteRemoteStatus.Active && note.noteId != null) {
                val updated = note.copy(
                    pendingRemoteStatus = NoteRemoteStatus.Active,
                    localUpdatedAt = System.currentTimeMillis(),
                    syncState = NoteSyncState.Pending, lastSyncError = null,
                )
                dao.update(updated)
                updated.ownerUserId?.let(::requestSync)
                return updated
            }
            return cancelPendingLifecycle(note)
        }
        if (note.pendingRemoteStatus == targetStatus) return note
        val updated = note.copy(
            pendingRemoteStatus = targetStatus,
            localUpdatedAt = System.currentTimeMillis(),
            syncState = NoteSyncState.Pending, lastSyncError = null,
        )
        dao.update(updated)
        updated.ownerUserId?.let(::requestSync)
        return updated
    }

    suspend fun cancelPendingLifecycle(note: LocalNoteEntity): LocalNoteEntity {
        if (note.pendingRemoteStatus == null) return note
        val updated = note.copy(
            pendingRemoteStatus = null,
            syncState = when {
                note.syncState == NoteSyncState.Conflict -> NoteSyncState.Conflict
                note.noteId == null || note.hasPendingSnapshot || note.hasUnsyncedChanges -> NoteSyncState.Pending
                else -> NoteSyncState.Synced
            },
            lastSyncError = if (note.syncState == NoteSyncState.Conflict) note.lastSyncError else null,
        )
        dao.update(updated)
        // 取消未确认的操作也重新 Pull，以便吸收已经到达远端的状态。
        updated.ownerUserId?.let(::requestSync)
        return updated
    }

    // MARK: - Content Push / baseline

    suspend fun preparePendingSnapshot(ownerUserId: String, localKey: String): PreparedNotePush? {
        val note = findByLocalKey(ownerUserId, localKey) ?: return null
        if (note.pendingRemoteStatus == NoteRemoteStatus.Deleted ||
            note.pendingRemoteStatus == NoteRemoteStatus.Active
        ) return null

        val existing = decodePendingSnapshot(note.pendingSnapshotJson)
        // 兼容旧持久化 snapshot：仅补足冻结的 Content，不换 requestId。
        val snapshot = if (existing != null) {
            if (existing.content == null) existing.copy(
                content = NoteContentCodec.fromPlainText(existing.text)
            ) else existing
        } else {
            PersistedNoteSyncSnapshot(
                requestId = UUID.randomUUID().toString(),
                noteId = note.noteId, clientKey = note.clientKey,
                baseVersion = note.remoteVersion,
                dayKey = note.dayKey, onlyDay = note.onlyDay,
                title = note.title, text = note.text, pinned = note.pinned,
                content = NoteContentCodec.parse(note.contentJson)
                    ?: NoteContentCodec.fromPlainText(note.text),
            )
        }
        val syncing = note.copy(
            pendingSnapshotJson = json.encodeToString(snapshot),
            syncState = NoteSyncState.Syncing, lastSyncError = null,
        )
        if (syncing != note) dao.update(syncing)
        return PreparedNotePush(note = syncing, snapshot = snapshot)
    }

    suspend fun applyPushSuccess(
        ownerUserId: String,
        localKey: String,
        sentSnapshot: PersistedNoteSyncSnapshot,
        outcome: String,
        remote: RemoteNoteSnapshot,
    ) {
        val current = findByLocalKey(ownerUserId, localKey) ?: return
        if (decodePendingSnapshot(current.pendingSnapshotJson)?.requestId != sentSnapshot.requestId) return

        val applied = outcome != "identity_resolved"
        val unchanged = contentMatchesSnapshot(current, sentSnapshot)
        val sentContent = sentSnapshot.content ?: NoteContentCodec.fromPlainText(sentSnapshot.text)
        val sentJson = NoteContentCodec.stringify(sentContent)
        val sentText = NoteContentCodec.extractText(sentContent)
        val remoteJson = remoteContentJson(remote)
        val remoteText = remotePlainText(remote)
        val committedJson = remoteJson ?: sentJson
        val committedText = if (remote.content != null) remoteText else sentText
        val officialKey = remoteLocalKey(ownerUserId, remote.noteId)
        val clientKey = remote.clientKey?.takeIf(String::isNotBlank) ?: current.clientKey

        if (applied && unchanged) {
            val hasRemainingWork = current.pendingRemoteStatus != null ||
                    current.pinned != current.syncedPinned
            dao.update(current.copy(
                localKey = officialKey, noteId = remote.noteId, clientKey = clientKey,
                dayKey = remote.dayKey, onlyDay = remote.onlyDay, title = remote.title,
                text = committedText, contentJson = committedJson, remoteStatus = remote.remoteStatus,
                syncedDayKey = remote.dayKey, syncedOnlyDay = remote.onlyDay,
                syncedTitle = remote.title, syncedText = committedText,
                syncedContentJson = committedJson,
                remoteVersion = remote.version.toLong(), remoteUpdatedAt = remote.updatedAt.toLong(),
                pendingSnapshotJson = null,
                syncState = if (hasRemainingWork) NoteSyncState.Pending else NoteSyncState.Synced,
                lastSyncError = null,
            ))
            if (hasRemainingWork) requestSync(ownerUserId)
            return
        }

        // 请求期间有本地编辑，或服务端只完成 identity_resolved。
        // 本地 current 保留；baseline 按服务端实际确认结果更新。
        dao.update(current.copy(
            localKey = officialKey, noteId = remote.noteId, clientKey = clientKey,
            remoteStatus = remote.remoteStatus,
            syncedDayKey = remote.dayKey, syncedOnlyDay = remote.onlyDay,
            syncedTitle = remote.title,
            syncedText = if (applied) committedText else remoteText,
            syncedContentJson = if (applied) committedJson else remoteJson,
            remoteVersion = remote.version.toLong(), remoteUpdatedAt = remote.updatedAt.toLong(),
            pendingSnapshotJson = null, syncState = NoteSyncState.Pending,
            lastSyncError = null,
        ))
        requestSync(ownerUserId)
    }

    suspend fun applyPushFailure(
        ownerUserId: String, localKey: String,
        sentSnapshot: PersistedNoteSyncSnapshot, failure: NoteSyncFailure,
    ) {
        val current = findByLocalKey(ownerUserId, localKey) ?: return
        if (decodePendingSnapshot(current.pendingSnapshotJson)?.requestId != sentSnapshot.requestId) return
        // Frozen snapshot 保留，下一轮使用相同 requestId。
        dao.update(applyFailure(current, failure))
    }

    // MARK: - Lifecycle confirmation

    suspend fun applyLifecycleSuccess(
        ownerUserId: String, localKey: String,
        expectedStatus: NoteRemoteStatus, remote: RemoteNoteSnapshot,
    ) {
        val current = findByLocalKey(ownerUserId, localKey) ?: return
        if (current.pendingRemoteStatus != expectedStatus) return
        if (remote.remoteStatus != expectedStatus) {
            markConflictFromRemote(
                current, remote,
                "生命周期返回状态不符合预期：expected=${expectedStatus.rawValue}, " +
                        "actual=${remote.remoteStatus.rawValue}",
            )
            return
        }
        if (expectedStatus == NoteRemoteStatus.Deleted) {
            dao.delete(current)
            return
        }

        val localContentDirty = hasPendingContentWork(current)
        val pinnedDirty = current.pinned != current.syncedPinned
        val remoteJson = remoteContentJson(remote)
        val remoteText = remotePlainText(remote)

        // 归档时若并发产生本地正文修改，不能将其误判为已上传。
        if (expectedStatus == NoteRemoteStatus.Archived && localContentDirty) {
            markConflictFromRemote(current, remote, "记录已在云端归档，但本机仍存在未同步正文")
            return
        }

        val stillPending = localContentDirty || pinnedDirty
        val updated = current.copy(
            noteId = remote.noteId,
            clientKey = remote.clientKey?.takeIf(String::isNotBlank) ?: current.clientKey,
            remoteStatus = remote.remoteStatus, pendingRemoteStatus = null,
            dayKey = if (localContentDirty) current.dayKey else remote.dayKey,
            onlyDay = if (localContentDirty) current.onlyDay else remote.onlyDay,
            title = if (localContentDirty) current.title else remote.title,
            text = if (localContentDirty) current.text else remoteText,
            contentJson = if (localContentDirty) current.contentJson else remoteJson,
            syncedDayKey = remote.dayKey, syncedOnlyDay = remote.onlyDay,
            syncedTitle = remote.title, syncedText = remoteText,
            syncedContentJson = remoteJson,
            // pinned / syncedPinned 由独立的 setPinned 处理。
            remoteVersion = remote.version.toLong(), remoteUpdatedAt = remote.updatedAt.toLong(),
            pendingSnapshotJson = if (localContentDirty) current.pendingSnapshotJson else null,
            conflictSnapshotJson = null,
            syncState = if (stillPending) NoteSyncState.Pending else NoteSyncState.Synced,
            lastSyncError = null,
        )
        dao.update(updated)
        if (stillPending) requestSync(ownerUserId)
    }

    suspend fun applyLifecycleFailure(
        ownerUserId: String, localKey: String,
        expectedStatus: NoteRemoteStatus, failure: NoteSyncFailure,
    ) {
        val current = findByLocalKey(ownerUserId, localKey) ?: return
        if (current.pendingRemoteStatus != expectedStatus) return
        if (failure is NoteSyncFailure.RemoteDeleted && expectedStatus == NoteRemoteStatus.Deleted) {
            dao.delete(current)
            return
        }
        dao.update(applyFailure(current, failure))
    }

    // MARK: - Conflict

    suspend fun resolveConflictUsingRemote(note: LocalNoteEntity): LocalNoteEntity? {
        val conflict = decodeConflictSnapshot(note.conflictSnapshotJson) ?: return note
        if (conflict.parsedRemoteStatus == NoteRemoteStatus.Deleted) {
            dao.delete(note)
            return null
        }
        val contentJson = conflictContentJson(note, conflict)
        val text = conflictPlainText(note, conflict)
        val updated = note.copy(
            noteId = conflict.noteId,
            clientKey = conflict.clientKey?.takeIf(String::isNotBlank) ?: note.clientKey,
            dayKey = conflict.dayKey, onlyDay = conflict.onlyDay,
            title = conflict.title, text = text, contentJson = contentJson,
            pinned = conflict.pinned,
            remoteStatus = conflict.parsedRemoteStatus, pendingRemoteStatus = null,
            syncedDayKey = conflict.dayKey, syncedOnlyDay = conflict.onlyDay,
            syncedTitle = conflict.title, syncedText = text, syncedContentJson = contentJson,
            syncedPinned = conflict.pinned,
            remoteVersion = conflict.version, remoteUpdatedAt = conflict.updatedAt,
            pendingSnapshotJson = null, conflictSnapshotJson = null,
            localUpdatedAt = System.currentTimeMillis(),
            syncState = NoteSyncState.Synced, lastSyncError = null,
        )
        dao.update(updated)
        return updated
    }

    suspend fun resolveConflictKeepingLocal(note: LocalNoteEntity): LocalNoteEntity? {
        val conflict = decodeConflictSnapshot(note.conflictSnapshotJson) ?: return note
        return when (conflict.parsedRemoteStatus) {
            NoteRemoteStatus.Active -> resolveKeepLocalAgainstActiveRemote(note, conflict)
            NoteRemoteStatus.Archived -> resolveKeepLocalAgainstArchivedRemote(note, conflict)
            NoteRemoteStatus.Deleted -> resolveKeepLocalAgainstDeletedRemote(note)
        }
    }

    private suspend fun resolveKeepLocalAgainstActiveRemote(
        note: LocalNoteEntity, conflict: PersistedNoteConflictSnapshot,
    ): LocalNoteEntity {
        val updated = note.copy(
            noteId = conflict.noteId,
            clientKey = conflict.clientKey?.takeIf(String::isNotBlank) ?: note.clientKey,
            remoteStatus = NoteRemoteStatus.Active, pendingRemoteStatus = null,
            syncedDayKey = conflict.dayKey, syncedOnlyDay = conflict.onlyDay,
            syncedTitle = conflict.title, syncedText = conflictPlainText(note, conflict),
            syncedContentJson = conflictContentJson(note, conflict), syncedPinned = conflict.pinned,
            remoteVersion = conflict.version, remoteUpdatedAt = conflict.updatedAt,
            pendingSnapshotJson = null, conflictSnapshotJson = null,
            localUpdatedAt = System.currentTimeMillis(),
            syncState = NoteSyncState.Pending, lastSyncError = null,
        )
        dao.update(updated)
        updated.ownerUserId?.let(::requestSync)
        return updated
    }

    private suspend fun resolveKeepLocalAgainstArchivedRemote(
        note: LocalNoteEntity, conflict: PersistedNoteConflictSnapshot,
    ): LocalNoteEntity {
        val updated = note.copy(
            noteId = conflict.noteId,
            clientKey = conflict.clientKey?.takeIf(String::isNotBlank) ?: note.clientKey,
            remoteStatus = NoteRemoteStatus.Archived,
            pendingRemoteStatus = NoteRemoteStatus.Active,
            syncedDayKey = conflict.dayKey, syncedOnlyDay = conflict.onlyDay,
            syncedTitle = conflict.title, syncedText = conflictPlainText(note, conflict),
            syncedContentJson = conflictContentJson(note, conflict), syncedPinned = conflict.pinned,
            remoteVersion = conflict.version, remoteUpdatedAt = conflict.updatedAt,
            pendingSnapshotJson = null, conflictSnapshotJson = null,
            localUpdatedAt = System.currentTimeMillis(),
            syncState = NoteSyncState.Pending, lastSyncError = null,
        )
        dao.update(updated)
        updated.ownerUserId?.let(::requestSync)
        return updated
    }

    private suspend fun resolveKeepLocalAgainstDeletedRemote(note: LocalNoteEntity): LocalNoteEntity {
        val owner = requireNotNull(note.ownerUserId) { "ownerUserId is required" }
        val updated = note.copy(
            localKey = LocalNoteKeys.new(ownerUserId = owner), noteId = null,
            clientKey = UUID.randomUUID().toString(),
            remoteStatus = NoteRemoteStatus.Active, pendingRemoteStatus = null,
            syncedDayKey = null, syncedOnlyDay = false,
            syncedTitle = "", syncedText = "", syncedContentJson = null,
            syncedPinned = false, remoteVersion = null, remoteUpdatedAt = null,
            pendingSnapshotJson = null, conflictSnapshotJson = null,
            localUpdatedAt = System.currentTimeMillis(),
            syncState = NoteSyncState.Pending, lastSyncError = null,
        )
        dao.update(updated)
        requestSync(owner)
        return updated
    }

    suspend fun markConflictFromRemote(
        note: LocalNoteEntity, remote: RemoteNoteSnapshot, message: String,
    ): LocalNoteEntity {
        val version = remote.version.toLong()
        val updatedAt = remote.updatedAt.toLong()
        val existing = decodeConflictSnapshot(note.conflictSnapshotJson)
        if (existing != null && existing.version > version) return note

        // PersistedNoteConflictSnapshot 现有 DTO 仍只有 text；将 content 同存入 JSON，
        // decodeConflictSnapshot(ignoreUnknownKeys=true) 可兼容旧数据，冲突解决时再读取。
        val snapshot = PersistedNoteConflictSnapshot(
            noteId = remote.noteId, clientKey = remote.clientKey,
            dayKey = remote.dayKey, onlyDay = remote.onlyDay,
            title = remote.title, text = remotePlainText(remote), pinned = remote.pinned,
            remoteStatus = remote.remoteStatus.rawValue,
            version = version, updatedAt = updatedAt,
        )
        val base = json.encodeToJsonElement(snapshot) as JsonObject
        val payload = remote.content?.let { JsonObject(base + ("content" to it)) } ?: base
        val updated = note.copy(
            conflictSnapshotJson = NoteContentCodec.stringify(payload),
            remoteUpdatedAt = maxOf(note.remoteUpdatedAt ?: 0L, updatedAt),
            syncState = NoteSyncState.Conflict, lastSyncError = message,
        )
        dao.upsert(updated)
        return updated
    }

    fun decodeConflictSnapshot(value: String?): PersistedNoteConflictSnapshot? =
        value?.takeIf(String::isNotBlank)?.let { json.decodeFromString<PersistedNoteConflictSnapshot>(it) }

    // MARK: - Persistence / Helpers

    suspend fun updateForSync(note: LocalNoteEntity) = dao.update(note)
    suspend fun deleteLocal(note: LocalNoteEntity) = dao.delete(note)

    private fun decodePendingSnapshot(value: String?): PersistedNoteSyncSnapshot? =
        value?.takeIf(String::isNotBlank)?.let { json.decodeFromString<PersistedNoteSyncSnapshot>(it) }

    /** 新建 Note、冻结请求和正文未同步变化统一视为待处理 Content。 */
    private fun hasPendingContentWork(note: LocalNoteEntity): Boolean =
        note.noteId == null || note.hasPendingSnapshot || note.hasUnsyncedContentChanges

    /** 避免临时 fromPlainText() 生成不同 UUID，误判未修改。 */
    private fun contentMatchesSnapshot(
        note: LocalNoteEntity, snapshot: PersistedNoteSyncSnapshot,
    ): Boolean {
        if (note.dayKey != snapshot.dayKey || note.onlyDay != snapshot.onlyDay ||
            note.title != snapshot.title || note.text != snapshot.text
        ) return false
        val current = NoteContentCodec.parse(note.contentJson) ?: return true
        val sent = snapshot.content ?: return false
        return current == NoteContentCodec.parse(NoteContentCodec.stringify(sent))
    }

    private fun remotePlainText(remote: RemoteNoteSnapshot): String =
        remote.content?.let(NoteContentCodec::extractText) ?: remote.text

    private fun remoteContentJson(remote: RemoteNoteSnapshot): String? =
        remote.content?.let(NoteContentCodec::stringify)

    private fun conflictContentJson(note: LocalNoteEntity, conflict: PersistedNoteConflictSnapshot): String {
        val obj = NoteContentCodec.parse(note.conflictSnapshotJson) as? JsonObject
        val content = obj?.get("content")?.takeUnless { it == JsonNull }
        return content?.let(NoteContentCodec::stringify)
            ?: NoteContentCodec.stringify(NoteContentCodec.fromPlainText(conflict.text))
    }

    private fun conflictPlainText(note: LocalNoteEntity, conflict: PersistedNoteConflictSnapshot): String {
        val obj = NoteContentCodec.parse(note.conflictSnapshotJson) as? JsonObject
        val content = obj?.get("content")?.takeUnless { it == JsonNull }
        return content?.let(NoteContentCodec::extractText) ?: conflict.text
    }

    private fun applyFailure(note: LocalNoteEntity, failure: NoteSyncFailure): LocalNoteEntity =
        when (failure) {
            is NoteSyncFailure.Retryable -> note.copy(
                syncState = NoteSyncState.Waiting, lastSyncError = failure.message,
            )
            is NoteSyncFailure.Conflict -> note.copy(
                syncState = NoteSyncState.Conflict, lastSyncError = failure.message,
            )
            is NoteSyncFailure.RemoteDeleted -> note.copy(
                remoteStatus = NoteRemoteStatus.Deleted,
                syncState = NoteSyncState.Conflict, lastSyncError = failure.message,
            )
            is NoteSyncFailure.RemoteArchived -> note.copy(
                remoteStatus = NoteRemoteStatus.Archived,
                syncState = NoteSyncState.Conflict, lastSyncError = failure.message,
            )
        }

    private fun makeNewLocalNote(
        ownerUserId: String,
        dayKey: String?,
        onlyDay: Boolean,
        localKey: String = LocalNoteKeys.new(ownerUserId = ownerUserId),
    ): LocalNoteEntity = LocalNoteEntity(
        localKey = localKey, ownerUserId = ownerUserId, noteId = null,
        clientKey = UUID.randomUUID().toString(), dayKey = dayKey, onlyDay = onlyDay,
        title = "", text = "", pinned = false,
        remoteStatus = NoteRemoteStatus.Active, pendingRemoteStatus = null,
        syncedDayKey = null, syncedOnlyDay = false,
        syncedTitle = "", syncedText = "", syncedPinned = false,
        remoteVersion = null, remoteUpdatedAt = null,
        pendingSnapshotJson = null, conflictSnapshotJson = null,
        localUpdatedAt = System.currentTimeMillis(),
        syncState = NoteSyncState.Pending, lastSyncError = null,
    )

    private fun nextPendingState(note: LocalNoteEntity): NoteSyncState = when (note.syncState) {
        NoteSyncState.Syncing -> NoteSyncState.Syncing
        NoteSyncState.Conflict -> NoteSyncState.Conflict
        else -> NoteSyncState.Pending
    }

    private fun remoteLocalKey(ownerUserId: String, noteId: String): String =
        "user:$ownerUserId:note:$noteId"

    private fun normalizeDayKey(value: String): String {
        val normalized = value.trim()
        require(DayKey.isValid(normalized)) { "Invalid dayKey: $value" }
        return normalized
    }
}