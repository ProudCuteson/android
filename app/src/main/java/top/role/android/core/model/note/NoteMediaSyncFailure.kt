package top.role.android.core.model.note

sealed interface NoteMediaSyncFailure {

    val message: String?

    data class Retryable(
        override val message: String?,
    ) : NoteMediaSyncFailure

    data class Conflict(
        override val message: String?,
    ) : NoteMediaSyncFailure

    data class RemoteDeleted(
        override val message: String?,
    ) : NoteMediaSyncFailure

    data class RemoteNotFound(
        override val message: String?,
    ) : NoteMediaSyncFailure
}