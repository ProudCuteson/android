package top.role.android.core.data.note

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import top.role.android.core.data.image.NoteImageService
import top.role.android.core.data.image.note.NoteMediaPushService
import top.role.android.core.data.image.note.NoteMediaRefreshService
import top.role.android.core.storage.NoteSyncCheckpointStorage

class NoteSyncCoordinator(
    private val noteRefreshService: NoteRefreshService,
    private val notePushService: NotePushService,
    private val noteLocalRepository: NoteLocalRepository,
    private val noteImageService: NoteImageService,
    private val noteMediaRefreshService: NoteMediaRefreshService,
    private val noteMediaPushService: NoteMediaPushService,
    private val checkpointStorage: NoteSyncCheckpointStorage,
) {

    private val syncMutex = Mutex()

    suspend fun sync(
        localOwnerUserId: String,
        pageLimit: Int = 100,
    ): Result<NoteSyncResult> {
        return syncMutex.withLock {
            runCatching {
                require(
                    localOwnerUserId.isNotBlank(),
                ) {
                    "OWNER_USER_ID_REQUIRED"
                }

                require(
                    pageLimit > 0,
                ) {
                    "NOTE_SYNC_PAGE_LIMIT_INVALID"
                }

                /*
                 * -----------------------------------------
                 * Phase 1
                 * Content Pull
                 * -----------------------------------------
                 *
                 * Push 前先获取最新远端 Content 状态。
                 *
                 * Content remoteVersion
                 * 与 Media remoteVersion 完全独立。
                 */
                val firstPull =
                    refreshAndPersistCheckpoint(
                        localOwnerUserId = localOwnerUserId,
                        pageLimit = pageLimit,
                    )
                        .getOrThrow()


                /*
                 * -----------------------------------------
                 * Phase 2
                 * Content Push
                 * -----------------------------------------
                 */
                val notesToPush =
                    noteLocalRepository
                        .findNotesNeedingSync(
                            localOwnerUserId = localOwnerUserId,
                        )

                var pushSucceededCount = 0
                var pushFailedCount = 0

                /*
                 * 第一版继续顺序 Push。
                 *
                 * 不并行：
                 *
                 * - frozen content snapshot 更容易保证
                 * - lifecycle 顺序更确定
                 * - DayOnly identity 更安全
                 * - 单条失败不影响其他 Note
                 */
                for (note in notesToPush) {
                    val result = notePushService
                        .pushOne(
                            localOwnerUserId = localOwnerUserId,
                            localKey = note.localKey,
                        )

                    if (
                        result.isSuccess
                    ) {
                        pushSucceededCount += 1

                        val syncedNote = noteLocalRepository
                            .findByClientKey(
                                localOwnerUserId = localOwnerUserId,
                                clientKey = note.clientKey,
                            )

                        val noteId = syncedNote
                                ?.noteId
                                ?.takeIf { it.isNotBlank() }

                        if ( syncedNote != null && noteId != null ) {

                            noteImageService
                                .rebindNote(
                                    localOwnerUserId = localOwnerUserId,
                                    oldNoteLocalKey = note.localKey,
                                    newNoteLocalKey = syncedNote.localKey,
                                    noteId = noteId,
                                )
                        }

                    } else {
                       pushFailedCount += 1
                    }
                }


                /*
                 * -----------------------------------------
                 * Phase 3
                 * Image Asset Upload
                 * -----------------------------------------
                 *
                 * Media Relation Push 之前，
                 * 尽量先让本地图片获得正式 imageId。
                 *
                 * NoteImageService.uploadPending()
                 * 内部已经逐张 runCatching：
                 *
                 * 单张图片失败
                 * 不阻止其他图片继续上传。
                 *
                 * 这里再次 runCatching 是为了：
                 *
                 * 即使 asset queue 本身发生异常，
                 * 也不要让已经完成的 Content Sync
                 * 整轮变成失败。
                 */
                runCatching {
                    noteImageService
                        .uploadPending(
                            localOwnerUserId = localOwnerUserId,
                        )
                }


                /*
                 * -----------------------------------------
                 * Phase 4
                 * Media Pull / Refresh
                 * -----------------------------------------
                 *
                 * 不能只处理 Content needing-sync Notes。
                 *
                 * 一个 Content 完全 Synced 的 Note，
                 * 仍然可能在其他设备新增 / 删除 / 排序图片。
                 *
                 * 所以这里处理所有已有 remote noteId 的 Note。
                 */
                val remoteBackedNotes = noteLocalRepository
                    .findRemoteBackedNotes(
                        localOwnerUserId = localOwnerUserId,
                    )

                for ( note in remoteBackedNotes ) {
                    val noteId = note.noteId
                            ?.takeIf { it.isNotBlank() }
                            ?: continue

                    /*
                     * Media 是独立同步域。
                     *
                     * 单条 Media Pull：
                     *
                     * - 网络失败
                     * - NOTE_DELETED
                     * - NOTE_NOT_FOUND
                     * - hydration 失败
                     *
                     * 都不应该让整个 Content Sync
                     * 回滚为失败。
                     *
                     * NoteMediaRefreshService 自己负责：
                     *
                     * Synced
                     * → authoritative reconcile
                     *
                     * Pending
                     * → preserve local / baseline / conflict
                     *
                     * Waiting / Syncing
                     * → skip
                     *
                     * Conflict
                     * → refresh remote conflict snapshot
                     */
                    runCatching {
                        noteMediaRefreshService
                            .refreshOne(
                                localOwnerUserId = localOwnerUserId,
                                noteLocalKey = note.localKey,
                                noteId = noteId,
                            )
                    }
                }


                /*
                 * -----------------------------------------
                 * Phase 5
                 * Media Push
                 * -----------------------------------------
                 *
                 * NoteMediaPushService 自己负责：
                 *
                 * - reset interrupted Syncing → Waiting
                 * - find Pending / Waiting
                 * - frozen snapshot
                 * - requestId idempotency
                 * - optimistic mediaVersion
                 * - Waiting / Conflict
                 *
                 * 如果本地仍存在尚未完成 Asset Upload 的图片：
                 *
                 * → WaitingForImageAssets
                 * → Media 保持 Pending
                 *
                 * 不会发送一个不完整的 remote media 集合。
                 */
                runCatching {
                    noteMediaPushService
                        .pushPending(
                            localOwnerUserId = localOwnerUserId,
                        )
                }


                /*
                 * -----------------------------------------
                 * Phase 6
                 * Final Content Pull
                 * -----------------------------------------
                 *
                 * 用来：
                 *
                 * - 拉回刚才自己 Content Push 产生的 change
                 * - 拉回同步期间其他端产生的 Content change
                 * - 推进 Content pull checkpoint
                 *
                 * 注意：
                 *
                 * 这里只是 Content Pull。
                 *
                 * Media 已经有自己的：
                 *
                 * NoteMediaRefreshService
                 *
                 * 不再把 Media 伪装成 Content Pull 的一部分。
                 */
                val finalPull =
                    refreshAndPersistCheckpoint(
                        localOwnerUserId = localOwnerUserId,
                        pageLimit = pageLimit,
                    )
                        .getOrThrow()


                /*
                 * 第一版保持现有 NoteSyncResult，
                 * 不因为引入 Media Sync
                 * 立即改变所有上层调用。
                 *
                 * Media observability 后续需要时
                 * 再单独扩展：
                 *
                 * - media refresh count
                 * - media push success
                 * - waiting
                 * - conflict
                 * - image asset failure
                 */
                NoteSyncResult(
                    firstPullMergedCount = firstPull.mergedCount,
                    pushAttemptedCount = notesToPush.size,
                    pushSucceededCount = pushSucceededCount,
                    pushFailedCount = pushFailedCount,
                    finalPullMergedCount = finalPull.mergedCount,
                )
            }
        }
    }


    /**
     * 纯 Content Pull。
     *
     * 注意：
     *
     * 当前 refresh() 保持原语义，
     * 只负责 Note Content Refresh。
     *
     * 不偷偷加入：
     *
     * - Image Asset upload
     * - Media Refresh
     * - Media Push
     *
     * 否则 refresh() 会从“纯 Pull”
     * 变成一个行为很重的方法。
     *
     * 后续如果 UI 需要：
     *
     * "刷新 Note + Media"
     *
     * 可以单独设计 refreshAll()，
     * 不修改这个方法的既有语义。
     */
    suspend fun refresh(
        localOwnerUserId: String,
        pageLimit: Int = 100,
    ): Result<NoteRefreshResult> {
        return syncMutex.withLock {
            refreshAndPersistCheckpoint(
                localOwnerUserId = localOwnerUserId,
                pageLimit = pageLimit,
            )
        }
    }


    /**
     * Content Pull + checkpoint commit。
     *
     * 注意：
     *
     * 这个 private 方法不能再次 withLock。
     *
     * sync() / refresh()
     * 外层已经持有同一个 Mutex。
     *
     * checkpoint 只有在完整 Content Pull 成功后
     * 才允许推进。
     */
    private suspend fun refreshAndPersistCheckpoint(
        localOwnerUserId: String,
        pageLimit: Int,
    ): Result<NoteRefreshResult> {
        return runCatching {
            val checkpoint = checkpointStorage
                .getPullCheckpoint(
                    localOwnerUserId =localOwnerUserId,
                )

            val result = noteRefreshService
                .refresh(
                    localOwnerUserId = localOwnerUserId,
                    checkpoint = checkpoint,
                    pageLimit = pageLimit,
                )
                .getOrThrow()

            /*
             * 只有完整 Content Pull 成功
             * 才提交新的 checkpoint。
             */
            checkpointStorage
                .savePullCheckpoint(
                    localOwnerUserId = localOwnerUserId,
                    checkpoint = result.nextCheckpoint,
                )

            result
        }
    }

}