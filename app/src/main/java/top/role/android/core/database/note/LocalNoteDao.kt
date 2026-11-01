package top.role.android.core.database.note

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import top.role.android.core.model.note.NoteSyncState

@Dao
interface LocalNoteDao {

    // MARK: - Identity

    @Query(
        """
        SELECT * FROM local_notes
        WHERE localOwnerUserId = :localOwnerUserId
          AND localKey = :localKey
        LIMIT 1
        """
    )
    suspend fun findByLocalKey(
        localOwnerUserId: String,
        localKey: String,
    ): LocalNoteEntity?

    @Query(
        """
        SELECT * FROM local_notes
        WHERE localOwnerUserId = :localOwnerUserId
          AND noteId = :noteId
        LIMIT 1
        """
    )
    suspend fun findByNoteId(
        localOwnerUserId: String,
        noteId: String,
    ): LocalNoteEntity?

    @Query(
        """
        SELECT * FROM local_notes
        WHERE localOwnerUserId = :localOwnerUserId
          AND clientKey = :clientKey
        LIMIT 1
        """
    )
    suspend fun findByClientKey(
        localOwnerUserId: String,
        clientKey: String,
    ): LocalNoteEntity?

    // MARK: - Diary

    @Query(
        """
        SELECT * FROM local_notes
        WHERE localOwnerUserId = :localOwnerUserId
          AND onlyDay = 1
          AND dayKey = :dayKey
          AND COALESCE(
              pendingRemoteStatus,
              remoteStatus
          ) = 'active'
        ORDER BY localUpdatedAt DESC
        LIMIT 1
        """
    )
    suspend fun findDayOnlyNote(
        localOwnerUserId: String,
        dayKey: String,
    ): LocalNoteEntity?

    @Query(
        """
        SELECT * FROM local_notes
        WHERE localOwnerUserId = :localOwnerUserId
          AND localKey = :localKey
        LIMIT 1
        """
    )
    fun observeByLocalKey(
        localOwnerUserId: String,
        localKey: String,
    ): Flow<LocalNoteEntity?>

    @Query(
        """
        SELECT * FROM local_notes
        WHERE localOwnerUserId = :localOwnerUserId
          AND onlyDay = 1
          AND dayKey = :dayKey
          AND COALESCE(
              pendingRemoteStatus,
              remoteStatus
          ) = 'active'
        ORDER BY localUpdatedAt DESC
        LIMIT 1
        """
    )
    fun observeDayOnlyNote(
        localOwnerUserId: String,
        dayKey: String,
    ): Flow<LocalNoteEntity?>

    // MARK: - Day

    @Query(
        """
        SELECT * FROM local_notes
        WHERE localOwnerUserId = :localOwnerUserId
          AND dayKey >= :startDayKey
          AND dayKey < :endDayKeyExclusive
          AND COALESCE(
              pendingRemoteStatus,
              remoteStatus
          ) = 'active'
        ORDER BY pinned DESC, localUpdatedAt DESC
        """
    )
    fun observeMonthNotesForDayHome(
        localOwnerUserId: String,
        startDayKey: String,
        endDayKeyExclusive: String,
    ): Flow<List<LocalNoteEntity>>

    @Query(
        """
        SELECT * FROM local_notes
        WHERE localOwnerUserId = :localOwnerUserId
          AND dayKey = :dayKey
          AND COALESCE(
              pendingRemoteStatus,
              remoteStatus
          ) = 'active'
        ORDER BY onlyDay DESC, localUpdatedAt DESC
        """
    )
    fun observeNotesForDay(
        localOwnerUserId: String,
        dayKey: String,
    ): Flow<List<LocalNoteEntity>>

    // MARK: - Library

    @Query(
        """
        SELECT * FROM local_notes
        WHERE localOwnerUserId = :localOwnerUserId
          AND COALESCE(
              pendingRemoteStatus,
              remoteStatus
          ) = 'active'
        ORDER BY localUpdatedAt DESC
        """
    )
    fun observeRecentNotes(
        localOwnerUserId: String,
    ): Flow<List<LocalNoteEntity>>

    @Query(
        """
        SELECT * FROM local_notes
        WHERE localOwnerUserId = :localOwnerUserId
          AND COALESCE(
              pendingRemoteStatus,
              remoteStatus
          ) = 'active'
          AND pinned = 1
        ORDER BY localUpdatedAt DESC
        """
    )
    fun observePinnedNotes(
        localOwnerUserId: String,
    ): Flow<List<LocalNoteEntity>>

