
package top.role.android.core.model.note

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull

import top.role.android.core.model.note.content.NoteContentCodec

@Serializable
data class PullNoteDocumentsResponse(
    val changes: List<JsonObject>,
    val continueCursor: String? = null,
    val isDone: Boolean,
    val nextCheckpoint: NotePullCheckpoint? = null,
)

fun JsonObject.toRemoteNoteSnapshot():
        RemoteNoteSnapshot {

    fun string(
        key: String,
    ): String? =
        (this[key] as? JsonPrimitive)
            ?.contentOrNull

    fun number(
        key: String,
    ): Double =
        (this[key] as? JsonPrimitive)
            ?.doubleOrNull
            ?: error("Missing Note field: $key")

    fun boolean(
        key: String,
    ): Boolean =
        (this[key] as? JsonPrimitive)
            ?.booleanOrNull
            ?: error("Missing Note field: $key")

    val noteId =
        requireNotNull(string("_id")) {
            "Missing Note _id"
        }

    val content: JsonElement? =
        this["content"]?.takeUnless {
            it == JsonNull
        }

    return RemoteNoteSnapshot(
        noteId = noteId,
        clientKey = string("clientKey"),

        dayKey = string("dayKey"),
        onlyDay = boolean("onlyDay"),

        title = string("title").orEmpty(),
        text = NoteContentCodec.extractText(content),
        content = content,

        pinned =
            (this["pinned"] as? JsonPrimitive)
                ?.booleanOrNull == true,

        status = requireNotNull(string("status")) {
            "Missing Note status"
        },

        version = number("version"),
        createdAt = number("createdAt"),
        updatedAt = number("updatedAt"),
    )
}