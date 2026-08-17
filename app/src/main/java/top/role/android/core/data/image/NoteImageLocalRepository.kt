package top.role.android.core.data.image

import kotlinx.coroutines.flow.Flow
import top.role.android.core.database.dao.LocalNoteImageDao
import top.role.android.core.database.entity.LocalNoteImageEntity

class NoteImageLocalRepository(
    private val dao: LocalNoteImageDao,
) {

    fun observeForNote(
        ownerUserId: String,
        noteLocalKey: String,
    ): Flow<List<LocalNoteImageEntity>> {
        return dao.observeForNote(
            ownerUserId = ownerUserId,
            noteLocalKey = noteLocalKey,
        )
    }

    suspend fun listForNote(
        ownerUserId: String,
        noteLocalKey: String,
    ): List<LocalNoteImageEntity> {
        return dao.listForNote(
            ownerUserId = ownerUserId,
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
        dao.upsert(
            image = image,
        )
    }

    suspend fun nextPosition(
        ownerUserId: String,
        noteLocalKey: String,
    ): Int {
        return dao.nextPosition(
            ownerUserId = ownerUserId,
            noteLocalKey = noteLocalKey,
        )
    }

    suspend fun listPendingUploads(
        ownerUserId: String,
        limit: Int = 20,
    ): List<LocalNoteImageEntity> {
        return dao.listPendingUploads(
            ownerUserId =
                ownerUserId,

            limit =
                limit,
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
        ownerUserId: String,
    ) {
        dao.resetInterruptedUploads(
            ownerUserId =
                ownerUserId,

            updatedAt =
                System.currentTimeMillis(),
        )
    }

    suspend fun rebindNoteLocalKey(
        ownerUserId: String,
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
            ownerUserId =
                ownerUserId,

            oldNoteLocalKey =
                oldNoteLocalKey,

            newNoteLocalKey =
                newNoteLocalKey,

            updatedAt =
                System.currentTimeMillis(),
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
        ownerUserId: String,
        noteLocalKey: String,
    ) {
        dao.deleteForNote(
            ownerUserId = ownerUserId,
            noteLocalKey = noteLocalKey,
        )
    }
}