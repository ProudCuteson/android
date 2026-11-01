package top.role.android.core.data.note

import kotlinx.coroutines.CancellationException
import top.role.android.core.model.note.NoteRemoteStatus
import top.role.android.core.model.note.NoteSyncFailure
import top.role.android.core.database.note.LocalNoteEntity
import top.role.android.core.database.note.hasUnsyncedContentChanges

class NotePushService(
    private val noteLocalRepository: NoteLocalRepository,
    private val noteRemoteDataSource: NoteRemoteDataSource,
) {

    suspend fun pushOne(
        localOwnerUserId: String,
        localKey: String,
    ): Result<Unit> {
        return try {
            val note = noteLocalRepository
                .findByLocalKey(
                    localOwnerUserId,
                    localKey = localKey,
                )
                ?: return Result.success(Unit)

            when (note.pendingRemoteStatus) {
                NoteRemoteStatus.Deleted -> {
                    pushDelete(
                        localOwnerUserId,
                        localKey,
                        note.noteId,
                    )
                }

                NoteRemoteStatus.Archived -> {
                    when {
                        hasPendingContentWork(note) -> {
                            pushContent(localOwnerUserId, localKey)
                        }

                        note.pinned != note.syncedPinned -> {
                            pushPinned(
                                localOwnerUserId,
                                localKey,
                                requireNotNull(note.noteId),
                                note.pinned,
                            )
                        }

                        else -> {
                            pushArchive(
                                localOwnerUserId,
                                localKey,
                                note.noteId,
                                note.remoteVersion,
                            )
                        }
                    }
                }

                NoteRemoteStatus.Active -> {
                    pushRestore(
                        localOwnerUserId,
                        localKey,
                        note.noteId,
                    )
                }

                null -> {
                    when {
                        hasPendingContentWork(note) -> {
                            pushContent(localOwnerUserId, localKey)
                        }

                        note.pinned != note.syncedPinned -> {
                            pushPinned(
                                localOwnerUserId,
                                localKey,
                                requireNotNull(note.noteId),
                                note.pinned,
                            )
                        }
                    }
                }
            }

            Result.success(Unit)

        } catch (error: CancellationException) {
            throw error

        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    private fun hasPendingContentWork(note: LocalNoteEntity): Boolean {
        return note.noteId == null ||
                note.pendingSnapshotJson != null ||
                note.hasUnsyncedContentChanges
    }

    private suspend fun pushPinned(
        localOwnerUserId: String,
        localKey: String,
        noteId: String,
        pinned: Boolean,
    ) {
        noteRemoteDataSource.setPinned(
            noteId = noteId,
            pinned = pinned,
        ).getOrElse { error ->
            noteLocalRepository.applyPinnedFailure(
                localOwnerUserId = localOwnerUserId,
                localKey = localKey,
                noteId = noteId,
                sentPinned = pinned,
                failure = classifyFailure(error),
            )
            throw error
        }

        noteLocalRepository.applyPinnedSuccess(
            localOwnerUserId = localOwnerUserId,
            localKey = localKey,
            noteId = noteId,
            sentPinned = pinned,
        )
    }

    // MARK: - Content

    private suspend fun pushContent(
        localOwnerUserId: String,
        localKey: String,
    ) {
        val prepared =
            noteLocalRepository
                .preparePendingSnapshot(
                    localOwnerUserId = localOwnerUserId,
                    localKey = localKey,
                )
                ?: return

        val sentSnapshot = prepared.snapshot

        val response = noteRemoteDataSource
            .upsertNote(snapshot = sentSnapshot)
            .getOrElse { error ->
                noteLocalRepository
                    .applyPushFailure(
                        localOwnerUserId = localOwnerUserId,
                        localKey = localKey,
                        sentSnapshot = sentSnapshot,
                        failure = classifyFailure(error),
                    )
                throw error
            }

        noteLocalRepository
            .applyPushSuccess(
                localOwnerUserId = localOwnerUserId,
                localKey = localKey,
                sentSnapshot = sentSnapshot,
                outcome = response.outcome,
                remote = response.note,
            )
    }

    // MARK: - Archive

    private suspend fun pushArchive(
        localOwnerUserId: String,
        localKey: String,
        noteId: String?,
        baseVersion: Long?,
    ) {
        val remoteNoteId = requireRemoteNoteId(
                noteId = noteId,
                operation = "archive",
            )

        val response = noteRemoteDataSource
                .archiveNote(
                    noteId = remoteNoteId,
                )
                .getOrElse { error ->
                    noteLocalRepository
                        .applyLifecycleFailure(
                            localOwnerUserId = localOwnerUserId,
                            localKey = localKey,
                            expectedStatus = NoteRemoteStatus.Archived,
                            failure = classifyFailure(error),
                        )

                    throw error
                }

        noteLocalRepository
            .applyLifecycleSuccess(
                localOwnerUserId = localOwnerUserId,
                localKey = localKey,
                expectedStatus = NoteRemoteStatus.Archived,
                remote = response.note,
            )
    }

    // MARK: - Restore

    private suspend fun pushRestore(
        localOwnerUserId: String,
        localKey: String,
        noteId: String?,
    ) {
        val remoteNoteId = requireRemoteNoteId(
            noteId = noteId,
            operation = "restore",
        )

        val response = noteRemoteDataSource
                .restoreNote(
                    noteId = remoteNoteId,
                )
                .getOrElse { error ->
                    noteLocalRepository
                        .applyLifecycleFailure(
                            localOwnerUserId = localOwnerUserId,
                            localKey = localKey,
                            expectedStatus = NoteRemoteStatus.Active,
                            failure = classifyFailure(error),
                        )

                    throw error
                }

        noteLocalRepository
            .applyLifecycleSuccess(
                localOwnerUserId = localOwnerUserId,
                localKey = localKey,
                expectedStatus = NoteRemoteStatus.Active,
                remote = response.note,
            )
    }

    // MARK: - Delete

    private suspend fun pushDelete(
        localOwnerUserId: String,
        localKey: String,
        noteId: String?,
    ) {

        val remoteNoteId = requireRemoteNoteId(
            noteId = noteId,
            operation = "delete",
        )

        val response = noteRemoteDataSource
            .softDeleteNote(noteId = remoteNoteId)
            .getOrElse { error ->
                noteLocalRepository
                    .applyLifecycleFailure(
                        localOwnerUserId = localOwnerUserId,
                        localKey = localKey,
                        expectedStatus = NoteRemoteStatus.Deleted,
                        failure = classifyFailure(error),
                    )

                throw error
            }

        noteLocalRepository
            .applyLifecycleSuccess(
                localOwnerUserId = localOwnerUserId,
                localKey = localKey,
                expectedStatus = NoteRemoteStatus.Deleted,
                remote = response.note,
            )
    }

    // MARK: - Helpers

    private fun requireRemoteNoteId(
        noteId: String?,
        operation: String,
    ): String {
        return noteId
            ?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException( "Remote noteId is required for $operation" )
    }

    private fun classifyFailure(
        error: Throwable,
    ): NoteSyncFailure {
        val message = error.message ?: "Note sync failed"

        return when {
            message.contains(
                "NOTE_VERSION_CONFLICT",
                ignoreCase = true,
            ) ->
                NoteSyncFailure.Conflict(message = message)

            message.contains(
                "NOTE_NOT_FOUND",
                ignoreCase = true,
            ) ->
                NoteSyncFailure.Conflict(message = message)

            message.contains( "NOTE_DELETED", ignoreCase = true ) ->
                NoteSyncFailure.RemoteDeleted(message = message)

            message.contains(
                "NOTE_ARCHIVED",
                ignoreCase = true,
            ) ->
                NoteSyncFailure.RemoteArchived( message = message )

            else ->
                NoteSyncFailure.Retryable( message = message )
        }
    }
}