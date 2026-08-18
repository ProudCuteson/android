package top.role.android.core.data.image.note

import top.role.android.core.data.image.NoteImageLocalRepository
import top.role.android.core.model.note.NoteMediaSyncFailure
import top.role.android.core.model.note.NoteMediaSyncState
import top.role.android.core.model.note.PersistedNoteMediaConflictSnapshot
import top.role.android.core.model.note.PersistedNoteMediaItem
import top.role.android.core.model.note.PersistedNoteMediaSyncSnapshot
import top.role.android.core.database.entity.LocalNoteImageEntity

data class NoteMediaPushBatchResult(
    val attemptedCount: Int,
    val succeededCount: Int,
    val waitingCount: Int,
    val conflictCount: Int,
    val skippedCount: Int,
    val failedCount: Int,
)

sealed interface NoteMediaPushResult {
    data object NoWork :
        NoteMediaPushResult
    data object WaitingForNoteIdentity :
        NoteMediaPushResult
    data object WaitingForImageAssets :
        NoteMediaPushResult
    data class Success(
        val noteId: String,
        val version: Long,
        val mediaCount: Int,
    ) : NoteMediaPushResult
    data class Waiting(
        val message: String?,
    ) : NoteMediaPushResult
    data class Conflict(
        val message: String?,
    ) : NoteMediaPushResult
}


