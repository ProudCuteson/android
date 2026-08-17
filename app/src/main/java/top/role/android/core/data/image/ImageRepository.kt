package top.role.android.core.data.image

import android.net.Uri
import top.role.android.core.model.image.MobileImageAsset
import top.role.android.core.model.image.MobileImagePreview
import top.role.android.core.model.image.MobileImageScene
import top.role.android.core.model.image.MobileImageVariantKind

interface ImageRepository {

    suspend fun uploadPrivateImage(
        sourceUri: Uri,
        scene: MobileImageScene,
    ): MobileImageAsset

    suspend fun uploadPrivateImage(
        sourceUri: Uri,
        sourceInfo: ImageSourceInfo,
        scene: MobileImageScene,
    ): MobileImageAsset

    suspend fun getPreviewUrl(
        imageId: String,

        kind: MobileImageVariantKind =
            MobileImageVariantKind.Original,
    ): MobileImagePreview
}