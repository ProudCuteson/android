package top.role.android.core.data.image.note

import java.util.UUID
import kotlinx.serialization.json.Json
import top.role.android.core.database.dao.LocalNoteMediaSyncDao
import top.role.android.core.database.entity.LocalNoteMediaSyncEntity
import top.role.android.core.model.note.NoteMediaSyncState
import top.role.android.core.model.note.PersistedNoteMediaItem
import top.role.android.core.model.note.PersistedNoteMediaSyncSnapshot
import top.role.android.core.model.note.PersistedNoteMediaConflictSnapshot

class NoteMediaLocalRepository(
    private val dao:
    LocalNoteMediaSyncDao,
) {

    private val json =
        Json {
            ignoreUnknownKeys = true
        }

    // MARK: - Read

    suspend fun findForNote(
        ownerUserId: String,
        noteLocalKey: String,
    ): LocalNoteMediaSyncEntity? {
        requireOwnerUserId(
            ownerUserId,
        )

        requireNoteLocalKey(
            noteLocalKey,
        )

        return dao.findForNote(
            ownerUserId =
                ownerUserId,

            noteLocalKey =
                noteLocalKey,
        )
    }

    suspend fun findByNoteId(
        ownerUserId: String,
        noteId: String,
    ): LocalNoteMediaSyncEntity? {
        requireOwnerUserId(
            ownerUserId,
        )

        require(
            noteId.isNotBlank(),
        ) {
            "NOTE_ID_REQUIRED"
        }

        return dao.findByNoteId(
            ownerUserId =
                ownerUserId,

            noteId =
                noteId,
        )
    }

    suspend fun findNeedingSync(
        ownerUserId: String,
        limit: Int = 50,
    ): List<LocalNoteMediaSyncEntity> {
        requireOwnerUserId(
            ownerUserId,
        )

        require(
            limit > 0,
        ) {
            "MEDIA_SYNC_LIMIT_INVALID"
        }

        return dao.findNeedingSync(
            ownerUserId =
                ownerUserId,

            limit =
                limit,
        )
    }

    // MARK: - Ensure

    suspend fun ensureState(
        ownerUserId: String,
        noteLocalKey: String,
        noteId: String? = null,
    ): LocalNoteMediaSyncEntity {
        requireOwnerUserId(
            ownerUserId,
        )

        requireNoteLocalKey(
            noteLocalKey,
        )

        val existing =
            dao.findForNote(
                ownerUserId =
                    ownerUserId,

                noteLocalKey =
                    noteLocalKey,
            )

        if (
            existing != null
        ) {
            /*
             * Note 后来获得正式 noteId，
             * 但 localKey 尚未变化的场景。
             */
            if (
                existing.noteId.isNullOrBlank() &&
                !noteId.isNullOrBlank()
            ) {
                val updated =
                    existing.copy(
                        noteId =
                            noteId,

                        localUpdatedAt =
                            System.currentTimeMillis(),
                    )

                dao.upsert(
                    entity =
                        updated,
                )

                return updated
            }

            return existing
        }

        val now =
            System.currentTimeMillis()

        val created =
            LocalNoteMediaSyncEntity(
                syncKey =
                    makeSyncKey(
                        ownerUserId =
                            ownerUserId,

                        noteLocalKey =
                            noteLocalKey,
                    ),

                ownerUserId =
                    ownerUserId,

                noteLocalKey =
                    noteLocalKey,

                noteId =
                    noteId
                        ?.takeIf {
                            it.isNotBlank()
                        },

                remoteVersion =
                    null,

                remoteUpdatedAt =
                    null,

                syncStateRaw =
                    NoteMediaSyncState
                        .Synced
                        .rawValue,

                pendingSnapshotJson =
                    null,

                conflictSnapshotJson =
                    null,

                lastSyncError =
                    null,

                localUpdatedAt =
                    now,
            )

        dao.upsert(
            entity =
                created,
        )

        return created
    }

    // MARK: - Pending

    suspend fun markPending(
        ownerUserId: String,
        noteLocalKey: String,
        noteId: String? = null,
    ): LocalNoteMediaSyncEntity {
        val state =
            ensureState(
                ownerUserId =
                    ownerUserId,

                noteLocalKey =
                    noteLocalKey,

                noteId =
                    noteId,
            )

        val nextState =
            when (
                state.syncState
            ) {
                NoteMediaSyncState.Syncing ->
                    NoteMediaSyncState.Syncing

                NoteMediaSyncState.Conflict ->
                    NoteMediaSyncState.Conflict

                else ->
                    NoteMediaSyncState.Pending
            }

        val updated =
            state.copy(
                noteId =
                    noteId
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?: state.noteId,

                syncStateRaw =
                    nextState.rawValue,

                lastSyncError =
                    null,

                localUpdatedAt =
                    System.currentTimeMillis(),
            )

        dao.upsert(
            entity =
                updated,
        )

        return updated
    }

    // MARK: - Prepare push

    suspend fun preparePendingSnapshot(
        ownerUserId: String,
        noteLocalKey: String,
        noteId: String,
        currentMedia: List<PersistedNoteMediaItem>,
    ): PreparedNoteMediaPush? {
        requireOwnerUserId(
            ownerUserId,
        )

        requireNoteLocalKey(
            noteLocalKey,
        )

        require(
            noteId.isNotBlank(),
        ) {
            "NOTE_ID_REQUIRED"
        }

        val state =
            ensureState(
                ownerUserId =
                    ownerUserId,

                noteLocalKey =
                    noteLocalKey,

                noteId =
                    noteId,
            )

        if (
            state.syncState ==
            NoteMediaSyncState.Synced
        ) {
            return null
        }

        if (
            state.syncState ==
            NoteMediaSyncState.Conflict
        ) {
            return null
        }

        val existingSnapshot =
            decodePendingSnapshot(
                state.pendingSnapshotJson,
            )

        if (
            existingSnapshot != null
        ) {
            val syncing =
                state.copy(
                    noteId =
                        noteId,

                    syncStateRaw =
                        NoteMediaSyncState
                            .Syncing
                            .rawValue,

                    lastSyncError =
                        null,
                )

            dao.upsert(
                entity =
                    syncing,
            )

            return PreparedNoteMediaPush(
                state =
                    syncing,

                snapshot =
                    existingSnapshot,
            )
        }

        val normalizedMedia =
            normalizeMedia(
                currentMedia,
            )

        val snapshot =
            PersistedNoteMediaSyncSnapshot(
                requestId =
                    UUID
                        .randomUUID()
                        .toString(),

                noteId =
                    noteId,

                baseVersion =
                    state.remoteVersion,

                media =
                    normalizedMedia,
            )

        val encoded =
            json.encodeToString(
                snapshot,
            )

        val syncing =
            state.copy(
                noteId =
                    noteId,

                syncStateRaw =
                    NoteMediaSyncState
                        .Syncing
                        .rawValue,

                pendingSnapshotJson =
                    encoded,

                lastSyncError =
                    null,
            )

        dao.upsert(
            entity =
                syncing,
        )

        return PreparedNoteMediaPush(
            state =
                syncing,

            snapshot =
                snapshot,
        )
    }

    // MARK: - Push success

    suspend fun applyPushSuccess(
        ownerUserId: String,
        noteLocalKey: String,
        sentSnapshot: PersistedNoteMediaSyncSnapshot,
        remoteVersion: Long,
        remoteUpdatedAt: Long,
        currentMedia: List<PersistedNoteMediaItem>,
    ) {
        val current =
            dao.findForNote(
                ownerUserId =
                    ownerUserId,

                noteLocalKey =
                    noteLocalKey,
            )
                ?: return

        val currentPending =
            decodePendingSnapshot(
                current.pendingSnapshotJson,
            )

        /*
         * 晚到的旧 response。
         */
        if (
            currentPending?.requestId !=
            sentSnapshot.requestId
        ) {
            return
        }

        val normalizedCurrent =
            normalizeMedia(
                currentMedia,
            )

        val normalizedSent =
            normalizeMedia(
                sentSnapshot.media,
            )

        val unchangedSinceSend =
            normalizedCurrent ==
                    normalizedSent

        val updated =
            if (
                unchangedSinceSend
            ) {
                current.copy(
                    remoteVersion =
                        remoteVersion,

                    remoteUpdatedAt =
                        remoteUpdatedAt,

                    syncStateRaw =
                        NoteMediaSyncState
                            .Synced
                            .rawValue,

                    pendingSnapshotJson =
                        null,

                    conflictSnapshotJson =
                        null,

                    lastSyncError =
                        null,
                )
            } else {
                current.copy(
                    remoteVersion =
                        remoteVersion,

                    remoteUpdatedAt =
                        remoteUpdatedAt,

                    syncStateRaw =
                        NoteMediaSyncState
                            .Pending
                            .rawValue,

                    pendingSnapshotJson =
                        null,

                    conflictSnapshotJson =
                        null,

                    lastSyncError =
                        null,

                    localUpdatedAt =
                        System.currentTimeMillis(),
                )
            }

        dao.upsert(
            entity =
                updated,
        )
    }

    // MARK: - Push failure

    suspend fun applyRetryableFailure(
        ownerUserId: String,
        noteLocalKey: String,
        sentSnapshot: PersistedNoteMediaSyncSnapshot,
        message: String?,
    ) {
        val current =
            dao.findForNote(
                ownerUserId =
                    ownerUserId,

                noteLocalKey =
                    noteLocalKey,
            )
                ?: return

        val currentPending =
            decodePendingSnapshot(
                current.pendingSnapshotJson,
            )

        if (
            currentPending?.requestId !=
            sentSnapshot.requestId
        ) {
            return
        }

        dao.upsert(
            entity =
                current.copy(
                    syncStateRaw =
                        NoteMediaSyncState
                            .Waiting
                            .rawValue,

                    lastSyncError =
                        normalizeError(
                            message,
                        ),
                ),
        )
    }

    // MARK: - Conflict

    suspend fun markConflict(
        ownerUserId: String,
        noteLocalKey: String,
        remote: PersistedNoteMediaConflictSnapshot,
        message: String?,
    ) {
        val current =
            dao.findForNote(
                ownerUserId =
                    ownerUserId,

                noteLocalKey =
                    noteLocalKey,
            )
                ?: return

        val encoded =
            json.encodeToString(
                remote,
            )

        val updated =
            current.copy(
                remoteUpdatedAt =
                    maxOf(
                        current.remoteUpdatedAt
                            ?: 0L,

                        remote.updatedAt,
                    ),

                syncStateRaw =
                    NoteMediaSyncState
                        .Conflict
                        .rawValue,

                conflictSnapshotJson =
                    encoded,

                lastSyncError =
                    normalizeError(
                        message,
                    ),
            )

        dao.upsert(
            entity =
                updated,
        )
    }

    suspend fun markConflictWithoutSnapshot(
        ownerUserId: String,
        noteLocalKey: String,
        message: String?,
    ) {
        val current =
            dao.findForNote(
                ownerUserId =
                    ownerUserId,

                noteLocalKey =
                    noteLocalKey,
            )
                ?: return

        dao.upsert(
            entity =
                current.copy(
                    syncStateRaw =
                        NoteMediaSyncState
                            .Conflict
                            .rawValue,

                    /*
                     * 不伪造 remote snapshot。
                     */
                    conflictSnapshotJson =
                        null,

                    lastSyncError =
                        normalizeError(
                            message,
                        ),
                ),
        )
    }

    suspend fun applyAuthoritativeRemote(
        ownerUserId: String,
        noteLocalKey: String,
        noteId: String,
        remoteVersion: Long,
        remoteUpdatedAt: Long,
    ) {
        requireOwnerUserId(
            ownerUserId,
        )

        requireNoteLocalKey(
            noteLocalKey,
        )

        require(
            noteId.isNotBlank(),
        ) {
            "NOTE_ID_REQUIRED"
        }

        val current =
            ensureState(
                ownerUserId =
                    ownerUserId,

                noteLocalKey =
                    noteLocalKey,

                noteId =
                    noteId,
            )

        dao.upsert(
            entity =
                current.copy(
                    noteId =
                        noteId,

                    remoteVersion =
                        remoteVersion,

                    remoteUpdatedAt =
                        remoteUpdatedAt,

                    syncStateRaw =
                        NoteMediaSyncState
                            .Synced
                            .rawValue,

                    pendingSnapshotJson =
                        null,

                    conflictSnapshotJson =
                        null,

                    lastSyncError =
                        null,

                    localUpdatedAt =
                        current.localUpdatedAt,
                ),
        )
    }

    suspend fun adoptRemoteBaselinePreservingLocal(
        ownerUserId: String,
        noteLocalKey: String,
        noteId: String,
        remoteVersion: Long,
        remoteUpdatedAt: Long,
    ) {
        requireOwnerUserId(
            ownerUserId,
        )

        requireNoteLocalKey(
            noteLocalKey,
        )

        require(
            noteId.isNotBlank(),
        ) {
            "NOTE_ID_REQUIRED"
        }

        val current =
            findForNote(
                ownerUserId =
                    ownerUserId,

                noteLocalKey =
                    noteLocalKey,
            )
                ?: return

        /*
         * 只允许给 Pending state 建立首次 remote baseline。
         */
        if (
            current.syncState !=
            NoteMediaSyncState.Pending
        ) {
            return
        }

        if (
            current.remoteVersion !=
            null
        ) {
            return
        }

        dao.upsert(
            entity =
                current.copy(
                    noteId =
                        noteId,

                    remoteVersion =
                        remoteVersion,

                    remoteUpdatedAt =
                        remoteUpdatedAt,

                    syncStateRaw =
                        NoteMediaSyncState
                            .Pending
                            .rawValue,

                    lastSyncError =
                        null,
                ),
        )
    }

    // MARK: - Identity

    /**
     * Note temporary identity
     * → official identity。
     *
     * Media state 同样必须收敛 localKey。
     */
    suspend fun rebindNote(
        ownerUserId: String,
        oldNoteLocalKey: String,
        newNoteLocalKey: String,
        noteId: String?,
    ) {
        requireOwnerUserId(
            ownerUserId,
        )

        requireNoteLocalKey(
            oldNoteLocalKey,
        )

        requireNoteLocalKey(
            newNoteLocalKey,
        )

        if (
            oldNoteLocalKey ==
            newNoteLocalKey
        ) {
            /*
             * key 没变但 noteId 可能刚刚建立。
             */
            val existing =
                dao.findForNote(
                    ownerUserId =
                        ownerUserId,

                    noteLocalKey =
                        newNoteLocalKey,
                )
                    ?: return

            if (
                existing.noteId.isNullOrBlank() &&
                !noteId.isNullOrBlank()
            ) {
                dao.upsert(
                    entity =
                        existing.copy(
                            noteId =
                                noteId,

                            localUpdatedAt =
                                System.currentTimeMillis(),
                        ),
                )
            }

            return
        }

        val existing =
            dao.findForNote(
                ownerUserId =
                    ownerUserId,

                noteLocalKey =
                    oldNoteLocalKey,
            )
                ?: return

        val updated =
            existing.copy(
                syncKey =
                    makeSyncKey(
                        ownerUserId =
                            ownerUserId,

                        noteLocalKey =
                            newNoteLocalKey,
                    ),

                noteLocalKey =
                    newNoteLocalKey,

                noteId =
                    noteId
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?: existing.noteId,

                localUpdatedAt =
                    System.currentTimeMillis(),
            )

        /*
         * 因为 PrimaryKey syncKey 发生变化，
         * 单纯 Upsert 会留下旧 row。
         *
         * 所以 DAO 的 rebindNote() 更合适。
         */
        dao.rebindNote(
            ownerUserId =
                ownerUserId,

            oldNoteLocalKey =
                oldNoteLocalKey,

            newNoteLocalKey =
                newNoteLocalKey,

            newSyncKey =
                updated.syncKey,

            noteId =
                updated.noteId,

            updatedAt =
                updated.localUpdatedAt,
        )
    }

    // MARK: - Recovery

    suspend fun resetInterruptedSync(
        ownerUserId: String,
    ) {
        requireOwnerUserId(
            ownerUserId,
        )

        dao.resetInterruptedSync(
            ownerUserId =
                ownerUserId,

            updatedAt =
                System.currentTimeMillis(),
        )
    }

    // MARK: - Cleanup

    suspend fun deleteForNote(
        ownerUserId: String,
        noteLocalKey: String,
    ) {
        dao.deleteForNote(
            ownerUserId =
                ownerUserId,

            noteLocalKey =
                noteLocalKey,
        )
    }

    // MARK: - Snapshot

    fun decodePendingSnapshot(
        value: String?,
    ): PersistedNoteMediaSyncSnapshot? {
        if (
            value.isNullOrBlank()
        ) {
            return null
        }

        return json.decodeFromString<
                PersistedNoteMediaSyncSnapshot
                >(
            value,
        )
    }

    fun decodeConflictSnapshot(
        value: String?,
    ): PersistedNoteMediaConflictSnapshot? {
        if (
            value.isNullOrBlank()
        ) {
            return null
        }

        return json.decodeFromString<
                PersistedNoteMediaConflictSnapshot
                >(
            value,
        )
    }

    // MARK: - Helpers

    private fun normalizeMedia(
        media:
        List<PersistedNoteMediaItem>,
    ): List<PersistedNoteMediaItem> {
        return media
            .sortedBy {
                it.order
            }
            .mapIndexed {
                    index,
                    item ->

                item.copy(
                    order =
                        index,
                )
            }
    }

    private fun makeSyncKey(
        ownerUserId: String,
        noteLocalKey: String,
    ): String {
        return "user:$ownerUserId:note-media:$noteLocalKey"
    }

    private fun requireOwnerUserId(
        ownerUserId: String,
    ) {
        require(
            ownerUserId.isNotBlank(),
        ) {
            "OWNER_USER_ID_REQUIRED"
        }
    }

    private fun requireNoteLocalKey(
        noteLocalKey: String,
    ) {
        require(
            noteLocalKey.isNotBlank(),
        ) {
            "NOTE_LOCAL_KEY_REQUIRED"
        }
    }

    private fun normalizeError(
        value: String?,
    ): String? {
        return value
            ?.takeIf {
                it.isNotBlank()
            }
            ?.take(
                500,
            )
    }
}

data class PreparedNoteMediaPush(
    val state:
    LocalNoteMediaSyncEntity,

    val snapshot:
    PersistedNoteMediaSyncSnapshot,
)