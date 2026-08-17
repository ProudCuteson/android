package top.role.android.core.model.image

enum class MobileImageVariantKind(
    val rawValue: String,
) {
    Avatar(
        rawValue = "avatar",
    ),

    Thumb(
        rawValue = "thumb",
    ),

    Card(
        rawValue = "card",
    ),

    Cover(
        rawValue = "cover",
    ),

    Hero(
        rawValue = "hero",
    ),

    Original(
        rawValue = "original",
    );

    companion object {
        fun fromRaw(
            value: String?,
        ): MobileImageVariantKind {
            return entries.firstOrNull {
                it.rawValue == value
            } ?: Original
        }
    }
}