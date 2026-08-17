package top.role.android.core.data.image

data class ImageSourceInfo(
    val mime: String,
    val bytes: Long,
    val fileName: String?,
    val width: Int?,
    val height: Int?,
)