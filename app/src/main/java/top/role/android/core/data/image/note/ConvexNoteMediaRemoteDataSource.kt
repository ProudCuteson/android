package top.role.android.core.data.image.note

import kotlinx.coroutines.flow.first
import top.role.android.core.convex.ConvexMobileClient
import top.role.android.core.data.image.remote.RemoteNoteMediaResponse
import top.role.android.core.data.image.remote.RemoteReplaceNoteMediaResponse
import top.role.android.core.model.note.PersistedNoteMediaItem

class ConvexNoteMediaRemoteDataSource(
    private val convexClient:
    ConvexMobileClient,
) : NoteMediaRemoteDataSource {

    override suspend fun getNoteMedia(
        noteId: String,
    ): RemoteNoteMediaResponse {
        require(
            noteId.isNotBlank(),
        ) {
            "NOTE_ID_REQUIRED"
        }

        val result =
            convexClient
                .subscribe<RemoteNoteMediaResponse>(
                    name =
                        "mobile/android/note:getMobileNoteMedia",

                    args =
                        mapOf(
                            "noteId" to
                                    noteId,
                        ),
                )
                .first()

        return result
            .getOrThrow()
    }

    override suspend fun replaceNoteMedia(
        noteId: String,
        requestId: String,
        baseVersion: Long?,
        media: List<PersistedNoteMediaItem>,
    ): RemoteReplaceNoteMediaResponse {
        require(
            noteId.isNotBlank(),
        ) {
            "NOTE_ID_REQUIRED"
        }

        require(
            requestId.isNotBlank(),
        ) {
            "MEDIA_REQUEST_ID_REQUIRED"
        }

        val args =
            buildMap<String, Any?> {
                put(
                    "noteId",
                    noteId,
                )

                put(
                    "requestId",
                    requestId,
                )

                /*
                 * 新 Note Media baseline：
                 *
                 * null
                 * → 参数省略
                 *
                 * 已有 Media baseline：
                 * Long → Convex v.number() Double
                 */
                baseVersion
                    ?.let {
                        put(
                            "baseVersion",
                            it.toDouble(),
                        )
                    }

                put(
                    "media",
                    media.map { item ->
                        mapOf(
                            "type" to
                                    "image",

                            "imageId" to
                                    item.imageId,

                            "order" to
                                    item.order.toDouble(),
                        )
                    },
                )
            }

        return convexClient
            .mutation<RemoteReplaceNoteMediaResponse>(
                name =
                    "mobile/android/note:replaceMobileNoteMedia",

                args =
                    args,
            )
    }
}