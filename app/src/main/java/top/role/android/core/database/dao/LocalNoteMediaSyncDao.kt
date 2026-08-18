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
        WHERE ownerUserId = :ownerUserId
          AND noteLocalKey = :noteLocalKey
        LIMIT 1
        """
    )
    suspend fun findForNote(
        ownerUserId: String,
        noteLocalKey: String,
    ): LocalNoteMediaSyncEntity?

    @Query(
        """
        SELECT *
        FROM local_note_media_sync
        WHERE ownerUserId = :ownerUserId
          AND noteId = :noteId
        LIMIT 1
        """
    )
    suspend fun findByNoteId(
        ownerUserId: String,
        noteId: String,
    ): LocalNoteMediaSyncEntity?

    @Query(
        """
        SELECT *
        FROM local_note_media_sync
        WHERE ownerUserId = :ownerUserId
          AND syncStateRaw IN ('pending', 'waiting')
        ORDER BY localUpdatedAt ASC
        LIMIT :limit
        """
    )
    suspend fun findNeedingSync(
        ownerUserId: String,
        limit: Int,
    ): List<LocalNoteMediaSyncEntity>

    @Upsert
    suspend fun upsert(
        entity: LocalNoteMediaSyncEntity,
    )

    @Query(
        """
        DELETE FROM local_note_media_sync
        WHERE ownerUserId = :ownerUserId
          AND noteLocalKey = :noteLocalKey
        """
    )
    suspend fun deleteForNote(
        ownerUserId: String,
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
        WHERE ownerUserId = :ownerUserId
          AND noteLocalKey = :oldNoteLocalKey
        """
    )
    suspend fun rebindNote(
        ownerUserId: String,
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
        WHERE ownerUserId = :ownerUserId
          AND syncStateRaw = 'syncing'
        """
    )
    suspend fun resetInterruptedSync(
        ownerUserId: String,
        updatedAt: Long,
    )
}