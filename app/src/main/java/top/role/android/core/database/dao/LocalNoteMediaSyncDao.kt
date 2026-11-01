package top.role.android.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import top.role.android.core.database.entity.LocalNoteMediaSyncEntity

@Dao
interface LocalNoteMediaSyncDao {

    @Query(
        """
        SELECT *
        FROM local_note_media_sync
        WHERE localOwnerUserId = :localOwnerUserId
          AND noteLocalKey = :noteLocalKey
        LIMIT 1
        """
    )
    suspend fun findForNote(
        localOwnerUserId: String,
        noteLocalKey: String,
    ): LocalNoteMediaSyncEntity?

    @Query(
        """
        SELECT *
        FROM local_note_media_sync
        WHERE localOwnerUserId = :localOwnerUserId
          AND noteId = :noteId
        LIMIT 1
        """
    )
    suspend fun findByNoteId(
        localOwnerUserId: String,
        noteId: String,
    ): LocalNoteMediaSyncEntity?

    @Query(
        """
        SELECT *
        FROM local_note_media_sync
        WHERE localOwnerUserId = :localOwnerUserId
          AND syncStateRaw IN ('pending', 'waiting')
        ORDER BY localUpdatedAt ASC
        LIMIT :limit
        """
    )
    suspend fun findNeedingSync(
        localOwnerUserId: String,
        limit: Int,
    ): List<LocalNoteMediaSyncEntity>

    @Upsert
    suspend fun upsert(
        entity: LocalNoteMediaSyncEntity,
    )

    @Query(
        """
        DELETE FROM local_note_media_sync
        WHERE localOwnerUserId = :localOwnerUserId
          AND noteLocalKey = :noteLocalKey
        """
    )
    suspend fun deleteForNote(
        localOwnerUserId: String,
        noteLocalKey: String,
    )

    @Query(
        """
        UPDATE local_note_media_sync
        SET
            syncKey = :newSyncKey,
            noteLocalKey = :newNoteLocalKey,
            noteId = :noteId,
            localUpdatedAt = :updatedAt
        WHERE localOwnerUserId = :localOwnerUserId
          AND noteLocalKey = :oldNoteLocalKey
        """
    )
    suspend fun rebindNote(
        localOwnerUserId: String,
        oldNoteLocalKey: String,
        newNoteLocalKey: String,
        newSyncKey: String,
        noteId: String?,
        updatedAt: Long,
    )

    /*
     * App 如果在 Syncing 状态被杀，
     * 下次启动恢复成 Waiting。
     */
    @Query(
        """
        UPDATE local_note_media_sync
        SET
            syncStateRaw = 'waiting',
            localUpdatedAt = :updatedAt
        WHERE localOwnerUserId = :localOwnerUserId
          AND syncStateRaw = 'syncing'
        """
    )
    suspend fun resetInterruptedSync(
        localOwnerUserId: String,
        updatedAt: Long,
    )

    // 认领 -- Start

    @Query(
        """
    UPDATE local_note_media_sync
    SET syncKey = :newSyncKey,
        localOwnerUserId = :accountUserId,
        noteLocalKey = :newNoteLocalKey,
        localUpdatedAt = :updatedAt
    WHERE localOwnerUserId = :guestLocalOwnerId
      AND noteLocalKey = :oldNoteLocalKey
    """
    )
    suspend fun transferNoteMediaSync(
        guestLocalOwnerId: String,
        accountUserId: String,
        oldNoteLocalKey: String,
        newNoteLocalKey: String,
        newSyncKey: String,
        updatedAt: Long,
    ): Int

    // 认领 -- End

}