package top.role.android.core.data.image.note

import top.role.android.core.data.image.remote.RemoteNoteMediaResponse
import top.role.android.core.data.image.remote.RemoteReplaceNoteMediaResponse
import top.role.android.core.model.note.PersistedNoteMediaItem

interface NoteMediaRemoteDataSource {

    suspend fun getNoteMedia(
        noteId: String,
    ): RemoteNoteMediaResponse

    suspend fun replaceNoteMedia(
        noteId: String,
        requestId: String,
        baseVersion: Long?,
        media: List<PersistedNoteMediaItem>,
    ): RemoteReplaceNoteMediaResponse
}