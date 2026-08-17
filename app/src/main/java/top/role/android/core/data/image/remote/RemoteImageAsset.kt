package top.role.android.core.data.image.remote

import kotlinx.serialization.Serializable

@Serializable
data class RemoteImageAsset(
    val id: String,

    val scene: String,

    val provider: String,

    val accessMode: String,

    val objectKey: String,

    val providerPath: String?,

    val folderPath: String?,

    val mime: String?,

    val width: Double?,

    val height: Double?,

    val bytes: Double?,
)