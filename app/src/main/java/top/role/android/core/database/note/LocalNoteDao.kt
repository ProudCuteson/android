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
        WHERE ownerUserId = :ownerUserId
          AND localKey = :localKey
        LIMIT 1
        """
    )
    suspend fun findByLocalKey(
        ownerUserId: String,
        localKey: String,
    ): LocalNoteEntity?

    @Query(
        """
        SELECT * FROM local_notes
        WHERE ownerUserId = :ownerUserId
          AND noteId = :noteId
        LIMIT 1
        """
    )
    suspend fun findByNoteId(
        ownerUserId: String,
        noteId: String,
    ): LocalNoteEntity?

    @Query(
        """
        SELECT * FROM local_notes
        WHERE ownerUserId = :ownerUserId
          AND clientKey = :clientKey
        LIMIT 1
        """
    )
    suspend fun findByClientKey(
        ownerUserId: String,
        clientKey: String,
    ): LocalNoteEntity?

    // MARK: - Diary

    @Query(
        """
        SELECT * FROM local_notes
        WHERE ownerUserId = :ownerUserId
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
        ownerUserId: String,
        dayKey: String,
    ): LocalNoteEntity?

    @Query(
        """
    SELECT * FROM local_notes
    WHERE ownerUserId = :ownerUserId
      AND localKey = :localKey
    LIMIT 1
    """
    )
    fun observeByLocalKey(
        ownerUserId: String,
        localKey: String,
    ): Flow<LocalNoteEntity?>

    @Query(
        """
        SELECT * FROM local_notes
        WHERE ownerUserId = :ownerUserId
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
        ownerUserId: String,
        dayKey: String,
    ): Flow<LocalNoteEntity?>

    // MARK: - Day

    @Query(
        """
        SELECT * FROM local_notes
        WHERE ownerUserId = :ownerUserId
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
        ownerUserId: String,
        startDayKey: String,
        endDayKeyExclusive: String,
    ): Flow<List<LocalNoteEntity>>

    @Query(
        """
        SELECT * FROM local_notes
        WHERE ownerUserId = :ownerUserId
          AND dayKey = :dayKey
          AND COALESCE(
              pendingRemoteStatus,
              remoteStatus
          ) = 'active'
        ORDER BY onlyDay DESC, localUpdatedAt DESC
        """
    )
    fun observeNotesForDay(
        ownerUserId: String,
        dayKey: String,
    ): Flow<List<LocalNoteEntity>>

    // MARK: - Library

    @Query(
        """
        SELECT * FROM local_notes
        WHERE ownerUserId = :ownerUserId
          AND COALESCE(
              pendingRemoteStatus,
              remoteStatus
          ) = 'active'
        ORDER BY localUpdatedAt DESC
        """
    )
    fun observeRecentNotes(
        ownerUserId: String,
    ): Flow<List<LocalNoteEntity>>

    @Query(
        """
        SELECT * FROM local_notes
        WHERE ownerUserId = :ownerUserId
          AND COALESCE(
              pendingRemoteStatus,
              remoteStatus
          ) = 'active'
          AND pinned = 1
        ORDER BY localUpdatedAt DESC
        """
    )
    fun observePinnedNotes(
        ownerUserId: String,
    ): Flow<List<LocalNoteEntity>>

    @Query(
        """
        SELECT * FROM local_notes
        WHERE ownerUserId = :ownerUserId
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
        ownerUserId: String,
    ): Flow<List<LocalNoteEntity>>

    @Query(
        """
        SELECT * FROM local_notes
        WHERE ownerUserId = :ownerUserId
          AND COALESCE(
              pendingRemoteStatus,
              remoteStatus
          ) = 'archived'
        ORDER BY localUpdatedAt DESC
        """
    )
    fun observeArchivedNotes(
        ownerUserId: String,
    ): Flow<List<LocalNoteEntity>>

    @Query(
        """
        SELECT * FROM local_notes
        WHERE ownerUserId = :ownerUserId
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
        ownerUserId: String,
        query: String,
    ): Flow<List<LocalNoteEntity>>

    // MARK: - Sync

    @Query(
        """
        SELECT * FROM local_notes
        WHERE ownerUserId = :ownerUserId
          AND syncState IN ('pending', 'waiting')
        ORDER BY localUpdatedAt ASC
        """
    )
    suspend fun findNotesNeedingSync(
        ownerUserId: String,
    ): List<LocalNoteEntity>

    @Query(
        """
        SELECT * FROM local_notes
        WHERE ownerUserId = :ownerUserId
          AND noteId IS NOT NULL
        ORDER BY localUpdatedAt ASC
        """
    )
    suspend fun findRemoteBackedNotes(
        ownerUserId: String,
    ): List<LocalNoteEntity>

    @Query(
        """
        SELECT * FROM local_notes
        WHERE ownerUserId = :ownerUserId
          AND syncState = :syncState
        ORDER BY localUpdatedAt ASC
        """
    )
    suspend fun findBySyncState(
        ownerUserId: String,
        syncState: NoteSyncState,
    ): List<LocalNoteEntity>

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