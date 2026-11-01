package top.role.android.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import top.role.android.core.database.entity.LocalNoteImageEntity
import top.role.android.core.database.entity.NoteImageCount

@Dao
interface LocalNoteImageDao {

    // MARK: - Observe / Query

    /*
     * 实时观察某一条 Note 的全部图片。
     */
    @Query(
        """
        SELECT *
        FROM local_note_images
        WHERE localOwnerUserId = :localOwnerUserId
          AND noteLocalKey = :noteLocalKey
        ORDER BY position ASC, createdAt ASC
        """
    )
    fun observeForNote(
        localOwnerUserId: String,
        noteLocalKey: String,
    ): Flow<List<LocalNoteImageEntity>>

    /*
     * 一次性读取某一条 Note 的全部图片。
     */
    @Query(
        """
        SELECT *
        FROM local_note_images
        WHERE localOwnerUserId = :localOwnerUserId
          AND noteLocalKey = :noteLocalKey
        ORDER BY position ASC, createdAt ASC
        """
    )
    suspend fun listForNote(
        localOwnerUserId: String,
        noteLocalKey: String,
    ): List<LocalNoteImageEntity>

    /*
     * 根据本地 attachment identity 查询。
     */
    @Query(
        """
        SELECT *
        FROM local_note_images
        WHERE localImageKey = :localImageKey
        LIMIT 1
        """
    )
    suspend fun getByLocalImageKey(
        localImageKey: String,
    ): LocalNoteImageEntity?

    /*
     * 根据 remote imageId 查询。
     */
    @Query(
        """
        SELECT *
        FROM local_note_images
        WHERE localOwnerUserId = :localOwnerUserId
          AND imageId = :imageId
        LIMIT 1
        """
    )
    suspend fun findByImageId(
        localOwnerUserId: String,
        imageId: String,
    ): LocalNoteImageEntity?

    /*
     * 在指定 Note 中根据 remote imageId 查询。
     */
    @Query(
        """
        SELECT *
        FROM local_note_images
        WHERE localOwnerUserId = :localOwnerUserId
          AND noteLocalKey = :noteLocalKey
          AND imageId = :imageId
        LIMIT 1
        """
    )
    suspend fun findForNoteByImageId(
        localOwnerUserId: String,
        noteLocalKey: String,
        imageId: String,
    ): LocalNoteImageEntity?

    /*
     * 获取待上传图片。
     */
    @Query(
        """
        SELECT *
        FROM local_note_images
        WHERE localOwnerUserId = :localOwnerUserId
          AND uploadState IN ('local', 'failed')
        ORDER BY createdAt ASC
        LIMIT :limit
        """
    )
    suspend fun listPendingUploads(
        localOwnerUserId: String,
        limit: Int,
    ): List<LocalNoteImageEntity>

    /*
     * 获取下一张图片的位置。
     */
    @Query(
        """
        SELECT COALESCE(MAX(position) + 1, 0)
        FROM local_note_images
        WHERE localOwnerUserId = :localOwnerUserId
          AND noteLocalKey = :noteLocalKey
        """
    )
    suspend fun nextPosition(
        localOwnerUserId: String,
        noteLocalKey: String,
    ): Int

    // MARK: - Persistence

    /*
     * 新建或覆盖本地图片记录。
     */
    @Insert(
        onConflict = OnConflictStrategy.REPLACE,
    )
    suspend fun upsert(
        image: LocalNoteImageEntity,
    )

    /*
     * 批量 hydration / reconcile。
     */
    @Insert(
        onConflict = OnConflictStrategy.REPLACE,
    )
    suspend fun upsertAll(
        images: List<LocalNoteImageEntity>,
    )

    /*
     * 普通完整更新。
     */
    @Update
    suspend fun update(
        image: LocalNoteImageEntity,
    )

    // MARK: - Upload State

    /*
     * 标记开始上传。
     */
    @Query(
        """
        UPDATE local_note_images
        SET uploadState = 'uploading',
            uploadError = NULL,
            updatedAt = :updatedAt
        WHERE localImageKey = :localImageKey
        """
    )
    suspend fun markUploading(
        localImageKey: String,
        updatedAt: Long,
    )

    /*
     * OSS 上传 + Convex record 成功。
     */
    @Query(
        """
        UPDATE local_note_images
        SET imageId = :imageId,
            uploadState = 'uploaded',
            uploadError = NULL,
            updatedAt = :updatedAt
        WHERE localImageKey = :localImageKey
        """
    )
    suspend fun markUploaded(
        localImageKey: String,
        imageId: String,
        updatedAt: Long,
    )

    /*
     * 上传失败。
     */
    @Query(
        """
        UPDATE local_note_images
        SET uploadState = 'failed',
            uploadError = :errorMessage,
            updatedAt = :updatedAt
        WHERE localImageKey = :localImageKey
        """
    )
    suspend fun markUploadFailed(
        localImageKey: String,
        errorMessage: String?,
        updatedAt: Long,
    )

