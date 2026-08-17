package top.role.android.core.model.image

data class MobileImageAsset(
    val id: String,

    val scene: MobileImageScene,

    val provider: String,

    val accessMode: String,

    val objectKey: String,

    val providerPath: String?,

    val folderPath: String?,

    val mime: String,

    val width: Int?,

    val height: Int?,

    val bytes: Long?,
) {
    val isPrivate: Boolean
        get() =
            accessMode == "private"

    val isPublic: Boolean
        get() =
            accessMode == "public"
}