    @Query(
        """
        SELECT * FROM local_notes
        WHERE localOwnerUserId = :localOwnerUserId
          AND COALESCE(
              pendingRemoteStatus,
              remoteStatus
          ) = 'active'
          AND onlyDay = 1
          AND dayKey IS NOT NULL
        ORDER BY dayKey DESC, localUpdatedAt DESC
        """
    )
    fun observeDiaryNotes(
        localOwnerUserId: String,
    ): Flow<List<LocalNoteEntity>>

    @Query(
        """
        SELECT * FROM local_notes
        WHERE localOwnerUserId = :localOwnerUserId
          AND COALESCE(
              pendingRemoteStatus,
              remoteStatus
          ) = 'archived'
        ORDER BY localUpdatedAt DESC
        """
    )
    fun observeArchivedNotes(
        localOwnerUserId: String,
    ): Flow<List<LocalNoteEntity>>

    @Query(
        """
        SELECT * FROM local_notes
        WHERE localOwnerUserId = :localOwnerUserId
          AND COALESCE(
              pendingRemoteStatus,
              remoteStatus
          ) = 'active'
          AND (
              title LIKE '%' || :query || '%'
              OR text LIKE '%' || :query || '%'
          )
        ORDER BY localUpdatedAt DESC
        """
    )
    fun searchNotes(
        localOwnerUserId: String,
        query: String,
    ): Flow<List<LocalNoteEntity>>

    // MARK: - Sync

    @Query(
        """
        SELECT * FROM local_notes
        WHERE localOwnerUserId = :localOwnerUserId
          AND syncState IN ('pending', 'waiting')
        ORDER BY localUpdatedAt ASC
        """
    )
    suspend fun findNotesNeedingSync(
        localOwnerUserId: String,
    ): List<LocalNoteEntity>

    @Query(
        """
        SELECT * FROM local_notes
        WHERE localOwnerUserId = :localOwnerUserId
          AND noteId IS NOT NULL
        ORDER BY localUpdatedAt ASC
        """
    )
    suspend fun findRemoteBackedNotes(
        localOwnerUserId: String,
    ): List<LocalNoteEntity>

    @Query(
        """
        SELECT * FROM local_notes
        WHERE localOwnerUserId = :localOwnerUserId
          AND syncState = :syncState
        ORDER BY localUpdatedAt ASC
        """
    )
    suspend fun findBySyncState(
        localOwnerUserId: String,
        syncState: NoteSyncState,
    ): List<LocalNoteEntity>

    // MARK: - Adoption

    /**
     * 查询指定本地身份下的全部笔记。
     *
     * 认领属于数据归属迁移，不按同步状态过滤。
     */
    @Query(
        """
    SELECT *
    FROM local_notes
    WHERE localOwnerUserId = :guestLocalOwnerId
      AND noteId IS NULL
      AND remoteVersion IS NULL
    ORDER BY localUpdatedAt DESC
    """
    )
    fun observeUnclaimedNotes(
        guestLocalOwnerId: String,
    ): Flow<List<LocalNoteEntity>>

    /**
     * 实时观察待认领笔记数量。
     */
    @Query(
        """
    SELECT COUNT(*)
    FROM local_notes
    WHERE localOwnerUserId = :guestLocalOwnerId
      AND noteId IS NULL
      AND remoteVersion IS NULL
    """
    )
    fun observeUnclaimedNoteCount(
        guestLocalOwnerId: String,
    ): Flow<Int>

    /**
     * Note归属更新。
     */
    @Query(
        """
    SELECT *
    FROM local_notes
    WHERE localOwnerUserId = :guestLocalOwnerId
      AND noteId IS NULL
      AND remoteVersion IS NULL
    ORDER BY localUpdatedAt ASC
    """
    )
    suspend fun findClaimableNotes(
        guestLocalOwnerId: String,
    ): List<LocalNoteEntity>

    // 更新
    @Query(
        """
    UPDATE local_notes
    SET localOwnerUserId = :accountUserId,
        localKey = :newLocalKey
    WHERE localOwnerUserId = :guestLocalOwnerId
      AND localKey = :oldLocalKey
      AND noteId IS NULL
      AND remoteVersion IS NULL
    """
    )
    suspend fun transferGuestNote(
        guestLocalOwnerId: String,
        accountUserId: String,
        oldLocalKey: String,
        newLocalKey: String,
    ): Int

    // MARK: - Persistence

    @Insert(
        onConflict = OnConflictStrategy.ABORT,
    )
    suspend fun insert(
        note: LocalNoteEntity,
    ): Long

    @Update
    suspend fun update(
        note: LocalNoteEntity,
    )

    @Upsert
    suspend fun upsert(
        note: LocalNoteEntity,
    )

    @Delete
    suspend fun delete(
        note: LocalNoteEntity,
    )
}