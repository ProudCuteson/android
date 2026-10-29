package top.role.android.feature.note.editor.richtext

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import top.role.android.core.model.note.content.BlockLine
import androidx.compose.foundation.ExperimentalFoundationApi

private val json = Json { prettyPrint = true; explicitNulls = false; encodeDefaults = false }
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RichTextDebugPanel(editor: RichTextEditorState) {
    val model = editor.export()
    val root = buildJsonObject {
        put("v", JsonPrimitive(1))
        put("lines", JsonArray(model.lines.map {
            json.encodeToJsonElement(BlockLine.serializer(), it)
        }))
    }
    val contentJson = json.encodeToString(JsonElement.serializer(), root)
    val selection = editor.field.selection
    val composition = editor.field.composition
    val styleCount = editor.field.textStyles.getSpanStyles(
        androidx.compose.ui.text.TextRange(0, editor.field.text.length)
    ).size
    val pending = editor.pendingMarks.entries.joinToString { "${it.key.label}=${it.value}" }.ifEmpty { "None" }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        HorizontalDivider()
        Text("Selection: ${selection.min}–${selection.max}" +
            if (selection.collapsed) " (光标)" else " (选区)",
            style = MaterialTheme.typography.labelSmall)
        Text(
            text = "Undo: ${editor.field.undoState.canUndo}" +
                    " · Redo: ${editor.field.undoState.canRedo}",
            style = MaterialTheme.typography.labelSmall,
        )
        Text("Composition: ${composition?.let { "${it.min}–${it.max}" } ?: "None"}  · Pending: $pending",
            style = MaterialTheme.typography.labelSmall)
        Text("Text: ${editor.field.text.length} · Lines: ${model.lines.size} · Runs: ${model.lines.sumOf { it.runs.size }} · Styles: $styleCount",
            style = MaterialTheme.typography.labelSmall)
        Text("当前段落：${blockLabel(editor.activeLine)} · 对齐：${editor.activeLine.align ?: "left"}",
            style = MaterialTheme.typography.labelSmall)
        Text("BlockContent v1", style = MaterialTheme.typography.titleMedium)
        Surface(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant) {
            SelectionContainer {
                Text(contentJson, modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
            }
        }
        Text("仅内存测试 · 返回后不保存", style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
