package top.role.android.core.asset

import kotlinx.serialization.Serializable

enum class AssetAccessMode(
    val value: String,
) {
    Public("public"),
    Private("private"),
}

enum class AssetUploadStatus {
    Idle,
    Preparing,
    Uploading,
    Finalizing,
    Done,
    Error,
}

/**
 * 后端 Prepare 返回结果。
 */
@Serializable
data class PreparedAssetUpload(
    val provider: String,
    val accessMode: String,
    val upload: AssetUploadInstruction,
    val target: AssetUploadTarget,
)

@Serializable
data class AssetUploadInstruction(
    val mode: String,
    val method: String,
    val uploadUrl: String,
    val headers: Map<String, String>,
    val expiresSec: Double,
)

@Serializable
data class AssetUploadTarget(
    val bucket: String,
    val region: String,
    val objectKey: String,
    val fileName: String,
    val scene: String,
)

/**
 * 调用方指定的上传选项。
 *
 * 后端仍负责最终参数校验。
 */
data class AssetUploadOptions(
    val accessMode: AssetAccessMode =
        AssetAccessMode.Private,

    val scene: String? = null,

    val allowedMimes: Set<String> = emptySet(),

    val maxBytes: Long? = null,
)

/**
 * 文件资产上传完成结果。
 */
data class AssetUploadResult(
    val assetId: String,

    val fileName: String,
    val mime: String,
    val bytes: Long,

    val accessMode: String,
    val scene: String,

    val objectKey: String,
)

/**
 * OSS 上传成功后，
 * 可以独立重试资产确认。
 */
data class PendingAssetFinalize(
    val prepared: PreparedAssetUpload,
    val bytes: Long,
)

/**
 * 上传过程中的 UI 状态。
 */
data class AssetUploadState(
    val status: AssetUploadStatus =
        AssetUploadStatus.Idle,

    val error: String? = null,

    val failedStage: AssetUploadStatus? = null,

    val result: AssetUploadResult? = null,

    val canRetryFinalize: Boolean = false,
) {
    val busy: Boolean
        get() = when (status) {
            AssetUploadStatus.Preparing,
            AssetUploadStatus.Uploading,
            AssetUploadStatus.Finalizing -> true

            else -> false
        }
}

fun AssetUploadStatus.displayLabel(): String {
    return when (this) {
        AssetUploadStatus.Idle -> "等待上传"
        AssetUploadStatus.Preparing -> "准备上传"
        AssetUploadStatus.Uploading -> "正在上传"
        AssetUploadStatus.Finalizing -> "正在确认文件"
        AssetUploadStatus.Done -> "上传完成"
        AssetUploadStatus.Error -> "上传失败"
    }
}