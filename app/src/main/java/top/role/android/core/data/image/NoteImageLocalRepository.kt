package top.role.android.core.data.image

import kotlinx.coroutines.flow.Flow
import top.role.android.core.database.dao.LocalNoteImageDao
import top.role.android.core.database.entity.LocalNoteImageEntity
import top.role.android.core.model.note.PersistedNoteMediaItem
import top.role.android.core.database.entity.NoteImageCount

data class NoteImageRemoteReconcileResult(
    val removedLocalPaths: List<String>,
)

class NoteImageLocalRepository(
    private val dao: LocalNoteImageDao,
) {

    fun observeForNote(
        localOwnerUserId: String,
        noteLocalKey: String,
    ): Flow<List<LocalNoteImageEntity>> {
        return dao.observeForNote(
            localOwnerUserId =
                localOwnerUserId,

            noteLocalKey =
                noteLocalKey,
        )
    }

    fun observeMonthImageCounts(
        localOwnerUserId: String,
        startDayKey: String,
        endDayKeyExclusive: String,
    ): Flow<List<NoteImageCount>> {
        return dao.observeMonthImageCounts(
            localOwnerUserId = localOwnerUserId,
            startDayKey = startDayKey,
            endDayKeyExclusive = endDayKeyExclusive,
        )
    }

    suspend fun listForNote(
        localOwnerUserId: String,
        noteLocalKey: String,
    ): List<LocalNoteImageEntity> {
        return dao.listForNote(
            localOwnerUserId = localOwnerUserId,
            noteLocalKey = noteLocalKey,
        )
    }

    suspend fun get(
        localImageKey: String,
    ): LocalNoteImageEntity? {
        return dao.getByLocalImageKey(
            localImageKey = localImageKey,
        )
    }

    suspend fun upsert(
        image: LocalNoteImageEntity,
    ) {
        dao.upsert( image = image )
    }

    suspend fun nextPosition(
        localOwnerUserId: String,
        noteLocalKey: String,
    ): Int {
        return dao.nextPosition(
            localOwnerUserId = localOwnerUserId,
            noteLocalKey = noteLocalKey,
        )
    }

    suspend fun listPendingUploads(
        localOwnerUserId: String,
        limit: Int = 20,
    ): List<LocalNoteImageEntity> {
        return dao.listPendingUploads(
            localOwnerUserId = localOwnerUserId,
            limit = limit,
        )
    }

    suspend fun markUploading(
        localImageKey: String,
    ) {
        dao.markUploading(
            localImageKey = localImageKey,
            updatedAt = System.currentTimeMillis(),
        )
    }

    suspend fun markUploaded(
        localImageKey: String,
        imageId: String,
    ) {
        dao.markUploaded(
            localImageKey = localImageKey,
            imageId = imageId,
            updatedAt = System.currentTimeMillis(),
        )
    }

    suspend fun markUploadFailed(
        localImageKey: String,
        errorMessage: String?,
    ) {
        dao.markUploadFailed(
            localImageKey = localImageKey,
            errorMessage = errorMessage,
            updatedAt = System.currentTimeMillis(),
        )
    }

    suspend fun resetInterruptedUploads(
        localOwnerUserId: String,
    ) {
        dao.resetInterruptedUploads(
            localOwnerUserId = localOwnerUserId,
            updatedAt = System.currentTimeMillis(),
        )
    }

    suspend fun rebindNoteLocalKey(
        localOwnerUserId: String,
        oldNoteLocalKey: String,
        newNoteLocalKey: String,
    ) {
        if (
            oldNoteLocalKey ==
            newNoteLocalKey
        ) {
            return
        }

        dao.rebindNoteLocalKey(
            localOwnerUserId = localOwnerUserId,
            oldNoteLocalKey = oldNoteLocalKey,
            newNoteLocalKey = newNoteLocalKey,
            updatedAt = System.currentTimeMillis(),
        )
    }

    suspend fun updatePosition(
        localImageKey: String,
        position: Int,
    ) {
        dao.updatePosition(
            localImageKey = localImageKey,
            position = position,
            updatedAt = System.currentTimeMillis(),
        )
    }

    suspend fun delete(
        localImageKey: String,
    ) {
        dao.deleteByLocalImageKey(
            localImageKey = localImageKey,
        )
    }

    suspend fun deleteForNote(
        localOwnerUserId: String,
        noteLocalKey: String,
    ) {
        dao.deleteForNote(
            localOwnerUserId = localOwnerUserId,
            noteLocalKey = noteLocalKey,
        )
    }

    // MARK: - Remote Reconcile

    suspend fun reconcileAuthoritativeRemoteMedia(
        localOwnerUserId: String,
        noteLocalKey: String,
        remoteMedia: List<PersistedNoteMediaItem>,
    ): NoteImageRemoteReconcileResult {
        require(
            localOwnerUserId.isNotBlank(),
        ) {
            "OWNER_USER_ID_REQUIRED"
        }

        require(
            noteLocalKey.isNotBlank(),
        ) {
            "NOTE_LOCAL_KEY_REQUIRED"
        }

        val now = System.currentTimeMillis()

        val normalizedRemote = remoteMedia
            .sortedBy { it.order }
            .mapIndexed { index, item ->
                PersistedNoteMediaItem(
                    imageId = item.imageId,
                    order = index,
                )
            }

        require(remoteMedia.all { it.imageId.isNotBlank() }) {
            "INVALID_REMOTE_NOTE_MEDIA_IMAGE_ID"
        }

        val remoteImageIds = normalizedRemote
            .map { it.imageId }

        require(
            remoteImageIds
                .distinct()
                .size == remoteImageIds.size,
        ) {
            "DUPLICATE_REMOTE_NOTE_MEDIA"
        }

        val current = dao.listForNote(
            localOwnerUserId = localOwnerUserId,
            noteLocalKey = noteLocalKey,
        )

        val currentByImageId = current
            .mapNotNull { image ->
                val imageId = image.imageId
                    ?.takeIf { it.isNotBlank() }
                    ?: return@mapNotNull null
                imageId to image
            }
            .toMap()

        /*
         * Remote 当前 authoritative media
         * 转成本地 rows。
         */
        val imagesToUpsert = normalizedRemote.map { remoteItem ->
            currentByImageId[remoteItem.imageId]?.copy(
                uploadState = "uploaded",
                uploadError = null,
                position = remoteItem.order,
                updatedAt = now,
            ) ?: LocalNoteImageEntity(
                localImageKey = remoteLocalImageKey(
                    localOwnerUserId = localOwnerUserId,
                    noteLocalKey = noteLocalKey,
                    imageId = remoteItem.imageId,
                ),
                localOwnerUserId = localOwnerUserId,
                noteLocalKey = noteLocalKey,
                imageId = remoteItem.imageId,
                localPath = null,
                mime = null,
                width = null,
                height = null,
                bytes = null,
                uploadState = "uploaded",
                uploadError = null,
                position = remoteItem.order,
                createdAt = now,
                updatedAt = now,
            )
        }

        val authoritativeRemoteIds = remoteImageIds.toSet()

        val removed = current.filter { image ->
            val imageId = image.imageId
                ?.takeIf { it.isNotBlank() }
                ?: return@filter false

            imageId !in authoritativeRemoteIds
        }

        val localImageKeysToDelete = removed.map { it.localImageKey }

        val removedLocalPaths = removed
            .mapNotNull { image ->
                image.localPath
                    ?.takeIf { it.isNotBlank() }
            }
            .distinct()

        dao.applyRemoteReconcile(
            localImageKeysToDelete = localImageKeysToDelete,
            imagesToUpsert = imagesToUpsert,
        )

        return NoteImageRemoteReconcileResult(
            removedLocalPaths = removedLocalPaths,
        )
    }

    private fun remoteLocalImageKey(
        localOwnerUserId: String,
        noteLocalKey: String,
        imageId: String,
    ): String {
        return buildString {
            append("remote:")
            append(localOwnerUserId)
            append(":")
            append(noteLocalKey)
            append(":")
            append(imageId)
        }
    }
}