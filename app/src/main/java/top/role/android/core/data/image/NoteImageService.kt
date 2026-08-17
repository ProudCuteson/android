package top.role.android.core.data.image

import android.net.Uri
import androidx.core.net.toUri
import kotlinx.coroutines.flow.Flow
import top.role.android.core.database.entity.LocalNoteImageEntity
import top.role.android.core.model.image.MobileImageScene
import java.io.File
import java.util.UUID

class NoteImageService(
    private val localRepository: NoteImageLocalRepository,

    private val fileStore: LocalImageFileStore,

    private val sourceInspector: ImageSourceInspector,

    private val imageRepository: ImageRepository,
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

        val now =
            System.currentTimeMillis()

        val localImageKey =
            UUID.randomUUID()
                .toString()

        val position =
            localRepository
                .nextPosition(
                    ownerUserId =
                        ownerUserId,

                    noteLocalKey =
                        noteLocalKey,
                )

        val localImage =
            LocalNoteImageEntity(
                localImageKey =
                    localImageKey,

                ownerUserId =
                    ownerUserId,

                noteLocalKey =
                    noteLocalKey,

                imageId =
                    null,

                localPath =
                    localPath,

                mime =
                    sourceInfo.mime,

                width =
                    sourceInfo.width,

                height =
                    sourceInfo.height,

                bytes =
                    sourceInfo.bytes,

                uploadState =
                    "local",

                uploadError =
                    null,

                position =
                    position,

                createdAt =
                    now,

                updatedAt =
                    now,
            )

        try {
            localRepository
                .upsert(
                    image =
                        localImage,
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
         * 已经完成上传并登记远端 imageId，
         * 不重复上传。
         */
        if (
            local.uploadState ==
            "uploaded" &&
            !local.imageId.isNullOrBlank()
        ) {
            return
        }

        val localFile =
            File(
                local.localPath,
            )

        /*
         * Room 记录还在，
         * 但文件已经不存在。
         *
         * 不删除记录，
         * 保留 failed 状态让 UI 可以显示错误。
         */
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

        if (
            local.bytes <= 0L
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

        localRepository
            .markUploading(
                localImageKey =
                    local.localImageKey,
            )

        try {
            /*
             * metadata 已经在用户选图时 inspect 并存入 Room。
             *
             * 延迟上传时直接复用，
             * 不重新 inspect 私有本地文件。
             */
            val sourceInfo =
                ImageSourceInfo(
                    mime =
                        local.mime,

                    bytes =
                        local.bytes,

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
            return
        }

        localRepository
            .rebindNoteLocalKey(
                ownerUserId =
                    ownerUserId,

                oldNoteLocalKey =
                    oldNoteLocalKey,

                newNoteLocalKey =
                    newNoteLocalKey,
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

    /**
     * 删除单张 Note 图片附件。
     *
     * 当前阶段删除：
     *
     * 1. LocalNoteImageEntity
     * 2. App 私有本地文件
     *
     * 如果已经存在远端 imageId，
     * 第一阶段暂不主动删除远端 Image Asset。
     */
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
         * 先删除数据库关系。
         *
         * 即使磁盘文件删除失败，
         * UI 也不会继续把它当作 Note 附件。
         */
        localRepository
            .delete(
                localImageKey =
                    local.localImageKey,
            )

        fileStore
            .delete(
                localPath =
                    local.localPath,
            )
    }

    /**
     * 删除某条 Note 的全部本地图片附件。
     *
     * 后面处理：
     * - 新建空 Note 被自动删除
     * - Note 本地彻底清理
     *
     * 时可以直接使用。
     */
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

        /*
         * 先删除数据库关系，
         * 再清理实际文件。
         */
        localRepository
            .deleteForNote(
                ownerUserId =
                    ownerUserId,

                noteLocalKey =
                    noteLocalKey,
            )

        images.forEach { image ->
            fileStore
                .delete(
                    localPath =
                        image.localPath,
                )
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