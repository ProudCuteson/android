package top.role.android.core.model.note

enum class NoteRemoteStatus(
    val rawValue: String,
) {
    Active("active"),
    Archived("archived"),
    Deleted("deleted");

    companion object {
        fun fromRaw(
            value: String?,
        ): NoteRemoteStatus {
            return entries.firstOrNull {
                it.rawValue == value
            } ?: Active
        }
    }
}