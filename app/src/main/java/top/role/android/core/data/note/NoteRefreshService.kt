package top.role.android.core.data.note

import top.role.android.core.model.note.NotePullCheckpoint

data class NoteRefreshResult(
    val mergedCount: Int,
    val nextCheckpoint: NotePullCheckpoint,
)

class NoteRefreshService(
    private val noteRemoteDataSource:
    NoteRemoteDataSource,

    private val noteRemoteMergeService:
    NoteRemoteMergeService,
) {

    suspend fun refresh(
        ownerUserId: String,

        checkpoint:
        NotePullCheckpoint? =
            null,

        pageLimit:
        Int =
            100,
    ): Result<NoteRefreshResult> {
        return runCatching {
            var cursor:
                    String? =
                null

            var mergedCount =
                0

            var finalCheckpoint:
                    NotePullCheckpoint? =
                null

            while (true) {
                val response =
                    noteRemoteDataSource
                        .pullNoteChanges(
                            checkpoint =
                                checkpoint,

                            cursor =
                                cursor,

                            limit =
                                pageLimit,
                        )
                        .getOrThrow()

                /*
                 * 每页拿到后立即 merge Room。
                 *
                 * 即使后续某一页失败，
                 * 已经 merge 的数据也保留。
                 *
                 * 由于 checkpoint 尚未推进，
                 * 下一轮会从旧 checkpoint 重拉，
                 * merge 层负责幂等处理。
                 */
                for (
                snapshot in
                response.changes
                ) {
                    noteRemoteMergeService
                        .merge(
                            ownerUserId =
                                ownerUserId,

                            snapshot =
                                snapshot,
                        )

                    mergedCount += 1
                }

                if (
                    response.isDone
                ) {
                    finalCheckpoint =
                        response.nextCheckpoint
                            ?: throw IllegalStateException(
                                "Note pull completed without checkpoint"
                            )

                    break
                }

                val nextCursor =
                    response.continueCursor
                        ?: throw IllegalStateException(
                            "Note pull is not done but continueCursor is missing"
                        )

                if (
                    nextCursor ==
                    cursor
                ) {
                    throw IllegalStateException(
                        "Note pull cursor did not advance"
                    )
                }

                cursor =
                    nextCursor
            }

            NoteRefreshResult(
                mergedCount =
                    mergedCount,

                nextCheckpoint =
                    finalCheckpoint
                        ?: throw IllegalStateException(
                            "Note pull checkpoint missing"
                        ),
            )
        }
    }
}