package top.role.android.core.data.image

import top.role.android.core.model.image.MobileImageAsset
import top.role.android.core.model.image.MobileImagePreview
import top.role.android.core.model.image.MobileImageScene
import top.role.android.core.model.image.MobileImageUploadAuth
import top.role.android.core.model.image.MobileImageVariantKind

interface ImageRemoteDataSource {

    suspend fun createUploadAuth(
        scene: MobileImageScene,
        mime: String,
        bytes: Long,
        fileName: String?,
    ): MobileImageUploadAuth

    suspend fun recordUpload(
        scene: MobileImageScene,

        objectKey: String,

        providerPath: String?,

        folderPath: String?,

        fileName: String?,

        mime: String,

        width: Int?,

        height: Int?,

        bytes: Long?,

        bucket: String?,

        region: String?,

        endpoint: String?,

        cdnUrl: String?,

        originUrl: String?,
    ): MobileImageAsset

    suspend fun getPreviewUrl(
        imageId: String,

        kind: MobileImageVariantKind =
            MobileImageVariantKind.Original,
    ): MobileImagePreview
}