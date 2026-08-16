package top.role.android.core.data.note

import kotlinx.coroutines.flow.first
import top.role.android.core.convex.ConvexMobileClient
import top.role.android.core.model.note.NotePullCheckpoint
import top.role.android.core.model.note.PersistedNoteSyncSnapshot
import top.role.android.core.model.note.PullNoteChangesResponse
import top.role.android.core.model.note.UpsertNoteResponse
import top.role.android.core.model.note.LifecycleNoteResponse

class NoteRemoteDataSource(
    private val convexClient:
    ConvexMobileClient,
) {

    suspend fun pullNoteChanges(
        checkpoint:
        NotePullCheckpoint? =
            null,

        cursor:
        String? =
            null,

        limit:
        Int =
            100,
    ): Result<PullNoteChangesResponse> {
        val args =
            buildMap<String, Any?> {
                if (
                    checkpoint != null
                ) {
                    put(
                        "checkpoint",
                        mapOf(
                            "kind" to
                                    checkpoint.kind,

                            "value" to
                                    checkpoint.value,
                        ),
                    )
                }

                if (
                    cursor != null
                ) {
                    put(
                        "cursor",
                        cursor,
                    )
                }

                /*
                 * Convex v.number()
                 * Android FFI 边界传 Double。
                 */
                put(
                    "limit",
                    limit.toDouble(),
                )
            }

        return convexClient
            .subscribe<PullNoteChangesResponse>(
                "mobile/android/note:pullNoteChanges",
                args,
            )
            .first()
    }

    suspend fun upsertNote(
        snapshot:
        PersistedNoteSyncSnapshot,
    ): Result<UpsertNoteResponse> {
        return runCatching {
            val args =
                buildMap<String, Any?> {
                    put(
                        "requestId",
                        snapshot.requestId,
                    )

                    if (
                        snapshot.noteId !=
                        null
                    ) {
                        put(
                            "noteId",
                            snapshot.noteId,
                        )
                    }

                    put(
                        "clientKey",
                        snapshot.clientKey,
                    )

                    if (
                        snapshot.baseVersion !=
                        null
                    ) {
                        put(
                            "baseVersion",
                            snapshot.baseVersion
                                .toDouble(),
                        )
                    }

                    if (
                        snapshot.dayKey !=
                        null
                    ) {
                        put(
                            "dayKey",
                            snapshot.dayKey,
                        )
                    }

                    put(
                        "onlyDay",
                        snapshot.onlyDay,
                    )

                    put(
                        "title",
                        snapshot.title,
                    )

                    put(
                        "text",
                        snapshot.text,
                    )

                    put(
                        "pinned",
                        snapshot.pinned,
                    )
                }

            convexClient
                .mutation<UpsertNoteResponse>(
                    "mobile/android/note:upsertNote",
                    args,
                )
        }
    }

    suspend fun archiveNote(
        noteId: String,
        baseVersion: Long?,
    ): Result<LifecycleNoteResponse> {
        return mutateLifecycle(
            functionName =
                "mobile/android/note:archiveNote",

            noteId =
                noteId,

            baseVersion =
                baseVersion,
        )
    }

    suspend fun restoreNote(
        noteId: String,
        baseVersion: Long?,
    ): Result<LifecycleNoteResponse> {
        return mutateLifecycle(
            functionName =
                "mobile/android/note:restoreNote",

            noteId =
                noteId,

            baseVersion =
                baseVersion,
        )
    }

    suspend fun softDeleteNote(
        noteId: String,
        baseVersion: Long?,
    ): Result<LifecycleNoteResponse> {
        return mutateLifecycle(
            functionName =
                "mobile/android/note:softDeleteNote",

            noteId =
                noteId,

            baseVersion =
                baseVersion,
        )
    }

    private suspend fun mutateLifecycle(
        functionName: String,
        noteId: String,
        baseVersion: Long?,
    ): Result<LifecycleNoteResponse> {
        return runCatching {
            val args =
                buildMap<String, Any?> {
                    put(
                        "noteId",
                        noteId,
                    )

                    if (
                        baseVersion != null
                    ) {
                        /*
                         * Convex v.number()
                         * Android FFI 边界使用 Double。
                         */
                        put(
                            "baseVersion",
                            baseVersion.toDouble(),
                        )
                    }
                }

            convexClient
                .mutation<LifecycleNoteResponse>(
                    functionName,
                    args,
                )
        }
    }

}