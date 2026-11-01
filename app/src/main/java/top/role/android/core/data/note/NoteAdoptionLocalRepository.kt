package top.role.android.core.data.note

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import top.role.android.core.database.TopRoleDatabase
import top.role.android.core.database.note.LocalNoteEntity

class NoteAdoptionLocalRepository(
    private val database: TopRoleDatabase,
) {

    /**
     * 实时观察 Guest 下符合认领条件的 Note。
     */
    fun observeUnclaimedNotes(
        guestLocalOwnerId: String,
    ): Flow<List<LocalNoteEntity>> {
        return database.localNoteDao()
            .observeUnclaimedNotes(
                guestLocalOwnerId = guestLocalOwnerId,
            )
    }

    fun observeUnclaimedCount(
        guestLocalOwnerId: String,
    ): Flow<Int> {
        return database.localNoteDao()
            .observeUnclaimedNoteCount(
                guestLocalOwnerId = guestLocalOwnerId,
            )
    }

    /**
     * 将 Guest Local Owner 下符合条件的 Note
     * 认领到当前登录账户。
     *
     * 三张表必须保持原子一致：
     * - local_notes
     * - local_note_images
     * - local_note_media_sync
     *
     * 每篇 Note 使用独立 Room Transaction。
     */
    suspend fun claimGuestNotes(
        guestLocalOwnerId: String,
        accountUserId: String,
    ): Int {

        require(guestLocalOwnerId.isNotBlank()) {
            "GUEST_OWNER_REQUIRED"
        }

        require(accountUserId.isNotBlank()) {
            "ACCOUNT_USER_REQUIRED"
        }

        require(guestLocalOwnerId != accountUserId) {
            "OWNER_IDENTITY_UNCHANGED"
        }

        val noteDao = database.localNoteDao()

        val notes = noteDao.findClaimableNotes(
            guestLocalOwnerId = guestLocalOwnerId,
        )

        var claimedCount = 0

        for (note in notes) {

            val claimed = database.withTransaction {

                val imageDao = database.localNoteImageDao()
                val mediaDao = database.localNoteMediaSyncDao()

                val oldLocalKey = note.localKey

                // 1. 生成目标账户下的新 Local Key。
                val newLocalKey = when {
                    note.onlyDay && note.dayKey != null ->
                        LocalNoteKeys.day(
                            localOwnerUserId = accountUserId,
                            dayKey = note.dayKey,
                        )

                    else ->
                        LocalNoteKeys.new(
                            localOwnerUserId = accountUserId,
                        )
                }

                // 2. 检查目标账户是否已经存在相同 Local Key。
                // 包括已归档记录，不仅检查 active。
                val existingNote = noteDao.findByLocalKey(
                    localOwnerUserId = accountUserId,
                    localKey = newLocalKey,
                )

                if (existingNote != null) {
                    return@withTransaction false
                }

                val now = System.currentTimeMillis()

                // 3. 迁移前读取图片数量。
                val images = imageDao.listForNote(
                    localOwnerUserId = guestLocalOwnerId,
                    noteLocalKey = oldLocalKey,
                )

                // 4. 迁移前读取 Media Sync State。
                val mediaState = mediaDao.findForNote(
                    localOwnerUserId = guestLocalOwnerId,
                    noteLocalKey = oldLocalKey,
                )

                // 5. 迁移 Note。
                val noteUpdated = noteDao.transferGuestNote(
                    guestLocalOwnerId = guestLocalOwnerId,
                    accountUserId = accountUserId,
                    oldLocalKey = oldLocalKey,
                    newLocalKey = newLocalKey,
                )

                // Note 已不存在或不再符合认领条件。
                if (noteUpdated != 1) {
                    return@withTransaction false
                }

                // 6. 迁移全部图片关系。
                val imageUpdated = imageDao.transferNoteImages(
                    guestLocalOwnerId = guestLocalOwnerId,
                    accountUserId = accountUserId,
                    oldNoteLocalKey = oldLocalKey,
                    newNoteLocalKey = newLocalKey,
                    updatedAt = now,
                )

                check(imageUpdated == images.size) {
                    "IMAGE_CLAIM_FAILED: $oldLocalKey"
                }

                // 7. 迁移 Media Sync State。
                if (mediaState != null) {

                    val newSyncKey = makeSyncKey(
                        localOwnerUserId = accountUserId,
                        noteLocalKey = newLocalKey,
                    )

                    val mediaUpdated =
                        mediaDao.transferNoteMediaSync(
                            guestLocalOwnerId = guestLocalOwnerId,
                            accountUserId = accountUserId,
                            oldNoteLocalKey = oldLocalKey,
                            newNoteLocalKey = newLocalKey,
                            newSyncKey = newSyncKey,
                            updatedAt = now,
                        )

                    check(mediaUpdated == 1) {
                        "MEDIA_SYNC_CLAIM_FAILED: $oldLocalKey"
                    }
                }

                true
            }

            if (claimed) {
                claimedCount++
            }
        }

        return claimedCount
    }

    /**
     * 与现有 Media Sync Key 生成规则保持一致。
     */
    private fun makeSyncKey(
        localOwnerUserId: String,
        noteLocalKey: String,
    ): String {
        return "user:$localOwnerUserId:note-media:$noteLocalKey"
    }
}