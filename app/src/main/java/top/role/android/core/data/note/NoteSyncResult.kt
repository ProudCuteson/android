package top.role.android.core.data.note

data class NoteSyncResult(
    val firstPullMergedCount: Int,
    val pushAttemptedCount: Int,
    val pushSucceededCount: Int,
    val pushFailedCount: Int,
    val finalPullMergedCount: Int,
)