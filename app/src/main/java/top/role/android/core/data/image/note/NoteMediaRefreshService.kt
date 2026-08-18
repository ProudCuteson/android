package top.role.android.core.data.image.note

import top.role.android.core.data.image.LocalImageFileStore
import top.role.android.core.data.image.NoteImageLocalRepository
import top.role.android.core.model.note.NoteMediaSyncState
import top.role.android.core.model.note.PersistedNoteMediaConflictSnapshot
import top.role.android.core.model.note.PersistedNoteMediaItem

sealed interface NoteMediaRefreshResult {

    /**
     * Remote media 已成为 authoritative，
     * 本地 relation 已完成 reconcile。
     */
    data class Refreshed(
        val noteId: String,
        val version: Long,
        val mediaCount: Int,
    ) : NoteMediaRefreshResult

    /**
     * Local 当前有 Pending change。
     *
     * Remote baseline 没有变化，
     * 因此保留 Local desired media。
     */
    data class PreservedLocal(
        val noteId: String,
        val version: Long,
    ) : NoteMediaRefreshResult

    /**
     * Local Pending 首次成功建立
     * remote media baseline。
     *
     * 不覆盖 Local desired media。
     */
    data class BaselineEstablished(
        val noteId: String,
        val version: Long,
    ) : NoteMediaRefreshResult

    /**
     * Waiting / Syncing：
     *
     * 不执行 Remote Pull，
     * 避免干扰 frozen request。
     */
    data object SkippedInFlight :
        NoteMediaRefreshResult

    /**
     * Remote version 已经偏离
     * Local media baseline。
     */
    data class Conflict(
        val noteId: String,
        val remoteVersion: Long,
    ) : NoteMediaRefreshResult
}


