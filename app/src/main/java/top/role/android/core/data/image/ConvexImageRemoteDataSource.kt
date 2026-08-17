package top.role.android.core.data.image

import top.role.android.core.convex.ConvexMobileClient
import top.role.android.core.data.image.remote.RemoteImageAsset
import top.role.android.core.data.image.remote.RemoteImagePreview
import top.role.android.core.data.image.remote.RemoteImageUploadAuth
import top.role.android.core.data.image.remote.toDomain
import top.role.android.core.model.image.MobileImageAsset
import top.role.android.core.model.image.MobileImagePreview
import top.role.android.core.model.image.MobileImageScene
import top.role.android.core.model.image.MobileImageUploadAuth
import top.role.android.core.model.image.MobileImageVariantKind

class ConvexImageRemoteDataSource(
    private val convexClient: ConvexMobileClient,
) : ImageRemoteDataSource {

    override suspend fun createUploadAuth(
        scene: MobileImageScene,
        mime: String,
        bytes: Long,
        fileName: String?,
    ): MobileImageUploadAuth {
        val args =
            buildMap<String, Any?> {
                put(
                    "scene",
                    scene.rawValue,
                )

                put(
                    "mime",
                    mime,
                )

                /*
                 * Convex v.number()
                 * transport 统一使用 Double。
                 */
                put(
                    "bytes",
                    bytes.toDouble(),
                )

                fileName
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?.let {
                        put(
                            "fileName",
                            it,
                        )
                    }
            }

        val remote =
            convexClient.action<RemoteImageUploadAuth>(
                name =
                    "mobile/android/image:createMobileImageUploadAuth",

                args =
                    args,
            )

        return remote.toDomain()
    }

    override suspend fun recordUpload(
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
    ): MobileImageAsset {
        require(
            objectKey.isNotBlank(),
        ) {
            "IMAGE_OBJECT_KEY_REQUIRED"
        }

        val args =
            buildMap<String, Any?> {
                put(
                    "scene",
                    scene.rawValue,
                )

                put(
                    "objectKey",
                    objectKey,
                )

                providerPath
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?.let {
                        put(
                            "providerPath",
                            it,
                        )
                    }

                folderPath
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?.let {
                        put(
                            "folderPath",
                            it,
                        )
                    }

                fileName
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?.let {
                        put(
                            "fileName",
                            it,
                        )
                    }

                put(
                    "mime",
                    mime,
                )

                /*
                 * Convex v.number()
                 * transport 统一 Double。
                 */
                width?.let {
                    put(
                        "width",
                        it.toDouble(),
                    )
                }

                height?.let {
                    put(
                        "height",
                        it.toDouble(),
                    )
                }

                bytes?.let {
                    put(
                        "bytes",
                        it.toDouble(),
                    )
                }

                bucket
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?.let {
                        put(
                            "bucket",
                            it,
                        )
                    }

                region
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?.let {
                        put(
                            "region",
                            it,
                        )
                    }

                endpoint
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?.let {
                        put(
                            "endpoint",
                            it,
                        )
                    }

                cdnUrl
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?.let {
                        put(
                            "cdnUrl",
                            it,
                        )
                    }

                originUrl
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?.let {
                        put(
                            "originUrl",
                            it,
                        )
                    }
            }

        val remote =
            convexClient.mutation<RemoteImageAsset>(
                name =
                    "mobile/android/image:recordMobileImageUpload",

                args =
                    args,
            )

        return remote.toDomain()
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

        val remote =
            convexClient.action<RemoteImagePreview>(
                name =
                    "mobile/android/image:getMobileImagePreviewUrl",

                args =
                    mapOf(
                        "imageId" to
                                imageId,

                        "kind" to
                                kind.rawValue,
                    ),
            )

        return remote.toDomain()
    }
}