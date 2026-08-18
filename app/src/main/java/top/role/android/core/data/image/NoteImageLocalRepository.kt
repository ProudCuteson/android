package top.role.android.core.data.image

import kotlinx.coroutines.flow.Flow
import top.role.android.core.database.dao.LocalNoteImageDao
import top.role.android.core.database.entity.LocalNoteImageEntity
import top.role.android.core.model.note.PersistedNoteMediaItem

data class NoteImageRemoteReconcileResult(
    val removedLocalPaths: List<String>,
)

class NoteImageLocalRepository(
    private val dao: LocalNoteImageDao,
) {

    fun observeForNote(
        ownerUserId: String,
        noteLocalKey: String,
    ): Flow<List<LocalNoteImageEntity>> {
        return dao.observeForNote(
            ownerUserId =
                ownerUserId,

            noteLocalKey =
                noteLocalKey,
        )
    }

    suspend fun listForNote(
        ownerUserId: String,
        noteLocalKey: String,
    ): List<LocalNoteImageEntity> {
        return dao.listForNote(
            ownerUserId =
                ownerUserId,

            noteLocalKey =
                noteLocalKey,
        )
    }

    suspend fun get(
        localImageKey: String,
    ): LocalNoteImageEntity? {
        return dao.getByLocalImageKey(
            localImageKey =
                localImageKey,
        )
    }

    suspend fun upsert(
        image: LocalNoteImageEntity,
    ) {
        dao.upsert(
            image =
                image,
        )
    }

    suspend fun nextPosition(
        ownerUserId: String,
        noteLocalKey: String,
    ): Int {
        return dao.nextPosition(
            ownerUserId =
                ownerUserId,

            noteLocalKey =
                noteLocalKey,
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
            localImageKey =
                localImageKey,

            updatedAt =
                System.currentTimeMillis(),
        )
    }

    suspend fun markUploaded(
        localImageKey: String,
        imageId: String,
    ) {
        dao.markUploaded(
            localImageKey =
                localImageKey,

            imageId =
                imageId,

            updatedAt =
                System.currentTimeMillis(),
        )
    }

    suspend fun markUploadFailed(
        localImageKey: String,
        errorMessage: String?,
    ) {
        dao.markUploadFailed(
            localImageKey =
                localImageKey,

            errorMessage =
                errorMessage,

            updatedAt =
                System.currentTimeMillis(),
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
            localImageKey =
                localImageKey,

            position =
                position,

            updatedAt =
                System.currentTimeMillis(),
        )
    }

    suspend fun delete(
        localImageKey: String,
    ) {
        dao.deleteByLocalImageKey(
            localImageKey =
                localImageKey,
        )
    }

    suspend fun deleteForNote(
        ownerUserId: String,
        noteLocalKey: String,
    ) {
        dao.deleteForNote(
            ownerUserId =
                ownerUserId,

            noteLocalKey =
                noteLocalKey,
        )
    }

    // MARK: - Remote Reconcile

    /**
     * 使用 authoritative remote media
     * 对本地 Note image relation 做 reconcile。
     *
     * 注意：
     *
     * 这个方法不判断 NoteMediaSyncState。
     *
     * 调用方必须确保当前远端数据
     * 可以覆盖本地 relation，
     * 正常只应在 Media Synced 状态使用。
     *
     * 规则：
     *
     * 1. Remote 与 Local 都存在相同 imageId
     *    → 保留已有 Local row
     *    → 保留 localPath / mime / size 等缓存信息
     *    → 只校正 position 和 uploaded 状态
     *
     * 2. Remote 新增 imageId
     *    → 创建 hydrated Local row
     *
     * 3. Local 中已有 imageId，
     *    但 authoritative Remote 已不存在
     *    → 删除该 relation
     *
     * 4. Local imageId == null
     *    → 不删除
     *
     *    即使理论上 Synced 状态不应该存在，
     *    也保守保护尚未上传完成的本地图片。
     */
    suspend fun reconcileAuthoritativeRemoteMedia(
        ownerUserId: String,
        noteLocalKey: String,
        remoteMedia: List<PersistedNoteMediaItem>,
    ): NoteImageRemoteReconcileResult {
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

        val now =
            System.currentTimeMillis()

        /*
         * Defensive normalize。
         *
         * Remote 正常已经按 order 返回，
         * Android 再统一压平成：
         *
         * 0,1,2,...
         */
        val normalizedRemote =
            remoteMedia
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

        /*
         * 后端正常已经禁止重复 imageId。
         *
         * 本地 boundary 再守一次，
         * 避免异常 payload 导致 reconcile 结果不确定。
         */
        val remoteImageIds =
            normalizedRemote
                .map {
                    it.imageId
                }

        require(
            remoteImageIds
                .distinct()
                .size ==
                    remoteImageIds.size,
        ) {
            "DUPLICATE_REMOTE_NOTE_MEDIA"
        }

        val current =
            dao.listForNote(
                ownerUserId =
                    ownerUserId,

                noteLocalKey =
                    noteLocalKey,
            )

        /*
         * 按 imageId 建索引。
         *
         * imageId == null 的 row
         * 是尚未取得 remote asset identity 的本地附件，
         * 不参与 remote match。
         */
        val currentByImageId =
            current
                .mapNotNull { image ->
                    val imageId =
                        image.imageId
                            ?.takeIf {
                                it.isNotBlank()
                            }
                            ?: return@mapNotNull null

                    imageId to
                            image
                }
                .toMap()

        /*
         * Remote 当前 authoritative media
         * 转成本地 rows。
         */
        val imagesToUpsert =
            normalizedRemote
                .map { remoteItem ->
                    val existing =
                        currentByImageId[
                            remoteItem.imageId
                        ]

                    if (
                        existing != null
                    ) {
                        /*
                         * 已经存在：
                         *
                         * 保留：
                         * - localImageKey
                         * - localPath
                         * - mime
                         * - width / height
                         * - bytes
                         * - createdAt
                         *
                         * 只更新 relation / sync 相关字段。
                         */
                        existing.copy(
                            uploadState =
                                "uploaded",

                            uploadError =
                                null,

                            position =
                                remoteItem.order,

                            updatedAt =
                                now,
                        )
                    } else {
                        /*
                         * Remote 新出现的 relation。
                         *
                         * 这类 row 没有本地文件，
                         * UI 后续使用 imageId
                         * 获取 private preview URL。
                         */
                        LocalNoteImageEntity(
                            localImageKey =
                                remoteLocalImageKey(
                                    ownerUserId =
                                        ownerUserId,

                                    noteLocalKey =
                                        noteLocalKey,

                                    imageId =
                                        remoteItem.imageId,
                                ),

                            ownerUserId =
                                ownerUserId,

                            noteLocalKey =
                                noteLocalKey,

                            imageId =
                                remoteItem.imageId,

                            localPath =
                                null,

                            mime =
                                null,

                            width =
                                null,

                            height =
                                null,

                            bytes =
                                null,

                            uploadState =
                                "uploaded",

                            uploadError =
                                null,

                            position =
                                remoteItem.order,

                            createdAt =
                                now,

                            updatedAt =
                                now,
                        )
                    }
                }

        val authoritativeRemoteIds =
            remoteImageIds
                .toSet()

        /*
         * 删除 Remote 已不存在的 remote-backed relation。
         *
         * 注意：
         *
         * imageId == null
         * 的本地附件不删除。
         */
        val removed =
            current
                .filter { image ->
                    val imageId =
                        image.imageId
                            ?.takeIf {
                                it.isNotBlank()
                            }
                            ?: return@filter false

                    imageId !in
                            authoritativeRemoteIds
                }

        val localImageKeysToDelete =
            removed
                .map {
                    it.localImageKey
                }

        /*
         * delete + upsert
         * 在同一个 Room transaction 中完成。
         */
        dao.applyRemoteReconcile(
            localImageKeysToDelete =
                localImageKeysToDelete,

            imagesToUpsert =
                imagesToUpsert,
        )

        /*
         * Repository 不操作 filesystem。
         *
         * 如果被 Remote reconcile 删除的 relation
         * 还有 app-private local file，
         * 把路径返回给上层 Service。
         */
        val removedLocalPaths =
            removed
                .mapNotNull { image ->
                    image.localPath
                        ?.takeIf {
                            it.isNotBlank()
                        }
                }
                .distinct()

        return NoteImageRemoteReconcileResult(
            removedLocalPaths =
                removedLocalPaths,
        )
    }

    // MARK: - Helpers

    /**
     * Remote hydration 使用 deterministic local identity。
     *
     * 不用 UUID，避免同一 remote image
     * 多次 hydration 产生不同 row。
     */
    private fun remoteLocalImageKey(
        ownerUserId: String,
        noteLocalKey: String,
        imageId: String,
    ): String {
        return buildString {
            append(
                "remote:"
            )

            append(
                ownerUserId,
            )

            append(
                ":",
            )

            append(
                noteLocalKey,
            )

            append(
                ":",
            )

            append(
                imageId,
            )
        }
    }
}