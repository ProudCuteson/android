package top.role.android.core.data.image.remote

import top.role.android.core.model.image.MobileImageAsset
import top.role.android.core.model.image.MobileImagePreview
import top.role.android.core.model.image.MobileImageScene
import top.role.android.core.model.image.MobileImageUploadAuth
import top.role.android.core.model.image.MobileImageVariantKind

fun RemoteImageAsset.toDomain(): MobileImageAsset {
    return MobileImageAsset(
        id = id,

        scene =
            MobileImageScene.fromRaw(
                scene,
            ) ?: error(
                "UNSUPPORTED_IMAGE_SCENE: $scene",
            ),

        provider =
            provider,

        accessMode =
            accessMode,

        objectKey =
            objectKey,

        providerPath =
            providerPath,

        folderPath =
            folderPath,

        mime =
            mime
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: "image/jpeg",

        width =
            width?.toInt(),

        height =
            height?.toInt(),

        bytes =
            bytes?.toLong(),
    )
}

fun RemoteImageUploadAuth.toDomain(): MobileImageUploadAuth {
    return MobileImageUploadAuth(
        scene =
            MobileImageScene.fromRaw(
                scene,
            ) ?: error(
                "UNSUPPORTED_IMAGE_SCENE: $scene",
            ),

        provider =
            provider,

        accessMode =
            accessMode,

        uploadUrl =
            uploadUrl,

        formData =
            formData,

        objectKey =
            objectKey,

        providerPath =
            providerPath,

        folderPath =
            folderPath
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: error(
                    "IMAGE_UPLOAD_FOLDER_PATH_MISSING",
                ),

        fileName =
            fileName,

        bucket =
            bucket,

        region =
            region,

        endpoint =
            endpoint,

        cdnUrl =
            cdnUrl,

        originUrl =
            originUrl,

        expiresAt =
            expiresAt?.toLong(),
    )
}

fun RemoteImagePreview.toDomain(): MobileImagePreview {
    val previewUrl =
        url
            ?.takeIf {
                it.isNotBlank()
            }
            ?: error(
                "IMAGE_PREVIEW_URL_MISSING",
            )

    return MobileImagePreview(
        imageId =
            imageId,

        kind =
            MobileImageVariantKind.fromRaw(
                kind,
            ),

        url =
            previewUrl,

        expiresSec =
            expiresSec.toInt(),
    )
}