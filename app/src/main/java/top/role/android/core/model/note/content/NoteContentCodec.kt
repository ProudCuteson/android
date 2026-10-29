
package top.role.android.core.model.note.content

import java.util.UUID

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull

object NoteContentCodec {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
    }

    // MARK: - JSON

    fun parse(raw: String?): JsonElement? {
        if (raw.isNullOrBlank()) return null

        return json.parseToJsonElement(raw)
            .takeUnless { it == JsonNull }
            ?.let(::normalizeVersion)
    }

    fun stringify(
        content: JsonElement,
    ): String {
        return json.encodeToString(
            JsonElement.serializer(),
            content,
        )
    }

    // MARK: - Model

    fun readModel(
        content: JsonElement,
    ): BlockContent {
        val model =
            json.decodeFromJsonElement<BlockContent>(
                normalizeVersion(content),
            )

        require(model.v == 1) {
            "Unsupported BlockContent version: ${model.v}"
        }

        return model
    }

    // MARK: - Plain text

    fun extractText(
        content: JsonElement?,
    ): String {
        if (content == null) {
            return ""
        }

        val model = readModel(content)

        return model.lines.joinToString("\n") { line ->
            line.runs.joinToString("") { run ->
                run.text
            }
        }
    }

    // MARK: - Legacy text conversion

    fun fromPlainText(
        text: String,
    ): JsonElement {
        val normalized =
            text.replace("\r\n", "\n")
                .replace("\r", "\n")

        val lines =
            if (normalized.isEmpty()) {
                emptyList()
            } else {
                normalized.split("\n")
                    .map { lineText ->
                        BlockLine(
                            id = UUID.randomUUID().toString(),
                            runs = listOf(
                                BlockRun(
                                    id = UUID.randomUUID().toString(),
                                    text = lineText,
                                )
                            ),
                        )
                    }
            }

        return json.encodeToJsonElement(
            BlockContent(
                lines = lines,
            )
        )
    }

    // MARK: - Equality

    fun sameContent(
        leftJson: String?,
        rightJson: String?,
    ): Boolean {
        return parse(leftJson) ==
                parse(rightJson)
    }

    private fun normalizeVersion(element: JsonElement): JsonElement {
        val obj = element as? JsonObject ?: return element
        val version = obj["v"] as? JsonPrimitive ?: return element

        if (version.doubleOrNull != 1.0) return element

        return JsonObject(
            obj + ("v" to JsonPrimitive(1))
        )
    }

}