class NoteMediaPushService(
    private val imageLocalRepository:
    NoteImageLocalRepository,

    private val mediaLocalRepository:
    NoteMediaLocalRepository,

    private val mediaRemoteDataSource:
    NoteMediaRemoteDataSource,
) {

    suspend fun pushOne(
        ownerUserId: String,
        noteLocalKey: String,
    ): Result<NoteMediaPushResult> {
        return runCatching {
            require(
                ownerUserId.isNotBlank(),
            ) {
                "OWNER_USER_ID_REQUIRED"
            }

            require(
                noteLocalKey.isNotBlank(),
            ) {
                "NOTE_LOCAL_KEY_REQUIRED"
            }

            val state =
                mediaLocalRepository
                    .findForNote(
                        ownerUserId =
                            ownerUserId,

                        noteLocalKey =
                            noteLocalKey,
                    )
                    ?: return@runCatching NoteMediaPushResult.NoWork

            if (
                state.syncState !=
                NoteMediaSyncState.Pending &&
                state.syncState !=
                NoteMediaSyncState.Waiting
            ) {
                return@runCatching NoteMediaPushResult.NoWork
            }

            val noteId =
                state.noteId
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?: return@runCatching NoteMediaPushResult.WaitingForNoteIdentity

            val localImages =
                imageLocalRepository
                    .listForNote(
                        ownerUserId =
                            ownerUserId,

                        noteLocalKey =
                            noteLocalKey,
                    )

            val hasUnresolvedImageAssets =
                localImages.any { image ->
                    image.uploadState !=
                            "uploaded" ||
                            image.imageId
                                .isNullOrBlank()
                }

            if (
                hasUnresolvedImageAssets
            ) {
                return@runCatching NoteMediaPushResult.WaitingForImageAssets
            }

            val currentMedia =
                buildCurrentMedia(
                    localImages =
                        localImages,
                )

            val prepared =
                mediaLocalRepository
                    .preparePendingSnapshot(
                        ownerUserId =
                            ownerUserId,

                        noteLocalKey =
                            noteLocalKey,

                        noteId =
                            noteId,

                        currentMedia =
                            currentMedia,
                    )
                    ?: return@runCatching NoteMediaPushResult.NoWork

            pushPrepared(
                ownerUserId =
                    ownerUserId,

                noteLocalKey =
                    noteLocalKey,

                prepared =
                    prepared,
            )
        }
    }

    // MARK: - Push

    private suspend fun pushPrepared(
        ownerUserId: String,
        noteLocalKey: String,
        prepared: PreparedNoteMediaPush,
    ): NoteMediaPushResult {
        val snapshot =
            prepared.snapshot

        return try {
            val response =
                mediaRemoteDataSource
                    .replaceNoteMedia(
                        noteId =
                            snapshot.noteId,

                        requestId =
                            snapshot.requestId,

                        baseVersion =
                            snapshot.baseVersion,

                        media =
                            snapshot.media,
                    )

            val localImages =
                imageLocalRepository
                    .listForNote(
                        ownerUserId =
                            ownerUserId,

                        noteLocalKey =
                            noteLocalKey,
                    )

            val currentMedia =
                buildComparableCurrentMedia(
                    localImages =
                        localImages,
                )

            mediaLocalRepository
                .applyPushSuccess(
                    ownerUserId =
                        ownerUserId,

                    noteLocalKey =
                        noteLocalKey,

                    sentSnapshot =
                        snapshot,

                    remoteVersion =
                        response.version
                            .toLong(),

                    remoteUpdatedAt =
                        response.updatedAt
                            .toLong(),

                    currentMedia =
                        currentMedia,
                )

            NoteMediaPushResult.Success(
                noteId =
                    response.noteId,

                version =
                    response.version
                        .toLong(),

                mediaCount =
                    response.media.size,
            )

        } catch (
            error: Throwable,
        ) {
            handlePushFailure(
                ownerUserId =
                    ownerUserId,

                noteLocalKey =
                    noteLocalKey,

                sentSnapshot =
                    snapshot,

                error =
                    error,
            )
        }
    }

    suspend fun pushPending(
        ownerUserId: String,
        limit: Int = 50,
    ): NoteMediaPushBatchResult {
        require(
            ownerUserId.isNotBlank(),
        ) {
            "OWNER_USER_ID_REQUIRED"
        }

        require(
            limit > 0,
        ) {
            "MEDIA_PUSH_LIMIT_INVALID"
        }

        mediaLocalRepository
            .resetInterruptedSync(
                ownerUserId =
                    ownerUserId,
            )

        val states =
            mediaLocalRepository
                .findNeedingSync(
                    ownerUserId =
                        ownerUserId,

                    limit =
                        limit,
                )

        var succeededCount = 0
        var waitingCount = 0
        var conflictCount = 0
        var skippedCount = 0
        var failedCount = 0

        for (
        state in states
        ) {
            val result =
                pushOne(
                    ownerUserId =
                        ownerUserId,

                    noteLocalKey =
                        state.noteLocalKey,
                )

            result.fold(
                onSuccess = { outcome ->
                    when (
                        outcome
                    ) {
                        is NoteMediaPushResult.Success -> {
                            succeededCount +=
                                1
                        }

                        is NoteMediaPushResult.Waiting -> {
                            waitingCount +=
                                1
                        }

                        is NoteMediaPushResult.Conflict -> {
                            conflictCount +=
                                1
                        }

                        NoteMediaPushResult.NoWork,
                        NoteMediaPushResult.WaitingForNoteIdentity,
                        NoteMediaPushResult.WaitingForImageAssets -> {
                            skippedCount +=
                                1
                        }
                    }
                },

                onFailure = {
                    failedCount += 1
                },
            )
        }

        return NoteMediaPushBatchResult(
            attemptedCount = states.size,

            succeededCount = succeededCount,

            waitingCount = waitingCount,

            conflictCount = conflictCount,

            skippedCount = skippedCount,

            failedCount = failedCount,
        )
    }

    // MARK: - Failure

    private suspend fun handlePushFailure(
        ownerUserId: String,
        noteLocalKey: String,
        sentSnapshot:
        PersistedNoteMediaSyncSnapshot,
        error: Throwable,
    ): NoteMediaPushResult {
        val failure =
            classifyFailure(
                error,
            )

        return when (
            failure
        ) {

            is NoteMediaSyncFailure.Retryable -> {
                mediaLocalRepository
                    .applyRetryableFailure(
                        ownerUserId =
                            ownerUserId,

                        noteLocalKey =
                            noteLocalKey,

                        sentSnapshot =
                            sentSnapshot,

                        message =
                            failure.message,
                    )

                NoteMediaPushResult.Waiting(
                    message =
                        failure.message,
                )
            }

            is NoteMediaSyncFailure.Conflict -> {
                hydrateVersionConflict(
                    ownerUserId =
                        ownerUserId,

                    noteLocalKey =
                        noteLocalKey,

                    noteId =
                        sentSnapshot.noteId,

                    message =
                        failure.message,
                )

                NoteMediaPushResult.Conflict(
                    message =
                        failure.message,
                )
            }

            is NoteMediaSyncFailure.RemoteDeleted,
            is NoteMediaSyncFailure.RemoteNotFound -> {
                mediaLocalRepository
                    .markConflictWithoutSnapshot(
                        ownerUserId =
                            ownerUserId,

                        noteLocalKey =
                            noteLocalKey,

                        message =
                            failure.message,
                    )

                NoteMediaPushResult.Conflict(
                    message =
                        failure.message,
                )
            }
        }
    }


    private fun classifyFailure(
        error: Throwable,
    ): NoteMediaSyncFailure {
        val message =
            error.message
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: "Note media sync failed"

        return when {

            message.contains(
                "MEDIA_VERSION_CONFLICT",
                ignoreCase =
                    true,
            ) ->
                NoteMediaSyncFailure.Conflict(
                    message =
                        message,
                )

            message.contains(
                "NOTE_NOT_FOUND",
                ignoreCase =
                    true,
            ) ->
                NoteMediaSyncFailure.RemoteNotFound(
                    message =
                        message,
                )

            message.contains(
                "NOTE_DELETED",
                ignoreCase =
                    true,
            ) ->
                NoteMediaSyncFailure.RemoteDeleted(
                    message =
                        message,
                )

            else ->
                NoteMediaSyncFailure.Retryable(
                    message =
                        message,
                )
        }
    }


    // MARK: - Conflict

    private suspend fun hydrateVersionConflict(
        ownerUserId: String,
        noteLocalKey: String,
        noteId: String,
        message: String?,
    ) {
        val remote =
            runCatching {
                mediaRemoteDataSource
                    .getNoteMedia(
                        noteId =
                            noteId,
                    )
            }
                .getOrNull()

        if (
            remote == null
        ) {
            mediaLocalRepository
                .markConflictWithoutSnapshot(
                    ownerUserId =
                        ownerUserId,

                    noteLocalKey =
                        noteLocalKey,

                    message =
                        message,
                )

            return
        }

        val remoteSnapshot =
            PersistedNoteMediaConflictSnapshot(
                noteId =
                    remote.noteId,

                version =
                    remote.version
                        .toLong(),

                updatedAt =
                    remote.updatedAt
                        .toLong(),

                media =
                    remote.media
                        .asSequence()
                        .filter {
                            it.type ==
                                    "image"
                        }
                        .sortedBy {
                            it.order
                        }
                        .mapIndexed {
                                index,
                                item ->

                            PersistedNoteMediaItem(
                                imageId =
                                    item.imageId,

                                order =
                                    index,
                            )
                        }
                        .toList(),
            )

        mediaLocalRepository
            .markConflict(
                ownerUserId =
                    ownerUserId,

                noteLocalKey =
                    noteLocalKey,

                remote =
                    remoteSnapshot,

                message =
                    message,
            )
    }


    // MARK: - Media Build

    private fun buildCurrentMedia(
        localImages: List<LocalNoteImageEntity>,
    ): List<PersistedNoteMediaItem> {
        return localImages
            .asSequence()
            .sortedBy {
                it.position
            }
            .mapIndexed {
                    index,
                    image ->

                val imageId =
                    requireNotNull(
                        image.imageId
                            ?.takeIf {
                                it.isNotBlank()
                            },
                    ) {
                        "Uploaded note image is missing imageId"
                    }

                PersistedNoteMediaItem(
                    imageId =
                        imageId,

                    order =
                        index,
                )
            }
            .toList()
    }

    private fun buildComparableCurrentMedia(
        localImages: List<LocalNoteImageEntity>,
    ): List<PersistedNoteMediaItem> {
        return localImages
            .asSequence()
            .sortedBy {
                it.position
            }
            .mapIndexed {
                    index,
                    image ->

                val comparisonId =
                    image.imageId
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?: "local:${image.localImageKey}"

                PersistedNoteMediaItem(
                    imageId =
                        comparisonId,

                    order =
                        index,
                )
            }
            .toList()
    }
}