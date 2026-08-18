package top.role.android.core.database.entity

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
    @PrimaryKey
    val localImageKey: String,

    val ownerUserId: String,

    val noteLocalKey: String,

    val imageId: String?,

    val localPath: String?,

    val mime: String?,

    val width: Int?,

    val height: Int?,

    val bytes: Long?,

    val uploadState: String,

    val uploadError: String?,

    val position: Int,

    val createdAt: Long,

    val updatedAt: Long,
)