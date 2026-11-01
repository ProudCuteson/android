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
        localOwnerUserId: String,
        noteLocalKey: String,
    ): Flow<List<LocalNoteImageEntity>> {
        require(
            localOwnerUserId.isNotBlank(),
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
                localOwnerUserId = localOwnerUserId,
                noteLocalKey = noteLocalKey,
            )
    }

    fun observeMonthImageCounts(
        localOwnerUserId: String,
        startDayKey: String,
        endDayKeyExclusive: String,
    ): Flow<Map<String, Int>> {
        require(localOwnerUserId.isNotBlank()) {
            "OWNER_USER_ID_REQUIRED"
        }

        return localRepository.observeMonthImageCounts(
            localOwnerUserId = localOwnerUserId,
            startDayKey = startDayKey,
            endDayKeyExclusive = endDayKeyExclusive,
        ).map { rows ->
            rows.associate { it.noteLocalKey to it.imageCount }
        }
    }

    suspend fun addImage(
        localOwnerUserId: String,
        noteLocalKey: String,
        sourceUri: Uri,
    ): LocalNoteImageEntity {
        require(
            localOwnerUserId.isNotBlank(),
        ) {
            "OWNER_USER_ID_REQUIRED"
        }

        require(
            noteLocalKey.isNotBlank(),
        ) {
            "NOTE_LOCAL_KEY_REQUIRED"
        }

        val sourceInfo = sourceInspector.inspect(sourceUri = sourceUri)

        val localPath = fileStore
            .importImage(
                sourceUri = sourceUri,
                mime = sourceInfo.mime,
            )

        val now = System.currentTimeMillis()

        val localImageKey = UUID.randomUUID().toString()

        val position = localRepository
            .nextPosition(
                localOwnerUserId = localOwnerUserId,
                noteLocalKey = noteLocalKey,
            )

        val localImage = LocalNoteImageEntity(
            localImageKey = localImageKey,
            localOwnerUserId = localOwnerUserId,
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
            localRepository.upsert(image = localImage)

            noteMediaLocalRepository
                .markPending(
                    localOwnerUserId = localOwnerUserId,
                    noteLocalKey = noteLocalKey,
                )

        } catch (
            error: Throwable,
        ) {
            fileStore.delete(localPath = localPath)
            throw error
        }

        runCatching {
            upload(
                localOwnerUserId = localOwnerUserId,
                localImageKey = localImageKey,
            )
        }

        return localRepository
            .get(localImageKey = localImageKey)
            ?: localImage
    }

    suspend fun upload(
        localOwnerUserId: String,
        localImageKey: String,
    ) {
        require(
            localOwnerUserId.isNotBlank(),
        ) {
            "OWNER_USER_ID_REQUIRED"
        }

        require(
            localImageKey.isNotBlank(),
        ) {
            "LOCAL_IMAGE_KEY_REQUIRED"
        }

        val local = localRepository
            .get(localImageKey = localImageKey)
            ?: return

        require(
            local.localOwnerUserId == localOwnerUserId,
        ) {
            "NOTE_IMAGE_OWNER_MISMATCH"
        }

        if (
            local.uploadState ==
            "uploaded" &&
            !local.imageId.isNullOrBlank()
        ) {
            return
        }

        val localPath = local.localPath
                ?.takeIf { it.isNotBlank() }

        if (localPath == null) {
            localRepository
                .markUploadFailed(
                    localImageKey = local.localImageKey,
                    errorMessage = "LOCAL_IMAGE_PATH_MISSING",
                )

            return
        }

        val localFile = File(localPath)

        if (
            !localFile.exists() ||
            !localFile.isFile
        ) {
            localRepository
                .markUploadFailed(
                    localImageKey = local.localImageKey,
                    errorMessage = "LOCAL_IMAGE_FILE_NOT_FOUND",
                )

            return
        }

        val bytes = local.bytes
        if (bytes == null || bytes <= 0L) {
            localRepository
                .markUploadFailed(
                    localImageKey = local.localImageKey,
                    errorMessage = "IMAGE_SIZE_INVALID",
                )

            return
        }

        val mime = local.mime
                ?.takeIf { it.isNotBlank() }

        if (mime == null) {
            localRepository
                .markUploadFailed(
                    localImageKey = local.localImageKey,
                    errorMessage = "IMAGE_MIME_MISSING",
                )

            return
        }

        localRepository
            .markUploading(
                localImageKey = local.localImageKey,
            )

        try {
            val sourceInfo = ImageSourceInfo(
                    mime = mime,
                    bytes = bytes,
                    fileName = localFile.name,
                    width = local.width,
                    height = local.height,
                )

            val asset = imageRepository
                .uploadPrivateImage(
                    sourceUri = localFile.toUri(),
                    sourceInfo = sourceInfo,
                    scene = MobileImageScene.Note,
                )

            localRepository
                .markUploaded(
                    localImageKey = local.localImageKey,
                    imageId = asset.id,
                )

            noteMediaLocalRepository
                .markPending(
                    localOwnerUserId = localOwnerUserId,
                    noteLocalKey = local.noteLocalKey,
                )

        } catch (
            error: Throwable,
        ) {
            localRepository
                .markUploadFailed(
                    localImageKey = local.localImageKey,
                    errorMessage = normalizeUploadError(error = error),
                )

            throw error
        }
    }

    suspend fun retryUpload(
        localOwnerUserId: String,
        localImageKey: String,
    ) {
        upload(
            localOwnerUserId = localOwnerUserId,
            localImageKey = localImageKey,
        )
    }

    suspend fun rebindNote(
        localOwnerUserId: String,
        oldNoteLocalKey: String,
        newNoteLocalKey: String,
        noteId: String? = null,
    ) {
        require(
            localOwnerUserId.isNotBlank(),
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
                    localOwnerUserId =
                        localOwnerUserId,

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
                localOwnerUserId =
                    localOwnerUserId,

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
                localOwnerUserId =
                    localOwnerUserId,

                oldNoteLocalKey =
                    oldNoteLocalKey,

                newNoteLocalKey =
                    newNoteLocalKey,

                noteId =
                    noteId,
            )
    }

    suspend fun uploadPending(
        localOwnerUserId: String,
        limit: Int = 20,
    ) {
        require(
            localOwnerUserId.isNotBlank(),
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
                localOwnerUserId = localOwnerUserId,
            )

        val pending =
            localRepository
                .listPendingUploads(
                    localOwnerUserId =
                        localOwnerUserId,

                    limit =
                        limit,
                )

        pending.forEach { image ->
            runCatching {
                upload(
                    localOwnerUserId =
                        localOwnerUserId,

                    localImageKey =
                        image.localImageKey,
                )
            }
        }
    }

    suspend fun removeImage(
        localOwnerUserId: String,
        localImageKey: String,
    ) {
        require(
            localOwnerUserId.isNotBlank(),
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
            local.localOwnerUserId ==
                    localOwnerUserId,
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
                localOwnerUserId =
                    localOwnerUserId,

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
        localOwnerUserId: String,
        noteLocalKey: String,
    ) {
        require(
            localOwnerUserId.isNotBlank(),
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
                    localOwnerUserId =
                        localOwnerUserId,

                    noteLocalKey =
                        noteLocalKey,
                )

        localRepository
            .deleteForNote(
                localOwnerUserId =
                    localOwnerUserId,

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
                localOwnerUserId =
                    localOwnerUserId,

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
        localOwnerUserId: String,
        noteLocalKey: String,
    ): List<LocalNoteImageEntity> {
        return localRepository
            .listForNote(
                localOwnerUserId =
                    localOwnerUserId,

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