    /*
     * App 在上传过程中被终止时，
     * 将 uploading 恢复为 local。
     */
    @Query(
        """
        UPDATE local_note_images
        SET uploadState = 'local',
            uploadError = NULL,
            updatedAt = :updatedAt
        WHERE localOwnerUserId = :localOwnerUserId
          AND uploadState = 'uploading'
        """
    )
    suspend fun resetInterruptedUploads(
        localOwnerUserId: String,
        updatedAt: Long,
    )

    // MARK: - Note Identity

    /*
     * Note remote identity 收敛时，
     * attachment 跟随 localKey 迁移。
     */
    @Query(
        """
        UPDATE local_note_images
        SET noteLocalKey = :newNoteLocalKey,
            updatedAt = :updatedAt
        WHERE localOwnerUserId = :localOwnerUserId
          AND noteLocalKey = :oldNoteLocalKey
        """
    )
    suspend fun rebindNoteLocalKey(
        localOwnerUserId: String,
        oldNoteLocalKey: String,
        newNoteLocalKey: String,
        updatedAt: Long,
    )

    // MARK: - Position

    /*
     * 修改图片展示位置。
     */
    @Query(
        """
        UPDATE local_note_images
        SET position = :position,
            updatedAt = :updatedAt
        WHERE localImageKey = :localImageKey
        """
    )
    suspend fun updatePosition(
        localImageKey: String,
        position: Int,
        updatedAt: Long,
    )

    // MARK: - Delete

    /*
     * 删除单张本地 relation。
     */
    @Query(
        """
        DELETE FROM local_note_images
        WHERE localImageKey = :localImageKey
        """
    )
    suspend fun deleteByLocalImageKey(
        localImageKey: String,
    )

    @Query(
        """
        DELETE FROM local_note_images
        WHERE localImageKey IN (:localImageKeys)
        """
    )
    suspend fun deleteByLocalImageKeys(
        localImageKeys: List<String>,
    )

    /*
     * Remote reconcile 时，
     * 删除某一 remote image relation。
     */
    @Query(
        """
        DELETE FROM local_note_images
        WHERE localOwnerUserId = :localOwnerUserId
          AND noteLocalKey = :noteLocalKey
          AND imageId = :imageId
        """
    )
    suspend fun deleteForNoteByImageId(
        localOwnerUserId: String,
        noteLocalKey: String,
        imageId: String,
    )

    /*
     * 删除某条 Note 的全部本地 media relation。
     */
    @Query(
        """
        DELETE FROM local_note_images
        WHERE localOwnerUserId = :localOwnerUserId
          AND noteLocalKey = :noteLocalKey
        """
    )
    suspend fun deleteForNote(
        localOwnerUserId: String,
        noteLocalKey: String,
    )

    // MARK: - Remote Reconcile

    @Transaction
    suspend fun applyRemoteReconcile(
        localImageKeysToDelete: List<String>,
        imagesToUpsert: List<LocalNoteImageEntity>,
    ) {
        if (localImageKeysToDelete.isNotEmpty()) {
            deleteByLocalImageKeys(
                localImageKeys = localImageKeysToDelete,
            )
        }

        if (imagesToUpsert.isNotEmpty()) {
            upsertAll(
                images = imagesToUpsert,
            )
        }
    }

    // MARK: - Day Statistics

    @Query(
        """
        SELECT
            i.noteLocalKey,
            COUNT(*) AS imageCount
        FROM local_note_images i
        INNER JOIN local_notes n
            ON n.localKey = i.noteLocalKey
           AND n.localOwnerUserId = i.localOwnerUserId
        WHERE n.localOwnerUserId = :localOwnerUserId
          AND n.dayKey >= :startDayKey
          AND n.dayKey < :endDayKeyExclusive
          AND COALESCE(
              n.pendingRemoteStatus,
              n.remoteStatus
          ) = 'active'
        GROUP BY i.noteLocalKey
        """
    )
    fun observeMonthImageCounts(
        localOwnerUserId: String,
        startDayKey: String,
        endDayKeyExclusive: String,
    ): Flow<List<NoteImageCount>>

    // 认领 - Start

    @Query(
        """
    UPDATE local_note_images
    SET localOwnerUserId = :accountUserId,
        noteLocalKey = :newNoteLocalKey,
        updatedAt = :updatedAt
    WHERE localOwnerUserId = :guestLocalOwnerId
      AND noteLocalKey = :oldNoteLocalKey
    """
    )
    suspend fun transferNoteImages(
        guestLocalOwnerId: String,
        accountUserId: String,
        oldNoteLocalKey: String,
        newNoteLocalKey: String,
        updatedAt: Long,
    ): Int

    // 认领 - END

}
