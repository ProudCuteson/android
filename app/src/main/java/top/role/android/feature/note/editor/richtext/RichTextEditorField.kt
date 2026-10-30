package top.role.android.feature.note.editor.richtext

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.ExpandPolicy
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.insert
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RichTextEditorField(
    editor: RichTextEditorState,
    readOnly: Boolean = false,
    minHeight: Dp = 240.dp,
) {
    val color = MaterialTheme.colorScheme.onSurface
    val metaSnapshot = editor.lines.toList()
    val display = remember(metaSnapshot) {
        OutputTransformation {
            val lines = asCharSequence().toString().split('\n')
            var start = 0
            lines.forEachIndexed { index, str ->
                val meta = metaSnapshot.getOrNull(index)
                if (str.isNotEmpty() && meta != null) {
                    val range = TextRange(start, start + str.length)
                    val heading = when (meta.variant) {
                        "title" -> SpanStyle(fontSize = 25.sp, fontWeight = FontWeight.Bold)
                        "h1" -> SpanStyle(fontSize = 23.sp, fontWeight = FontWeight.Bold)
                        "h2" -> SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                        "h3" -> SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        "small" -> SpanStyle(fontSize = 12.sp)
                        "caption" -> SpanStyle(fontSize = 11.sp)
                        else -> null
                    }
                    if (heading != null) addStyle(heading, range, ExpandPolicy.InsideOnly)
                    val align = when (meta.align) {
                        "center" -> TextAlign.Center
                        "right" -> TextAlign.Right
                        "justify" -> TextAlign.Justify
                        "left" -> TextAlign.Left
                        else -> null
                    }
                    if (align != null || meta.lineHeight != null) {
                        addStyle(
                            ParagraphStyle(
                                textAlign = align ?: TextAlign.Unspecified,
                                lineHeight = meta.lineHeight?.let { (20f * it.toFloat()).sp }
                                    ?: androidx.compose.ui.unit.TextUnit.Unspecified,
                            ),
                            range, ExpandPolicy.InsideOnly,
                        )
                    }
                }
                start += str.length + 1
            }
            // List markers exist only in the visual output: never pollute run.text.
            var at = 0
            val positions = lines.map { str -> val old = at; at += str.length + 1; old }
            val markers = mutableListOf<String>()
            var sequence = 0
            lines.forEachIndexed { i, _ ->
                val list = metaSnapshot.getOrNull(i)?.list
                if (list?.type != "numbered") sequence = 0
                val marker = when (list?.type) {
                    "bullet" -> "• "
                    "numbered" -> {
                        sequence = if (sequence == 0) (list.start ?: 1.0).toInt() else sequence + 1
                        "$sequence. "
                    }
                    "checkbox" -> if (list.checked == true) "☑ " else "☐ "
                    else -> ""
                }
                val depth = (list?.indent?.toInt() ?: 0).coerceIn(0, 8)
                markers += if (marker.isEmpty()) "" else "  ".repeat(depth) + marker
            }
            for (i in lines.indices.reversed()) {
                if (markers[i].isNotEmpty()) insert(positions[i], markers[i])
            }
        }
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
    ) {
        BasicTextField(
            state = editor.field,
            readOnly = readOnly,
            outputTransformation = display,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minHeight)
                .focusRequester(editor.focusRequester),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = color),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            lineLimits = TextFieldLineLimits.MultiLine(),
            decorator = { inner ->
                Box(Modifier.fillMaxWidth()) {
                    if (editor.field.text.isEmpty()) Text(
                        "开始输入……", style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    inner()
                }
            },
        )
    }
}