class NoteMediaRefreshService(
    private val imageLocalRepository:
    NoteImageLocalRepository,

    private val mediaLocalRepository:
    NoteMediaLocalRepository,

    private val mediaRemoteDataSource:
    NoteMediaRemoteDataSource,

    private val fileStore:
    LocalImageFileStore,
) {

    /**
     * Refresh 单条 Note 的 Media。
     *
     * Media Pull 与 Content Pull 独立。
     *
     * 不负责：
     *
     * - Content sync
     * - Image asset upload
     * - Media push
     */
    suspend fun refreshOne(
        ownerUserId: String,
        noteLocalKey: String,
        noteId: String,
    ): Result<NoteMediaRefreshResult> {
        return runCatching {
            require(
                ownerUserId.isNotBlank(),
            ) {
                "OWNER_USER_ID_REQUIRED"
            }

            require(
                noteLocalKey.isNotBlank(),
            ) {
                "NOTE_LOCAL_KEY_REQUIRED"
            }

            require(
                noteId.isNotBlank(),
            ) {
                "NOTE_ID_REQUIRED"
            }

            /*
             * Pull 前先读取 Media Sync State。
             *
             * Waiting / Syncing 不能被 Pull 干扰。
             *
             * Waiting 尤其重要：
             *
             * mutation 可能已经在服务端成功，
             * 只是 response 丢失。
             *
             * 下一轮应该继续用原 requestId retry，
             * 由服务端幂等返回真正的成功结果。
             */
            val state =
                mediaLocalRepository
                    .findForNote(
                        ownerUserId =
                            ownerUserId,

                        noteLocalKey =
                            noteLocalKey,
                    )

            if (
                state?.syncState ==
                NoteMediaSyncState.Waiting ||
                state?.syncState ==
                NoteMediaSyncState.Syncing
            ) {
                return@runCatching NoteMediaRefreshResult.SkippedInFlight
            }

            /*
             * 获取 canonical remote media。
             */
            val remote =
                mediaRemoteDataSource
                    .getNoteMedia(
                        noteId =
                            noteId,
                    )

            require(
                remote.noteId ==
                        noteId,
            ) {
                "NOTE_MEDIA_REMOTE_ID_MISMATCH"
            }

            val remoteVersion =
                remote.version
                    .toLong()

            val remoteUpdatedAt =
                remote.updatedAt
                    .toLong()

            val remoteMedia =
                normalizeRemoteMedia(
                    remote.media
                        .map { item ->
                            PersistedNoteMediaItem(
                                imageId =
                                    item.imageId,

                                order =
                                    item.order
                                        .toInt(),
                            )
                        },
                )

            /*
             * State 可能在网络请求期间变化。
             *
             * 所以 response 回来以后必须再次读取。
             */
            val latestState =
                mediaLocalRepository
                    .findForNote(
                        ownerUserId =
                            ownerUserId,

                        noteLocalKey =
                            noteLocalKey,
                    )

            /*
             * 如果 Pull 飞行期间进入
             * Waiting / Syncing，
             * 当前 remote response 不再应用。
             */
            if (
                latestState?.syncState ==
                NoteMediaSyncState.Waiting ||
                latestState?.syncState ==
                NoteMediaSyncState.Syncing
            ) {
                return@runCatching NoteMediaRefreshResult.SkippedInFlight
            }

            when (
                latestState?.syncState
            ) {
                /*
                 * ----------------------------------------
                 * No Media State
                 * ----------------------------------------
                 *
                 * 典型场景：
                 *
                 * 旧 Note 第一次在 Android hydration。
                 *
                 * Remote 可以直接成为 authoritative。
                 */
                null -> {
                    applyAuthoritativeRemote(
                        ownerUserId =
                            ownerUserId,

                        noteLocalKey =
                            noteLocalKey,

                        noteId =
                            noteId,

                        remoteVersion =
                            remoteVersion,

                        remoteUpdatedAt =
                            remoteUpdatedAt,

                        remoteMedia =
                            remoteMedia,
                    )

                    NoteMediaRefreshResult.Refreshed(
                        noteId =
                            noteId,

                        version =
                            remoteVersion,

                        mediaCount =
                            remoteMedia.size,
                    )
                }

                /*
                 * ----------------------------------------
                 * Synced
                 * ----------------------------------------
                 *
                 * Local 没有待提交 Media change。
                 *
                 * Remote 是 authoritative。
                 */
                NoteMediaSyncState.Synced -> {
                    applyAuthoritativeRemote(
                        ownerUserId =
                            ownerUserId,

                        noteLocalKey =
                            noteLocalKey,

                        noteId =
                            noteId,

                        remoteVersion =
                            remoteVersion,

                        remoteUpdatedAt =
                            remoteUpdatedAt,

                        remoteMedia =
                            remoteMedia,
                    )

                    NoteMediaRefreshResult.Refreshed(
                        noteId =
                            noteId,

                        version =
                            remoteVersion,

                        mediaCount =
                            remoteMedia.size,
                    )
                }

                /*
                 * ----------------------------------------
                 * Pending
                 * ----------------------------------------
                 *
                 * Local desired media 已经变化。
                 *
                 * Pull 永远不能覆盖它。
                 */
                NoteMediaSyncState.Pending -> {
                    handlePendingRemote(
                        ownerUserId =
                            ownerUserId,

                        noteLocalKey =
                            noteLocalKey,

                        noteId =
                            noteId,

                        localRemoteVersion =
                            latestState.remoteVersion,

                        remoteVersion =
                            remoteVersion,

                        remoteUpdatedAt =
                            remoteUpdatedAt,

                        remoteMedia =
                            remoteMedia,
                    )
                }

                /*
                 * ----------------------------------------
                 * Conflict
                 * ----------------------------------------
                 *
                 * Local desired media 保持不动。
                 *
                 * 只刷新最新 canonical remote snapshot。
                 */
                NoteMediaSyncState.Conflict -> {
                    val conflictSnapshot =
                        PersistedNoteMediaConflictSnapshot(
                            noteId =
                                noteId,

                            version =
                                remoteVersion,

                            updatedAt =
                                remoteUpdatedAt,

                            media =
                                remoteMedia,
                        )

                    mediaLocalRepository
                        .markConflict(
                            ownerUserId =
                                ownerUserId,

                            noteLocalKey =
                                noteLocalKey,

                            remote =
                                conflictSnapshot,

                            message =
                                latestState.lastSyncError,
                        )

                    NoteMediaRefreshResult.Conflict(
                        noteId =
                            noteId,

                        remoteVersion =
                            remoteVersion,
                    )
                }

                /*
                 * 前面已经处理。
                 *
                 * 这里保留 exhaustive when。
                 */
                NoteMediaSyncState.Waiting,
                NoteMediaSyncState.Syncing -> {
                    NoteMediaRefreshResult.SkippedInFlight
                }
            }
        }
    }


    // MARK: - Pending

    private suspend fun handlePendingRemote(
        ownerUserId: String,
        noteLocalKey: String,
        noteId: String,
        localRemoteVersion: Long?,
        remoteVersion: Long,
        remoteUpdatedAt: Long,
        remoteMedia: List<PersistedNoteMediaItem>,
    ): NoteMediaRefreshResult {

        /*
         * Local 从未建立 Media baseline。
         */
        if (
            localRemoteVersion ==
            null
        ) {
            /*
             * Remote 仍然是初始 version 0。
             *
             * 可以安全建立 baseline，
             * 但绝不能覆盖 Local desired media。
             */
            if (
                remoteVersion ==
                0L
            ) {
                mediaLocalRepository
                    .adoptRemoteBaselinePreservingLocal(
                        ownerUserId =
                            ownerUserId,

                        noteLocalKey =
                            noteLocalKey,

                        noteId =
                            noteId,

                        remoteVersion =
                            remoteVersion,

                        remoteUpdatedAt =
                            remoteUpdatedAt,
                    )

                return NoteMediaRefreshResult.BaselineEstablished(
                    noteId =
                        noteId,

                    version =
                        remoteVersion,
                )
            }

            /*
             * Local 没有 baseline，
             * Remote 却已经存在 Media history。
             *
             * 不能猜应该覆盖哪一边。
             */
            markVersionConflict(
                ownerUserId =
                    ownerUserId,

                noteLocalKey =
                    noteLocalKey,

                noteId =
                    noteId,

                remoteVersion =
                    remoteVersion,

                remoteUpdatedAt =
                    remoteUpdatedAt,

                remoteMedia =
                    remoteMedia,

                message =
                    "MEDIA_REMOTE_BASELINE_CONFLICT",
            )

            return NoteMediaRefreshResult.Conflict(
                noteId =
                    noteId,

                remoteVersion =
                    remoteVersion,
            )
        }

        /*
         * Remote version 与本地 baseline 一致。
         *
         * 说明 Remote 自 Local 开始编辑以后
         * 没有被其他端改变。
         *
         * Local Pending 可以安全继续等待 Push。
         */
        if (
            remoteVersion ==
            localRemoteVersion
        ) {
            return NoteMediaRefreshResult.PreservedLocal(
                noteId =
                    noteId,

                version =
                    remoteVersion,
            )
        }

        /*
         * Local 正在 Pending，
         * 但 Remote baseline 已经变化。
         *
         * 典型：
         *
         * Android baseline 3
         * Android local edit → Pending
         *
         * iOS:
         * 3 → 4
         *
         * Android Pull:
         * remote = 4
         *
         * → Conflict
         */
        markVersionConflict(
            ownerUserId =
                ownerUserId,

            noteLocalKey =
                noteLocalKey,

            noteId =
                noteId,

            remoteVersion =
                remoteVersion,

            remoteUpdatedAt =
                remoteUpdatedAt,

            remoteMedia =
                remoteMedia,

            message =
                "MEDIA_VERSION_CONFLICT",
        )

        return NoteMediaRefreshResult.Conflict(
            noteId =
                noteId,

            remoteVersion =
                remoteVersion,
        )
    }


    // MARK: - Authoritative

    private suspend fun applyAuthoritativeRemote(
        ownerUserId: String,
        noteLocalKey: String,
        noteId: String,
        remoteVersion: Long,
        remoteUpdatedAt: Long,
        remoteMedia: List<PersistedNoteMediaItem>,
    ) {
        /*
         * 先 reconcile relation。
         */
        val reconcileResult =
            imageLocalRepository
                .reconcileAuthoritativeRemoteMedia(
                    ownerUserId =
                        ownerUserId,

                    noteLocalKey =
                        noteLocalKey,

                    remoteMedia =
                        remoteMedia,
                )

        /*
         * 再更新 Media baseline。
         */
        mediaLocalRepository
            .applyAuthoritativeRemote(
                ownerUserId =
                    ownerUserId,

                noteLocalKey =
                    noteLocalKey,

                noteId =
                    noteId,

                remoteVersion =
                    remoteVersion,

                remoteUpdatedAt =
                    remoteUpdatedAt,
            )

        /*
         * Room relation 已经完成。
         *
         * 本地缓存文件清理失败
         * 不影响 Media Sync State。
         */
        reconcileResult
            .removedLocalPaths
            .forEach { localPath ->
                runCatching {
                    fileStore
                        .delete(
                            localPath =
                                localPath,
                        )
                }
            }
    }


    // MARK: - Conflict

    private suspend fun markVersionConflict(
        ownerUserId: String,
        noteLocalKey: String,
        noteId: String,
        remoteVersion: Long,
        remoteUpdatedAt: Long,
        remoteMedia: List<PersistedNoteMediaItem>,
        message: String?,
    ) {
        val snapshot =
            PersistedNoteMediaConflictSnapshot(
                noteId =
                    noteId,

                version =
                    remoteVersion,

                updatedAt =
                    remoteUpdatedAt,

                media =
                    remoteMedia,
            )

        mediaLocalRepository
            .markConflict(
                ownerUserId =
                    ownerUserId,

                noteLocalKey =
                    noteLocalKey,

                remote =
                    snapshot,

                message =
                    message,
            )
    }


    // MARK: - Helpers

    private fun normalizeRemoteMedia(
        media: List<PersistedNoteMediaItem>,
    ): List<PersistedNoteMediaItem> {
        val normalized =
            media
                .sortedBy {
                    it.order
                }
                .mapIndexed {
                        index,
                        item ->

                    PersistedNoteMediaItem(
                        imageId =
                            item.imageId,

                        order =
                            index,
                    )
                }

        val imageIds =
            normalized
                .map {
                    it.imageId
                }

        require(
            imageIds
                .distinct()
                .size ==
                    imageIds.size,
        ) {
            "DUPLICATE_REMOTE_NOTE_MEDIA"
        }

        return normalized
    }
}