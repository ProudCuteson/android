package top.role.android.core.model.note

sealed interface NoteSyncFailure {

    data class Retryable(
        val message: String,
    ) : NoteSyncFailure

    data class Conflict(
        val message: String,
    ) : NoteSyncFailure

    data class RemoteDeleted(
        val message: String,
    ) : NoteSyncFailure

    data class RemoteArchived(
        val message: String,
    ) : NoteSyncFailure
}