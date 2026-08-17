package top.role.android.core.model.image

data class MobileImageUploadAuth(
    val scene: MobileImageScene,

    val provider: String,

    val accessMode: String,

    val uploadUrl: String,

    /**
     * Aliyun OSS POST policy 所需字段。
     *
     * Android 原样提交，不自行理解或构造 policy。
     */
    val formData: Map<String, String>,

    val objectKey: String,

    val providerPath: String?,

    val folderPath: String,

    val fileName: String,

    val bucket: String?,

    val region: String?,

    val endpoint: String?,

    val cdnUrl: String?,

    val originUrl: String?,

    /**
     * 绝对时间戳，毫秒。
     */
    val expiresAt: Long?,
)