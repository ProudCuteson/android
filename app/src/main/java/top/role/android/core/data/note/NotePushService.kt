package top.role.android.core.data.note

import top.role.android.core.database.note.hasUnsyncedChanges
import top.role.android.core.model.note.NoteRemoteStatus
import top.role.android.core.model.note.NoteSyncFailure
import top.role.android.core.database.note.LocalNoteEntity
import top.role.android.core.database.note.hasUnsyncedContentChanges

class NotePushService(
    private val noteLocalRepository:
    NoteLocalRepository,

    private val noteRemoteDataSource:
    NoteRemoteDataSource,
) {

    suspend fun pushOne(
        ownerUserId: String,
        localKey: String,
    ): Result<Unit> {
        return runCatching {

            val note =
                noteLocalRepository
                    .findByLocalKey(
                        ownerUserId =
                            ownerUserId,

                        localKey =
                            localKey,
                    )
                    ?: return@runCatching

            when (
                note.pendingRemoteStatus
            ) {

                NoteRemoteStatus.Deleted -> {
                    pushDelete(
                        ownerUserId =
                            ownerUserId,

                        localKey =
                            localKey,

                        noteId =
                            note.noteId,

                        baseVersion =
                            note.remoteVersion,
                    )
                }

                NoteRemoteStatus.Archived -> {
                    when {
                        hasPendingContentWork(note) -> {
                            pushContent(ownerUserId, localKey)
                        }

                        note.pinned != note.syncedPinned -> {
                            pushPinned(
                                ownerUserId = ownerUserId,
                                localKey = localKey,
                                noteId = requireNotNull(note.noteId),
                                pinned = note.pinned,
                            )
                        }

                        else -> {
                            pushArchive(
                                ownerUserId = ownerUserId,
                                localKey = localKey,
                                noteId = note.noteId,
                                baseVersion = note.remoteVersion,
                            )
                        }
                    }
                }

                NoteRemoteStatus.Active -> {
                    pushRestore(
                        ownerUserId =
                            ownerUserId,

                        localKey =
                            localKey,

                        noteId =
                            note.noteId,

                        baseVersion =
                            note.remoteVersion,
                    )
                }

                /*
                 * ------------------------------------------------
                 * 普通 content sync
                 * ------------------------------------------------
                 */
                null -> {
                    when {
                        hasPendingContentWork(note) -> {
                            pushContent(ownerUserId, localKey)
                        }

                        note.pinned != note.syncedPinned -> {
                            pushPinned(
                                ownerUserId = ownerUserId,
                                localKey = localKey,
                                noteId = requireNotNull(note.noteId),
                                pinned = note.pinned,
                            )
                        }
                    }
                }
            }
        }
    }

    private fun hasPendingContentWork(note: LocalNoteEntity): Boolean {
        return note.noteId == null ||
                note.pendingSnapshotJson != null ||
                note.hasUnsyncedContentChanges
    }

    private suspend fun pushPinned(
        ownerUserId: String,
        localKey: String,
        noteId: String,
        pinned: Boolean,
    ) {
        noteRemoteDataSource.setPinned(
            noteId = noteId,
            pinned = pinned,
        ).getOrElse { error ->
            noteLocalRepository.applyPinnedFailure(
                ownerUserId = ownerUserId,
                localKey = localKey,
                noteId = noteId,
                sentPinned = pinned,
                failure = classifyFailure(error),
            )
            throw error
        }

        noteLocalRepository.applyPinnedSuccess(
            ownerUserId = ownerUserId,
            localKey = localKey,
            noteId = noteId,
            sentPinned = pinned,
        )
    }

    // MARK: - Content

    private suspend fun pushContent(
        ownerUserId: String,
        localKey: String,
    ) {
        val prepared =
            noteLocalRepository
                .preparePendingSnapshot(
                    ownerUserId =
                        ownerUserId,

                    localKey =
                        localKey,
                )
                ?: return

        val sentSnapshot =
            prepared.snapshot

        val response =
            noteRemoteDataSource
                .upsertNote(
                    snapshot =
                        sentSnapshot,
                )
                .getOrElse { error ->
                    noteLocalRepository
                        .applyPushFailure(
                            ownerUserId =
                                ownerUserId,

                            localKey =
                                localKey,

                            sentSnapshot =
                                sentSnapshot,

                            failure =
                                classifyFailure(
                                    error,
                                ),
                        )

                    throw error
                }

        noteLocalRepository
            .applyPushSuccess(
                ownerUserId =
                    ownerUserId,

                localKey =
                    localKey,

                sentSnapshot =
                    sentSnapshot,

                outcome =
                    response.outcome,

                remote =
                    response.note,
            )
    }

    // MARK: - Archive

    private suspend fun pushArchive(
        ownerUserId: String,
        localKey: String,
        noteId: String?,
        baseVersion: Long?,
    ) {
        val remoteNoteId =
            requireRemoteNoteId(
                noteId =
                    noteId,

                operation =
                    "archive",
            )

        val response =
            noteRemoteDataSource
                .archiveNote(
                    noteId =
                        remoteNoteId,

                    baseVersion =
                        baseVersion,
                )
                .getOrElse { error ->
                    noteLocalRepository
                        .applyLifecycleFailure(
                            ownerUserId =
                                ownerUserId,

                            localKey =
                                localKey,

                            expectedStatus =
                                NoteRemoteStatus.Archived,

                            failure =
                                classifyFailure(
                                    error,
                                ),
                        )

                    throw error
                }

        noteLocalRepository
            .applyLifecycleSuccess(
                ownerUserId =
                    ownerUserId,

                localKey =
                    localKey,

                expectedStatus =
                    NoteRemoteStatus.Archived,

                remote =
                    response.note,
            )
    }

    // MARK: - Restore

    private suspend fun pushRestore(
        ownerUserId: String,
        localKey: String,
        noteId: String?,
        baseVersion: Long?,
    ) {
        val remoteNoteId =
            requireRemoteNoteId(
                noteId =
                    noteId,

                operation =
                    "restore",
            )

        val response =
            noteRemoteDataSource
                .restoreNote(
                    noteId =
                        remoteNoteId,

                    baseVersion =
                        baseVersion,
                )
                .getOrElse { error ->
                    noteLocalRepository
                        .applyLifecycleFailure(
                            ownerUserId =
                                ownerUserId,

                            localKey =
                                localKey,

                            expectedStatus =
                                NoteRemoteStatus.Active,

                            failure =
                                classifyFailure(
                                    error,
                                ),
                        )

                    throw error
                }

        noteLocalRepository
            .applyLifecycleSuccess(
                ownerUserId =
                    ownerUserId,

                localKey =
                    localKey,

                expectedStatus =
                    NoteRemoteStatus.Active,

                remote =
                    response.note,
            )
    }

    // MARK: - Delete

    private suspend fun pushDelete(
        ownerUserId: String,
        localKey: String,
        noteId: String?,
        baseVersion: Long?,
    ) {
        /*
         * markForDelete() 对纯本地 Note
         * 已经直接进行了 deleteLocal。
         *
         * 因此能够进入这里的 Note
         * 理论上必须已经存在远端 identity。
         */
        val remoteNoteId =
            requireRemoteNoteId(
                noteId =
                    noteId,

                operation =
                    "delete",
            )

        val response =
            noteRemoteDataSource
                .softDeleteNote(
                    noteId =
                        remoteNoteId,

                    baseVersion =
                        baseVersion,
                )
                .getOrElse { error ->
                    noteLocalRepository
                        .applyLifecycleFailure(
                            ownerUserId =
                                ownerUserId,

                            localKey =
                                localKey,

                            expectedStatus =
                                NoteRemoteStatus.Deleted,

                            failure =
                                classifyFailure(
                                    error,
                                ),
                        )

                    throw error
                }

        noteLocalRepository
            .applyLifecycleSuccess(
                ownerUserId =
                    ownerUserId,

                localKey =
                    localKey,

                expectedStatus =
                    NoteRemoteStatus.Deleted,

                remote =
                    response.note,
            )
    }

    // MARK: - Helpers

    private fun requireRemoteNoteId(
        noteId: String?,
        operation: String,
    ): String {
        return noteId
            ?.takeIf {
                it.isNotBlank()
            }
            ?: throw IllegalStateException(
                "Remote noteId is required for $operation"
            )
    }

    private fun classifyFailure(
        error: Throwable,
    ): NoteSyncFailure {
        val message =
            error.message
                ?: "Note sync failed"

        return when {
            /*
             * 明确的乐观并发冲突。
             */
            message.contains(
                "NOTE_VERSION_CONFLICT",
                ignoreCase = true,
            ) ->
                NoteSyncFailure.Conflict(
                    message =
                        message,
                )

            /*
             * 本应具有 remote identity，
             * 但服务端找不到。
             *
             * 这种情况继续自动 Retry
             * 一般也不会恢复，
             * 所以先进入 Conflict，
             * 后续由 conflict resolver 处理。
             */
            message.contains(
                "NOTE_NOT_FOUND",
                ignoreCase = true,
            ) ->
                NoteSyncFailure.Conflict(
                    message =
                        message,
                )

            /*
             * 已有远端 Note 的 mutation
             * 缺少 baseVersion，
             * 属于本地同步状态异常，
             * 重试本身无法解决。
             */
            message.contains(
                "NOTE_BASE_VERSION_REQUIRED",
                ignoreCase = true,
            ) ->
                NoteSyncFailure.Conflict(
                    message =
                        message,
                )

            message.contains(
                "NOTE_DELETED",
                ignoreCase = true,
            ) ->
                NoteSyncFailure.RemoteDeleted(
                    message =
                        message,
                )

            message.contains(
                "NOTE_ARCHIVED",
                ignoreCase = true,
            ) ->
                NoteSyncFailure.RemoteArchived(
                    message =
                        message,
                )

            /*
             * 网络错误、临时服务错误等
             * 第一版统一视为可重试。
             */
            else ->
                NoteSyncFailure.Retryable(
                    message =
                        message,
                )
        }
    }
}