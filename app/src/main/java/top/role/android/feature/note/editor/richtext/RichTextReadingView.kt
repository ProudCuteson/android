
package top.role.android.feature.note.editor.richtext

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.role.android.core.model.note.content.BlockContent
import top.role.android.core.model.note.content.BlockLine
import top.role.android.core.model.note.content.BlockMark

@Composable
fun RichTextReadingView(
    content: BlockContent,
    modifier: Modifier = Modifier,
    onChecklistChange: ((lineId: String, checked: Boolean) -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    var number = 0
    var previousNumbered = false

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        content.lines.forEach { line ->
            val style = readingLineStyle(line, typography.bodyLarge)
            val text = readingAnnotatedText(line)
            val list = line.list
            val isNumbered = list?.type == "numbered"
            val isChecklist = list?.type == "checkbox"

            val marker = when (list?.type) {
                "bullet" -> "•"
                "numbered" -> {
                    number = if (previousNumbered) {
                        number + 1
                    } else {
                        (list.start ?: 1.0).toInt()
                    }
                    "$number."
                }
                else -> null
            }
            previousNumbered = isNumbered
            if (!isNumbered) number = 0

            val align = when (line.align) {
                "center" -> TextAlign.Center
                "right" -> TextAlign.Right
                "justify" -> TextAlign.Justify
                else -> TextAlign.Start
            }

            if (marker != null || isChecklist) {
                val indent = (list?.indent?.toInt() ?: 0).coerceIn(0, 8)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = (indent * 16).dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    if (isChecklist) {
                        Checkbox(
                            checked = list?.checked == true,
                            onCheckedChange = onChecklistChange?.let { callback ->
                                { checked -> callback(line.id, checked) }
                            },
                            modifier = Modifier.size(40.dp),
                        )
                    } else {
                        Text(
                            text = marker.orEmpty(),
                            modifier = Modifier.width(28.dp),
                            style = style,
                            color = colors.onSurfaceVariant,
                        )
                    }

                    Text(
                        text = if (text.isEmpty()) AnnotatedString(" ") else text,
                        modifier = Modifier
                            .weight(1f)
                            .then(if (isChecklist) Modifier.padding(top = 7.dp) else Modifier),
                        style = style,
                        color = colors.onSurface,
                        textAlign = align,
                    )
                }
            } else if (text.isEmpty()) {
                // 保留用户输入的空段落，但不占满一整行正文高度。
                Spacer(Modifier.height(12.dp))
            } else {
                Text(
                    text = text,
                    modifier = Modifier.fillMaxWidth(),
                    style = style,
                    color = colors.onSurface,
                    textAlign = align,
                )
            }
        }
    }
}

private fun readingLineStyle(
    line: BlockLine,
    base: TextStyle,
): TextStyle {
    val style = when (line.variant) {
        "title" -> base.copy(
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 36.sp,
        )
        "h1" -> base.copy(
            fontSize = 23.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 33.sp,
        )
        "h2" -> base.copy(
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 30.sp,
        )
        "h3" -> base.copy(
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 28.sp,
        )
        "small" -> base.copy(
            fontSize = 12.sp,
            lineHeight = 19.sp,
        )
        "caption" -> base.copy(
            fontSize = 11.sp,
            lineHeight = 18.sp,
        )
        else -> base.copy(
            fontSize = 18.sp,
            lineHeight = 24.sp,
        )
    }

    // 尊重 BlockLine 中用户明确设置的行高倍率。
    val customHeight = line.lineHeight
        ?.takeIf { it > 0 }
        ?.let { (style.fontSize.value * it.toFloat()).sp }

    return if (customHeight != null) {
        style.copy(lineHeight = customHeight)
    } else {
        style
    }
}

private fun readingAnnotatedText(line: BlockLine): AnnotatedString {
    return buildAnnotatedString {
        line.runs.forEach { run ->
            val mark = run.mark

            if (mark == null) {
                append(run.text)
            } else {
                withStyle(readingSpanStyle(mark)) {
                    append(run.text)
                }
            }
        }
    }
}

private fun readingSpanStyle(mark: BlockMark): SpanStyle {
    val decorations = listOfNotNull(
        TextDecoration.Underline.takeIf { mark.underline == true },
        TextDecoration.LineThrough.takeIf { mark.strikethrough == true },
    )

    return SpanStyle(
        fontWeight = FontWeight.Bold.takeIf { mark.bold == true },
        fontStyle = FontStyle.Italic.takeIf { mark.italic == true },
        textDecoration = decorations
            .takeIf { it.isNotEmpty() }
            ?.let(TextDecoration::combine),

        fontFamily = if (mark.code == true) {
            FontFamily.Monospace
        } else {
            readingFontFamily(mark.fontFamily)
        },

        fontSize = mark.fontSize
            ?.takeIf { it > 0 }
            ?.toFloat()
            ?.sp
            ?: androidx.compose.ui.unit.TextUnit.Unspecified,

        color = readingColor(mark.color) ?: Color.Unspecified,
        background = readingColor(mark.bgColor) ?: Color.Unspecified,

        baselineShift = when {
            mark.subscript == true -> BaselineShift.Subscript
            mark.superscript == true -> BaselineShift.Superscript
            else -> null
        },
    )
}

private fun readingFontFamily(value: String?): FontFamily? {
    return when (value?.lowercase()) {
        "serif" -> FontFamily.Serif
        "sans-serif", "sans" -> FontFamily.SansSerif
        "monospace", "mono" -> FontFamily.Monospace
        "cursive" -> FontFamily.Cursive
        else -> null
    }
}

private fun readingColor(value: String?): Color? {
    if (value.isNullOrBlank()) return null

    return runCatching {
        Color(AndroidColor.parseColor(value))
    }.getOrNull()
}