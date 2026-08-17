package top.role.android.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import top.role.android.core.database.entity.LocalNoteImageEntity

@Dao
interface LocalNoteImageDao {

    /*
     * 实时观察某一条 Note 的全部图片。
     */
    @Query(
        """
        SELECT *
        FROM local_note_images
        WHERE ownerUserId = :ownerUserId
          AND noteLocalKey = :noteLocalKey
        ORDER BY position ASC, createdAt ASC
        """
    )
    fun observeForNote(
        ownerUserId: String,
        noteLocalKey: String,
    ): Flow<List<LocalNoteImageEntity>>

    /*
     * 一次性读取某一条 Note 的全部图片。
     */
    @Query(
        """
        SELECT *
        FROM local_note_images
        WHERE ownerUserId = :ownerUserId
          AND noteLocalKey = :noteLocalKey
        ORDER BY position ASC, createdAt ASC
        """
    )
    suspend fun listForNote(
        ownerUserId: String,
        noteLocalKey: String,
    ): List<LocalNoteImageEntity>

    /*
     * 根据本地附件 key 查询。
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
     * 获取待上传图片。
     *
     * local:
     * 从未上传。
     *
     * failed:
     * 上次上传失败，可再次重试。
     *
     * uploading 不包含在这里，
     * 防止同一任务被并发重复处理。
     */
    @Query(
        """
        SELECT *
        FROM local_note_images
        WHERE ownerUserId = :ownerUserId
          AND uploadState IN ('local', 'failed')
        ORDER BY createdAt ASC
        LIMIT :limit
        """
    )
    suspend fun listPendingUploads(
        ownerUserId: String,
        limit: Int,
    ): List<LocalNoteImageEntity>

    /*
     * 获取下一张图片的位置。
     *
     * 当前 Note 没有图片时返回 0。
     */
    @Query(
        """
        SELECT COALESCE(MAX(position) + 1, 0)
        FROM local_note_images
        WHERE ownerUserId = :ownerUserId
          AND noteLocalKey = :noteLocalKey
        """
    )
    suspend fun nextPosition(
        ownerUserId: String,
        noteLocalKey: String,
    ): Int

    /*
     * 新建或覆盖本地图片记录。
     */
    @Insert(
        onConflict =
            OnConflictStrategy.REPLACE,
    )
    suspend fun upsert(
        image: LocalNoteImageEntity,
    )

    /*
     * 普通完整更新。
     *
     * 保留这个方法，
     * 后续修改多个普通字段时仍然有用。
     */
    @Update
    suspend fun update(
        image: LocalNoteImageEntity,
    )

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
     *
     * 保留本地文件和记录，
     * 后续可以 retry。
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
     * Room 中可能残留 uploading。
     *
     * 下次恢复同步前将它们重新置为 local，
     * 使其重新进入 pending upload 队列。
     */
    @Query(
        """
        UPDATE local_note_images
        SET uploadState = 'local',
            uploadError = NULL,
            updatedAt = :updatedAt
        WHERE ownerUserId = :ownerUserId
          AND uploadState = 'uploading'
        """
    )
    suspend fun resetInterruptedUploads(
        ownerUserId: String,
        updatedAt: Long,
    )

    @Query(
        """
        UPDATE local_note_images
        SET noteLocalKey = :newNoteLocalKey,
            updatedAt = :updatedAt
        WHERE ownerUserId = :ownerUserId
          AND noteLocalKey = :oldNoteLocalKey
    """
    )
    suspend fun rebindNoteLocalKey(
        ownerUserId: String,
        oldNoteLocalKey: String,
        newNoteLocalKey: String,
        updatedAt: Long,
    )

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

    /*
     * 删除单张本地附件记录。
     *
     * 本地文件本身由 LocalImageFileStore 删除，
     * DAO 不负责文件系统。
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

    /*
     * 删除某条 Note 的全部本地附件记录。
     */
    @Query(
        """
        DELETE FROM local_note_images
        WHERE ownerUserId = :ownerUserId
          AND noteLocalKey = :noteLocalKey
        """
    )
    suspend fun deleteForNote(
        ownerUserId: String,
        noteLocalKey: String,
    )
}