package top.role.android.core.model.note

enum class NoteSyncState(
    val rawValue: String,
) {
    Synced("synced"),
    Pending("pending"),
    Syncing("syncing"),
    Waiting("waiting"),
    Conflict("conflict");

    companion object {
        fun fromRaw(
            value: String?,
        ): NoteSyncState {
            return entries.firstOrNull {
                it.rawValue == value
            } ?: Pending
        }
    }
}