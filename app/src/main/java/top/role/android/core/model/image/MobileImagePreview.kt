package top.role.android.core.model.image

data class MobileImagePreview(
    val imageId: String,

    val kind: MobileImageVariantKind,

    val url: String,

    val expiresSec: Int,
)