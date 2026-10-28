
package top.role.android.feature.note.editor

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

import top.role.android.core.model.note.content.BlockContent
import top.role.android.core.model.note.content.BlockLine
import top.role.android.core.model.note.content.BlockRun
import top.role.android.core.model.note.content.BlockMark

private val debugJson = Json {
    prettyPrint = true
}

@OptIn(ExperimentalFoundationApi::class)
fun buildRichTextDebugJson(
    state: TextFieldState,
): String {
    val text = state.text.toString()

    val styles = state.textStyles.getSpanStyles(
        TextRange(0, text.length)
    )

    val lines = mutableListOf<BlockLine>()

    var lineStart = 0
    var lineIndex = 0

    while (true) {
        val newlineIndex =
            text.indexOf('\n', lineStart)

        val lineEnd =
            if (newlineIndex >= 0) {
                newlineIndex
            } else {
                text.length
            }

        // 当前行中所有格式变化的位置。
        val boundaries = mutableSetOf(
            lineStart,
            lineEnd,
        )

        styles.forEach { style ->
            val start = maxOf(
                lineStart,
                style.start,
            )

            val end = minOf(
                lineEnd,
                style.end,
            )

            if (start < end) {
                boundaries += start
                boundaries += end
            }
        }

        val positions = boundaries.sorted()

        val runs = positions
            .zipWithNext()
            .mapIndexedNotNull { index, (start, end) ->

                if (start >= end) {
                    return@mapIndexedNotNull null
                }

                val activeStyles =
                    styles.filter { style ->
                        style.start < end &&
                                style.end > start
                    }

                val bold = activeStyles.any {
                    it.item.fontWeight ==
                            FontWeight.Bold
                }

                val italic = activeStyles.any {
                    it.item.fontStyle ==
                            FontStyle.Italic
                }

                val underline = activeStyles.any {
                    it.item.textDecoration
                        ?.contains(
                            TextDecoration.Underline
                        ) == true
                }

                val mark =
                    if (bold || italic || underline) {
                        BlockMark(
                            bold = if (bold) true else null,
                            italic = if (italic) true else null,
                            underline = if (underline) true else null,
                        )
                    } else {
                        null
                    }

                BlockRun(
                    id = "preview-run-${lineIndex + 1}-${index + 1}",
                    text = text.substring(start, end),
                    mark = mark,
                )
            }

        lines += BlockLine(
            id = "preview-line-${lineIndex + 1}",
            runs = runs.ifEmpty {
                listOf(
                    BlockRun(
                        id = "preview-run-${lineIndex + 1}-1",
                        text = "",
                    )
                )
            },
        )

        lineIndex++

        if (newlineIndex < 0) {
            break
        }

        lineStart = newlineIndex + 1
    }

    val content = BlockContent(
        v = 1,
        lines = lines,
    )

    // 明确输出 v:1，避免默认值序列化时省略。
    val root = buildJsonObject {
        put("v", JsonPrimitive(content.v))

        put(
            "lines",
            JsonArray(
                content.lines.map { line ->
                    debugJson.encodeToJsonElement(
                        BlockLine.serializer(),
                        line,
                    )
                }
            ),
        )
    }

    return debugJson.encodeToString(
        JsonElement.serializer(),
        root,
    )
}
