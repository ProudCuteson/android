package top.role.android.core.data.image.remote

import kotlinx.serialization.Serializable

@Serializable
data class RemoteImageUploadAuth(
    val scene: String,

    val provider: String,

    val accessMode: String,

    val uploadUrl: String,

    val formData: Map<String, String>,

    val objectKey: String,

    val providerPath: String? = null,

    val folderPath: String? = null,

    val fileName: String,

    val bucket: String? = null,

    val region: String? = null,

    val endpoint: String? = null,

    val cdnUrl: String? = null,

    val originUrl: String? = null,

    val expiresAt: Double? = null,
)