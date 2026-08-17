package top.role.android.core.model.image

enum class MobileImageScene(
    val rawValue: String,
) {
    Avatar(
        rawValue = "avatar",
    ),

    Note(
        rawValue = "note",
    ),

    Conversation(
        rawValue = "conversation",
    );

    companion object {
        fun fromRaw(
            value: String?,
        ): MobileImageScene? {
            return entries.firstOrNull {
                it.rawValue == value
            }
        }
    }
}