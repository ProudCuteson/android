package top.role.android.core.data.note

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import top.role.android.core.storage.NoteSyncCheckpointStorage

class NoteSyncCoordinator(
    private val noteRefreshService:
    NoteRefreshService,

    private val notePushService:
    NotePushService,

    private val noteLocalRepository:
    NoteLocalRepository,

    private val checkpointStorage:
    NoteSyncCheckpointStorage,
) {

    /*
     * 同一时刻只允许一轮 Note remote sync。
     *
     * 以后无论触发来源是：
     *
     * - App 启动
     * - App 回前台
     * - Editor 退出
     * - 手动刷新
     *
     * 都在这里串行化。
     */
    private val syncMutex =
        Mutex()

    /**
     * 执行一轮完整 Note 双向同步。
     *
     * 顺序：
     *
     * 1. Pull Remote -> Room
     * 2. Push Room Pending / Waiting -> Remote
     * 3. 再 Pull 一次 Remote -> Room
     *
     * UI 始终只观察 Room。
     */
    suspend fun sync(
        ownerUserId: String,
        pageLimit: Int = 100,
    ): Result<NoteSyncResult> {
        return syncMutex.withLock {
            runCatching {
                /*
                 * -----------------------------------------
                 * Phase 1
                 * Pull Remote -> Room
                 * -----------------------------------------
                 *
                 * Push 前先获取最新远端状态，
                 * 尽量让 Room 中的 remoteVersion
                 * 保持最新。
                 */
                val firstPull =
                    refreshAndPersistCheckpoint(
                        ownerUserId =
                            ownerUserId,

                        pageLimit =
                            pageLimit,
                    )
                        .getOrThrow()

                /*
                 * -----------------------------------------
                 * Phase 2
                 * Push Pending / Waiting
                 * -----------------------------------------
                 */
                val notesToPush =
                    noteLocalRepository
                        .findNotesNeedingSync(
                            ownerUserId =
                                ownerUserId,
                        )

                var pushSucceededCount =
                    0

                var pushFailedCount =
                    0

                /*
                 * 第一版顺序 Push。
                 *
                 * 暂时不并行，原因：
                 *
                 * - 状态机更确定
                 * - pending snapshot 更容易保证
                 * - DayOnly identity 更安全
                 * - 单条失败不影响其他 Note
                 */
                for (
                note in
                notesToPush
                ) {
                    val result =
                        notePushService
                            .pushOne(
                                ownerUserId =
                                    ownerUserId,

                                localKey =
                                    note.localKey,
                            )

                    if (
                        result.isSuccess
                    ) {
                        pushSucceededCount +=
                            1
                    } else {
                        /*
                         * 单条失败不终止整轮 Push。
                         *
                         * applyPushFailure() 应负责：
                         *
                         * Syncing
                         * -> Waiting
                         *
                         * 并保留 pendingSnapshotJson，
                         * 方便下一次继续使用同一个 requestId。
                         */
                        pushFailedCount +=
                            1
                    }
                }

                /*
                 * -----------------------------------------
                 * Phase 3
                 * Final Pull
                 * -----------------------------------------
                 *
                 * 用来：
                 *
                 * - 拉回刚才自己产生的远端 change
                 * - 拉回同步期间其他端产生的 change
                 * - 推进 pull checkpoint
                 */
                val finalPull =
                    refreshAndPersistCheckpoint(
                        ownerUserId =
                            ownerUserId,

                        pageLimit =
                            pageLimit,
                    )
                        .getOrThrow()

                NoteSyncResult(
                    firstPullMergedCount =
                        firstPull.mergedCount,

                    pushAttemptedCount =
                        notesToPush.size,

                    pushSucceededCount =
                        pushSucceededCount,

                    pushFailedCount =
                        pushFailedCount,

                    finalPullMergedCount =
                        finalPull.mergedCount,
                )
            }
        }
    }

    /**
     * 纯 Pull。
     *
     * 例如后面可能用于：
     *
     * - 手动刷新
     * - repair
     * - 只需要远端 hydration 的场景
     *
     * 它和完整 sync 使用同一个 Mutex，
     * 避免与 Pull -> Push -> Pull 并发。
     */
    suspend fun refresh(
        ownerUserId: String,
        pageLimit: Int = 100,
    ): Result<NoteRefreshResult> {
        return syncMutex.withLock {
            refreshAndPersistCheckpoint(
                ownerUserId =
                    ownerUserId,

                pageLimit =
                    pageLimit,
            )
        }
    }

    /**
     * Pull + checkpoint commit。
     *
     * 注意：
     *
     * 这个 private 方法本身不要再 withLock。
     *
     * 因为 sync() 已经持有 syncMutex，
     * 如果这里再次锁同一个 Mutex，
     * 会造成嵌套等待。
     *
     * checkpoint 只有在完整 Pull 成功后
     * 才允许推进。
     */
    private suspend fun refreshAndPersistCheckpoint(
        ownerUserId: String,
        pageLimit: Int,
    ): Result<NoteRefreshResult> {
        return runCatching {
            val checkpoint =
                checkpointStorage
                    .getPullCheckpoint(
                        ownerUserId =
                            ownerUserId,
                    )

            val result =
                noteRefreshService
                    .refresh(
                        ownerUserId =
                            ownerUserId,

                        checkpoint =
                            checkpoint,

                        pageLimit =
                            pageLimit,
                    )
                    .getOrThrow()

            /*
             * 只有完整 Pull 成功后
             * 才提交新的 checkpoint。
             */
            checkpointStorage
                .savePullCheckpoint(
                    ownerUserId =
                        ownerUserId,

                    checkpoint =
                        result.nextCheckpoint,
                )

            result
        }
    }

    /**
     * Repair / 开发调试使用。
     *
     * 正常 logout 不调用。
     *
     * 也需要与 sync 串行，
     * 防止正在 pull 时 checkpoint 被清掉。
     */
    suspend fun resetPullCheckpoint(
        ownerUserId: String,
    ) {
        syncMutex.withLock {
            checkpointStorage
                .clearPullCheckpoint(
                    ownerUserId =
                        ownerUserId,
                )
        }
    }
}