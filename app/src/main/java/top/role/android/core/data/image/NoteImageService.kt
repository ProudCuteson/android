package top.role.android.core.data.image

import java.io.File
import java.util.UUID
import android.net.Uri
import androidx.core.net.toUri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import top.role.android.core.database.entity.LocalNoteImageEntity
import top.role.android.core.model.image.MobileImageScene
import top.role.android.core.data.image.note.NoteMediaLocalRepository

class NoteImageService(
    private val localRepository: NoteImageLocalRepository,
    private val fileStore: LocalImageFileStore,
    private val sourceInspector: ImageSourceInspector,
    private val imageRepository: ImageRepository,
    private val noteMediaLocalRepository: NoteMediaLocalRepository,
) {

    fun observeImages(
        ownerUserId: String,
        noteLocalKey: String,
    ): Flow<List<LocalNoteImageEntity>> {
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

        return localRepository
            .observeForNote(
                ownerUserId =
                    ownerUserId,

                noteLocalKey =
                    noteLocalKey,
            )
    }

    fun observeMonthImageCounts(
        ownerUserId: String,
        startDayKey: String,
        endDayKeyExclusive: String,
    ): Flow<Map<String, Int>> {
        require(ownerUserId.isNotBlank()) {
            "OWNER_USER_ID_REQUIRED"
        }

        return localRepository.observeMonthImageCounts(
            ownerUserId = ownerUserId,
            startDayKey = startDayKey,
            endDayKeyExclusive = endDayKeyExclusive,
        ).map { rows ->
            rows.associate { it.noteLocalKey to it.imageCount }
        }
    }

    suspend fun addImage(
        ownerUserId: String,
        noteLocalKey: String,
        sourceUri: Uri,
    ): LocalNoteImageEntity {
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

        val sourceInfo =
            sourceInspector
                .inspect(
                    sourceUri =
                        sourceUri,
                )

        val localPath =
            fileStore
                .importImage(
                    sourceUri =
                        sourceUri,

                    mime =
                        sourceInfo.mime,
                )

        val now = System.currentTimeMillis()

        val localImageKey = UUID.randomUUID().toString()

        val position = localRepository
            .nextPosition(
                ownerUserId = ownerUserId,
                noteLocalKey = noteLocalKey,
            )

        val localImage = LocalNoteImageEntity(
            localImageKey = localImageKey,
            ownerUserId = ownerUserId,
            noteLocalKey = noteLocalKey,
            imageId = null,
            localPath = localPath,
            mime = sourceInfo.mime,
            width = sourceInfo.width,
            height = sourceInfo.height,
            bytes = sourceInfo.bytes,
            uploadState = "local",
            uploadError = null,
            position = position,
            createdAt = now,
            updatedAt = now,
        )

        try {
            localRepository
                .upsert(
                    image =
                        localImage,
                )

            /*
             * 本地 Note media 已经发生变化。
             *
             * 即使图片 asset 还没上传，
             * desired local media 已经包含这张图片。
             *
             * Media Push 会在后续独立同步阶段处理。
             */
            noteMediaLocalRepository
                .markPending(
                    ownerUserId = ownerUserId,
                    noteLocalKey = noteLocalKey,
                )

        } catch (
            error: Throwable,
        ) {
            /*
             * Room 写入失败时，
             * 本地文件也不能成为孤儿文件。
             */
            fileStore
                .delete(
                    localPath =
                        localPath,
                )

            throw error
        }

        /*
         * 从这里开始，本地附件已经成立。
         *
         * 网络上传失败不应该回滚：
         * - local file
         * - LocalNoteImageEntity
         */
        runCatching {
            upload(
                ownerUserId =
                    ownerUserId,

                localImageKey =
                    localImageKey,
            )
        }

        /*
         * 上传可能已经修改：
         * imageId
         * uploadState
         * uploadError
         *
         * 所以重新读取一次最新状态。
         */
        return localRepository
            .get(
                localImageKey =
                    localImageKey,
            )
            ?: localImage
    }

    suspend fun upload(
        ownerUserId: String,
        localImageKey: String,
    ) {
        require(
            ownerUserId.isNotBlank(),
        ) {
            "OWNER_USER_ID_REQUIRED"
        }

        require(
            localImageKey.isNotBlank(),
        ) {
            "LOCAL_IMAGE_KEY_REQUIRED"
        }

        val local =
            localRepository
                .get(
                    localImageKey =
                        localImageKey,
                )
                ?: return

        require(
            local.ownerUserId ==
                    ownerUserId,
        ) {
            "NOTE_IMAGE_OWNER_MISMATCH"
        }

        /*
         * Remote hydrated image
         * 或已经完成 Asset Upload 的本地图片。
         *
         * 已经有正式 imageId，
         * 不需要再次上传。
         */
        if (
            local.uploadState ==
            "uploaded" &&
            !local.imageId.isNullOrBlank()
        ) {
            return
        }

        /*
         * 真正需要执行 Asset Upload 的记录
         * 必须具有本地文件。
         */
        val localPath =
            local.localPath
                ?.takeIf {
                    it.isNotBlank()
                }

        if (
            localPath == null
        ) {
            localRepository
                .markUploadFailed(
                    localImageKey =
                        local.localImageKey,

                    errorMessage =
                        "LOCAL_IMAGE_PATH_MISSING",
                )

            return
        }

        val localFile =
            File(
                localPath,
            )

        if (
            !localFile.exists() ||
            !localFile.isFile
        ) {
            localRepository
                .markUploadFailed(
                    localImageKey =
                        local.localImageKey,

                    errorMessage =
                        "LOCAL_IMAGE_FILE_NOT_FOUND",
                )

            return
        }

        /*
         * Remote hydration 允许 bytes == null，
         * 但真正执行 Asset Upload 的本地图片
         * 必须有合法 bytes。
         */
        val bytes =
            local.bytes

        if (
            bytes == null ||
            bytes <= 0L
        ) {
            localRepository
                .markUploadFailed(
                    localImageKey =
                        local.localImageKey,

                    errorMessage =
                        "IMAGE_SIZE_INVALID",
                )

            return
        }

        /*
         * 同理：
         * Remote hydrated image 可以不知道 mime，
         * 真正 Asset Upload 时必须存在。
         */
        val mime =
            local.mime
                ?.takeIf {
                    it.isNotBlank()
                }

        if (
            mime == null
        ) {
            localRepository
                .markUploadFailed(
                    localImageKey =
                        local.localImageKey,

                    errorMessage =
                        "IMAGE_MIME_MISSING",
                )

            return
        }

        localRepository
            .markUploading(
                localImageKey =
                    local.localImageKey,
            )

        try {
            val sourceInfo =
                ImageSourceInfo(
                    mime =
                        mime,

                    bytes =
                        bytes,

                    fileName =
                        localFile.name,

                    width =
                        local.width,

                    height =
                        local.height,
                )

            val asset =
                imageRepository
                    .uploadPrivateImage(
                        sourceUri =
                            localFile.toUri(),

                        sourceInfo =
                            sourceInfo,

                        scene =
                            MobileImageScene.Note,
                    )

            localRepository
                .markUploaded(
                    localImageKey =
                        local.localImageKey,

                    imageId =
                        asset.id,
                )

            /*
             * Asset 已获得正式 imageId。
             *
             * Media relation 需要进入下一轮同步。
             */
            noteMediaLocalRepository
                .markPending(
                    ownerUserId =
                        ownerUserId,

                    noteLocalKey =
                        local.noteLocalKey,
                )

        } catch (
            error: Throwable,
        ) {
            localRepository
                .markUploadFailed(
                    localImageKey =
                        local.localImageKey,

                    errorMessage =
                        normalizeUploadError(
                            error =
                                error,
                        ),
                )

            throw error
        }
    }

    suspend fun retryUpload(
        ownerUserId: String,
        localImageKey: String,
    ) {
        upload(
            ownerUserId =
                ownerUserId,

            localImageKey =
                localImageKey,
        )
    }

    suspend fun rebindNote(
        ownerUserId: String,
        oldNoteLocalKey: String,
        newNoteLocalKey: String,
        noteId: String? = null,
    ) {
        require(
            ownerUserId.isNotBlank(),
        ) {
            "OWNER_USER_ID_REQUIRED"
        }

        require(
            oldNoteLocalKey.isNotBlank(),
        ) {
            "OLD_NOTE_LOCAL_KEY_REQUIRED"
        }

        require(
            newNoteLocalKey.isNotBlank(),
        ) {
            "NEW_NOTE_LOCAL_KEY_REQUIRED"
        }

        if (
            oldNoteLocalKey ==
            newNoteLocalKey
        ) {
            /*
             * localKey 没变，
             * 但 Note 可能刚刚获得正式 noteId。
             */
            noteMediaLocalRepository
                .rebindNote(
                    ownerUserId =
                        ownerUserId,

                    oldNoteLocalKey =
                        oldNoteLocalKey,

                    newNoteLocalKey =
                        newNoteLocalKey,

                    noteId =
                        noteId,
                )

            return
        }

        /*
         * 先迁移真正的本地附件。
         */
        localRepository
            .rebindNoteLocalKey(
                ownerUserId =
                    ownerUserId,

                oldNoteLocalKey =
                    oldNoteLocalKey,

                newNoteLocalKey =
                    newNoteLocalKey,
            )

        /*
         * 再迁移对应 Media Sync State。
         */
        noteMediaLocalRepository
            .rebindNote(
                ownerUserId =
                    ownerUserId,

                oldNoteLocalKey =
                    oldNoteLocalKey,

                newNoteLocalKey =
                    newNoteLocalKey,

                noteId =
                    noteId,
            )
    }

    suspend fun uploadPending(
        ownerUserId: String,
        limit: Int = 20,
    ) {
        require(
            ownerUserId.isNotBlank(),
        ) {
            "OWNER_USER_ID_REQUIRED"
        }

        require(
            limit > 0,
        ) {
            "IMAGE_UPLOAD_LIMIT_INVALID"
        }

        /*
         * App 上次可能在 uploading 状态时被杀掉。
         *
         * 这里只恢复当前账号的中断任务。
         */
        localRepository
            .resetInterruptedUploads(
                ownerUserId =
                    ownerUserId,
            )

        val pending =
            localRepository
                .listPendingUploads(
                    ownerUserId =
                        ownerUserId,

                    limit =
                        limit,
                )

        pending.forEach { image ->
            runCatching {
                upload(
                    ownerUserId =
                        ownerUserId,

                    localImageKey =
                        image.localImageKey,
                )
            }
        }
    }

    suspend fun removeImage(
        ownerUserId: String,
        localImageKey: String,
    ) {
        require(
            ownerUserId.isNotBlank(),
        ) {
            "OWNER_USER_ID_REQUIRED"
        }

        require(
            localImageKey.isNotBlank(),
        ) {
            "LOCAL_IMAGE_KEY_REQUIRED"
        }

        val local =
            localRepository
                .get(
                    localImageKey =
                        localImageKey,
                )
                ?: return

        require(
            local.ownerUserId ==
                    ownerUserId,
        ) {
            "NOTE_IMAGE_OWNER_MISMATCH"
        }

        /*
         * 先修改本地 authoritative media。
         */
        localRepository
            .delete(
                localImageKey =
                    local.localImageKey,
            )

        /*
         * 即使删除后已经没有任何图片，
         * Media Sync State 仍然保留 Pending。
         *
         * 这样后续可以明确发送：
         *
         * media = []
         */
        noteMediaLocalRepository
            .markPending(
                ownerUserId =
                    ownerUserId,

                noteLocalKey =
                    local.noteLocalKey,
            )

        local.localPath
            ?.takeIf {
                it.isNotBlank()
            }
            ?.let { localPath ->
                fileStore
                    .delete(
                        localPath =
                            localPath,
                    )
            }
    }

    suspend fun removeAllImagesForNote(
        ownerUserId: String,
        noteLocalKey: String,
    ) {
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

        val images =
            localRepository
                .listForNote(
                    ownerUserId =
                        ownerUserId,

                    noteLocalKey =
                        noteLocalKey,
                )

        localRepository
            .deleteForNote(
                ownerUserId =
                    ownerUserId,

                noteLocalKey =
                    noteLocalKey,
            )

        /*
         * 这是本地 Note 整体 cleanup，
         * 不是“用户把远端 Note 的所有图片删掉”。
         *
         * Media Sync State 一并清理。
         */
        noteMediaLocalRepository
            .deleteForNote(
                ownerUserId =
                    ownerUserId,

                noteLocalKey =
                    noteLocalKey,
            )

        images.forEach { image ->
            image.localPath
                ?.takeIf {
                    it.isNotBlank()
                }
                ?.let { localPath ->
                    fileStore
                        .delete(
                            localPath =
                                localPath,
                        )
                }
        }
    }

    suspend fun listImages(
        ownerUserId: String,
        noteLocalKey: String,
    ): List<LocalNoteImageEntity> {
        return localRepository
            .listForNote(
                ownerUserId =
                    ownerUserId,

                noteLocalKey =
                    noteLocalKey,
            )
    }

    private fun normalizeUploadError(
        error: Throwable,
    ): String {
        return error.message
            ?.takeIf {
                it.isNotBlank()
            }
            ?.take(
                500,
            )
            ?: error
                .javaClass
                .simpleName
    }

}