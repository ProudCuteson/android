package top.role.android.core.data.image

import android.net.Uri
import top.role.android.core.model.image.MobileImageAsset
import top.role.android.core.model.image.MobileImagePreview
import top.role.android.core.model.image.MobileImageScene
import top.role.android.core.model.image.MobileImageVariantKind

class ImageRepositoryImpl(
    private val remoteDataSource: ImageRemoteDataSource,
    private val objectUploader: ImageObjectUploader,
    private val sourceInspector: ImageSourceInspector,
) : ImageRepository {

    override suspend fun uploadPrivateImage(
        sourceUri: Uri,
        scene: MobileImageScene,
    ): MobileImageAsset {
        val sourceInfo =
            sourceInspector.inspect(
                sourceUri = sourceUri,
            )

        return uploadPrivateImage(
            sourceUri = sourceUri,
            sourceInfo = sourceInfo,
            scene = scene,
        )
    }

    override suspend fun uploadPrivateImage(
        sourceUri: Uri,
        sourceInfo: ImageSourceInfo,
        scene: MobileImageScene,
    ): MobileImageAsset {
        require(
            sourceInfo.bytes > 0L,
        ) {
            "IMAGE_SIZE_INVALID"
        }

        val auth =
            remoteDataSource
                .createUploadAuth(
                    scene = scene,

                    mime =
                        sourceInfo.mime,

                    bytes =
                        sourceInfo.bytes,

                    fileName =
                        sourceInfo.fileName,
                )

        check(
            auth.accessMode ==
                    "private",
        ) {
            "IMAGE_UPLOAD_AUTH_NOT_PRIVATE"
        }

        objectUploader.upload(
            sourceUri =
                sourceUri,

            mime =
                sourceInfo.mime,

            bytes =
                sourceInfo.bytes,

            auth =
                auth,
        )

        return remoteDataSource
            .recordUpload(
                scene =
                    scene,

                objectKey =
                    auth.objectKey,

                providerPath =
                    auth.providerPath,

                folderPath =
                    auth.folderPath,

                fileName =
                    auth.fileName,

                mime =
                    sourceInfo.mime,

                width =
                    sourceInfo.width,

                height =
                    sourceInfo.height,

                bytes =
                    sourceInfo.bytes,

                bucket =
                    auth.bucket,

                region =
                    auth.region,

                endpoint =
                    auth.endpoint,

                cdnUrl =
                    auth.cdnUrl,

                originUrl =
                    auth.originUrl,
            )
    }

    override suspend fun getPreviewUrl(
        imageId: String,
        kind: MobileImageVariantKind,
    ): MobileImagePreview {
        require(
            imageId.isNotBlank(),
        ) {
            "IMAGE_ID_REQUIRED"
        }

        return remoteDataSource
            .getPreviewUrl(
                imageId =
                    imageId,

                kind =
                    kind,
            )
    }

}