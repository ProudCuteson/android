package top.role.android.core.model.image

enum class NoteImageUploadState(
    val rawValue: String,
) {
    Local(
        rawValue = "local",
    ),

    Uploading(
        rawValue = "uploading",
    ),

    Uploaded(
        rawValue = "uploaded",
    ),

    Failed(
        rawValue = "failed",
    );

    companion object {
        fun fromRaw(
            value: String?,
        ): NoteImageUploadState {
            return entries.firstOrNull {
                it.rawValue == value
            } ?: Local
        }
    }
}