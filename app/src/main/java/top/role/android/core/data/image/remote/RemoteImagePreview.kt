package top.role.android.core.data.image.remote

import kotlinx.serialization.Serializable

@Serializable
data class RemoteImagePreview(
    val imageId: String,

    val kind: String,

    val url: String?,

    val expiresSec: Double,
)