package top.role.android.core.asset

import top.role.android.core.convex.ConvexMobileClient

class AssetUploadRepository(
    private val convexClient: ConvexMobileClient,
) {

    /**
     * 获取文件资产上传授权。
     */
    suspend fun prepare(
        fileName: String,
        mime: String,
        bytes: Long,
        options: AssetUploadOptions,
    ): PreparedAssetUpload {

        val args = mutableMapOf<String, Any?>(
            "fileName" to fileName,
            "mime" to mime,
            "bytes" to bytes.toDouble(),
            "accessMode" to options.accessMode.value,
        )

        options.scene
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let {
                args["scene"] = it
            }

        return convexClient.action<PreparedAssetUpload>(
            name = "mobile/native/asset/upload:prepareAssetUpload",
            args = args,
        )
    }

    /**
     * 确认 OSS 上传并完成资产登记。
     */
    suspend fun finalize(
        pending: PendingAssetFinalize,
    ): AssetUploadResult {

        val prepared = pending.prepared
        val target = prepared.target

        val mime = prepared.upload.headers
            .entries
            .firstOrNull {
                it.key.equals(
                    "Content-Type",
                    ignoreCase = true,
                )
            }
            ?.value
            ?: error("Missing upload Content-Type")

        val assetId = convexClient.action<String>(
            name = "mobile/native/asset/upload:finalizeAssetUpload",
            args = mapOf(
                "accessMode" to prepared.accessMode,

                "scene" to target.scene,
                "objectKey" to target.objectKey,

                "bucket" to target.bucket,
                "region" to target.region,

                "fileName" to target.fileName,
                "mime" to mime,
                "bytes" to pending.bytes.toDouble(),
            ),
        )

        return AssetUploadResult(
            assetId = assetId,

            fileName = target.fileName,
            mime = mime,
            bytes = pending.bytes,

            accessMode = prepared.accessMode,
            scene = target.scene,

            objectKey = target.objectKey,
        )
    }
}