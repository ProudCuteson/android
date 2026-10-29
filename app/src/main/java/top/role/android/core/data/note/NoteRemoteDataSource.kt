package top.role.android.core.data.note

import kotlinx.serialization.json.JsonElement
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import top.role.android.core.convex.ConvexMobileClient
import top.role.android.core.model.note.NotePullCheckpoint
import top.role.android.core.model.note.PersistedNoteSyncSnapshot
import top.role.android.core.model.note.PullNoteChangesResponse
import top.role.android.core.model.note.UpsertNoteResponse
import top.role.android.core.model.note.LifecycleNoteResponse
import top.role.android.core.model.note.content.NoteContentCodec

class NoteRemoteDataSource(
    private val convexClient:
    ConvexMobileClient,
) {

    suspend fun pullNoteChanges(
        checkpoint: NotePullCheckpoint? = null,
        cursor: String? = null,
        limit: Int = 100,
    ): Result<PullNoteChangesResponse> {
        val args = buildMap<String, Any?> {
            if (
                checkpoint != null
            ) {
                put( "checkpoint", mapOf(
                        "kind" to  checkpoint.kind,
                        "value" to  checkpoint.value,
                    ),
                )
            }

            if (cursor != null) {
                put(
                    "cursor",
                    cursor,
                )
            }

            put(
                "limit",
                limit.toDouble(),
            )
        }

        return convexClient
            .subscribe<PullNoteChangesResponse>(
                "mobile/ios/note/native/pull:pullNoteChanges",
                args,
            )
            .first()
    }

    suspend fun upsertNote(
        snapshot: PersistedNoteSyncSnapshot,
    ): Result<UpsertNoteResponse> {
        return runCatching {
            val args =  buildMap<String, Any?> {
                put(
                    "requestId",
                    snapshot.requestId,
                )

                if (snapshot.noteId != null) {
                    put(
                        "noteId",
                        snapshot.noteId,
                    )
                }

                put(
                    "clientKey",
                    snapshot.clientKey,
                )

                if ( snapshot.baseVersion != null ) {
                    put(
                        "baseVersion",
                        snapshot.baseVersion.toDouble(),
                    )
                }

                if (snapshot.dayKey != null ) {
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
                    "content",
                    (
                            snapshot.content
                                ?: NoteContentCodec.fromPlainText(snapshot.text)
                            ).toConvexArg(),
                )

            }

            convexClient
                .mutation<UpsertNoteResponse>(
                    "mobile/ios/note/native/upsert:upsertNote",
                    args,
                )
        }
    }

    suspend fun setPinned(
        noteId: String,
        pinned: Boolean,
    ): Result<JsonElement> {
        return runCatching {
            convexClient.mutation<JsonElement>(
                "mobile/ios/note/native/upsert:setPinned",
                mapOf(
                    "noteId" to noteId,
                    "pinned" to pinned,
                ),
            )
        }
    }

    suspend fun archiveNote(
        noteId: String,
        baseVersion: Long?,
    ): Result<LifecycleNoteResponse> {
        return mutateLifecycle(
            functionName =
                "mobile/ios/note/native/lifecycle:archiveNote",

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
                "mobile/ios/note/native/lifecycle:restoreNote",

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
                "mobile/ios/note/native/lifecycle:softDeleteNote",

            noteId =
                noteId,

            baseVersion =
                baseVersion,
        )
    }

    private fun JsonElement.toConvexArg(): Any? = when (this) {
        is JsonObject -> entries.associate { (key, value) ->
            key to value.toConvexArg()
        }

        is JsonArray -> map { it.toConvexArg() }

        JsonNull -> null

        is JsonPrimitive -> when {
            isString -> content
            booleanOrNull != null -> booleanOrNull
            else -> doubleOrNull
                ?: error("Unsupported JSON number: $content")
        }
    }


    private suspend fun mutateLifecycle(
        functionName: String,
        noteId: String,
        baseVersion: Long?,
    ): Result<LifecycleNoteResponse> {
        return runCatching {
            convexClient.mutation<LifecycleNoteResponse>(
                functionName,
                mapOf("noteId" to noteId),
            )
        }
    }

}