package top.role.android.core.database.note

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "local_note_images",
    indices = [
        Index(
            value = [
                "ownerUserId",
                "noteLocalKey",
            ],
        ),
        Index(
            value = [
                "ownerUserId",
                "imageId",
            ],
        ),
        Index(
            value = [
                "uploadState",
            ],
        ),
    ],
)

data class LocalNoteImageEntity(
    /**
     * 本地图片附件唯一标识。
     *
     * 与远端 imageId 无关，
     * 在选择图片并复制到 App 私有目录时立即生成。
     */
    @PrimaryKey
    val localImageKey: String,

    /**
     * 本地账号作用域。
     *
     * 与 LocalNoteEntity.ownerUserId 保持一致。
     */
    val ownerUserId: String,

    /**
     * 对应 LocalNoteEntity.localKey。
     *
     * Note 尚未同步到远端时，
     * 仍然可以通过 localKey 建立附件关系。
     */
    val noteLocalKey: String,

    /**
     * images._id。
     *
     * OSS 上传 + record 成功后回填。
     * 本地待上传阶段为 null。
     */
    val imageId: String?,

    /**
     * App 私有目录中的绝对文件路径。
     *
     * 不存 Photo Picker 的 content:// Uri。
     */
    val localPath: String,

    /**
     * MIME type，例如：
     *
     * image/jpeg
     * image/png
     * image/webp
     */
    val mime: String,

    /**
     * 本地图片像素尺寸。
     */
    val width: Int?,

    val height: Int?,

    /**
     * 文件字节数。
     */
    val bytes: Long,

    /**
     * local
     * uploading
     * uploaded
     * failed
     */
    val uploadState: String,

    /**
     * 最近一次上传错误。
     *
     * 成功后清空。
     */
    val uploadError: String?,

    /**
     * 当前 Note 内的显示顺序。
     */
    val position: Int,

    val createdAt: Long,

    val updatedAt: Long,
)