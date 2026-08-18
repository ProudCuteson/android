package top.role.android.core.model.note

enum class NoteMediaSyncState(
    val rawValue: String,
) {
    Synced(
        rawValue = "synced",
    ),

    Pending(
        rawValue = "pending",
    ),

    Syncing(
        rawValue = "syncing",
    ),

    Waiting(
        rawValue = "waiting",
    ),

    Conflict(
        rawValue = "conflict",
    );

    companion object {

        fun fromRawValue(
            value: String?,
        ): NoteMediaSyncState {
            return entries
                .firstOrNull {
                    it.rawValue == value
                }
                ?: Pending
        }
    }
}