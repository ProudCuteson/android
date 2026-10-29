package top.role.android.feature.note.editor.richtext

import android.graphics.Color as AndroidColor
import java.util.UUID
import androidx.compose.foundation.text.input.ExpandPolicy
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import top.role.android.core.model.note.content.BlockContent
import top.role.android.core.model.note.content.BlockLine
import top.role.android.core.model.note.content.BlockRun
import top.role.android.core.model.note.content.BlockMark

/** BlockContent v1 <-> native Compose editable text. Media is intentionally not part of this model. */
object RichTextContentMapper {
    fun plainText(content: BlockContent): String =
        content.lines.joinToString("\n") { line -> line.runs.joinToString("") { it.text } }

    fun sample(): BlockContent = BlockContent(
        v = 1,
        lines = listOf(
            BlockLine(id = UUID.randomUUID().toString(), runs = listOf(
                BlockRun(id = UUID.randomUUID().toString(), text = "今天是美好的一天。"),
            )),
            BlockLine(id = UUID.randomUUID().toString(), runs = listOf(
                BlockRun(id = UUID.randomUUID().toString(), text = "选中文字设置格式；光标状态可指定后续输入格式。"),
            )),
            BlockLine(id = UUID.randomUUID().toString(), runs = listOf(
                BlockRun(id = UUID.randomUUID().toString(), text = "测试中文输入、换行、标题、列表与对齐。"),
            )),
        ),
    )

    /** Import each independent property separately to keep toggle operations non-destructive. */
    fun importInlineStyles(field: TextFieldState, content: BlockContent) {
        var lineStart = 0
        field.edit {
            content.lines.forEachIndexed { index, line ->
                var offset = lineStart
                line.runs.forEach { run ->
                    val range = TextRange(offset, offset + run.text.length)
                    if (!range.collapsed) {
                        val mark = run.mark
                        if (mark?.bold == true) addStyle(RichInlineMark.Bold.spanStyle, range, ExpandPolicy.AtEnd)
                        if (mark?.italic == true) addStyle(RichInlineMark.Italic.spanStyle, range, ExpandPolicy.AtEnd)
                        if (mark?.underline == true) addStyle(RichInlineMark.Underline.spanStyle, range, ExpandPolicy.AtEnd)
                        if (mark?.strikethrough == true) addStyle(RichInlineMark.Strike.spanStyle, range, ExpandPolicy.AtEnd)
                        if (mark?.code == true) addStyle(RichInlineMark.Code.spanStyle, range, ExpandPolicy.AtEnd)
                        if (mark?.subscript == true) addStyle(RichInlineMark.Subscript.spanStyle, range, ExpandPolicy.AtEnd)
                        if (mark?.superscript == true) addStyle(RichInlineMark.Superscript.spanStyle, range, ExpandPolicy.AtEnd)
                        mark?.color?.let { parseColor(it) }?.let { addStyle(SpanStyle(color = it), range, ExpandPolicy.AtEnd) }
                        mark?.bgColor?.let { parseColor(it) }?.let { addStyle(SpanStyle(background = it), range, ExpandPolicy.AtEnd) }
                        mark?.fontSize?.let { addStyle(SpanStyle(fontSize = it.toFloat().sp), range, ExpandPolicy.AtEnd) }
                        mark?.fontFamily?.let { fontFamily(it) }?.let { addStyle(SpanStyle(fontFamily = it), range, ExpandPolicy.AtEnd) }
                    }
                    offset = range.end
                }
                lineStart = offset + if (index < content.lines.lastIndex) 1 else 0
            }
        }
    }

    /**
     * Export content without preview-generated ordinal IDs. Existing unchanged run IDs are reused
     * through editor.previousRuns; new runs get permanent UUIDs for the current editing session.
     */
    fun export(editor: RichTextEditorState): BlockContent {
        val text = editor.field.text.toString()
        val styleRanges = editor.field.textStyles.getSpanStyles(TextRange(0, text.length))
        val result = mutableListOf<BlockLine>()
        var offset = 0
        val strings = text.split('\n')
        strings.forEachIndexed { index, lineText ->
            val meta = editor.lines.getOrNull(index) ?: RichLineMeta()
            val start = offset
            val end = offset + lineText.length
            val cuts = mutableSetOf(start, end)
            styleRanges.forEach { r ->
                val a = maxOf(start, r.start)
                val b = minOf(end, r.end)
                if (a < b) { cuts += a; cuts += b }
            }
            val runs = mutableListOf<BlockRun>()
            val positions = cuts.sorted()
            for (i in 0 until positions.size - 1) {
                val a = positions[i]
                val b = positions[i + 1]
                if (a >= b) continue
                val mark = fromStyles(styleRanges.filter { it.start < b && it.end > a }.map { it.item })
                val value = text.substring(a, b)
                val previous = editor.previousRuns[meta.id].orEmpty()
                val reused = previous.getOrNull(runs.size)?.takeIf { it.mark == mark }
                    ?: previous.firstOrNull { it.text == value && it.mark == mark && runs.none { r -> r.id == it.id } }
                runs += BlockRun(id = reused?.id ?: UUID.randomUUID().toString(), text = value, mark = mark)
            }
            if (runs.isEmpty()) {
                val old = editor.previousRuns[meta.id].orEmpty().firstOrNull()
                runs += BlockRun(id = old?.id ?: UUID.randomUUID().toString(), text = "")
            }
            // Merge adjacent runs with equal marks (and preserve the first ID).
            val normalized = mutableListOf<BlockRun>()
            runs.forEach { run ->
                val prev = normalized.lastOrNull()
                if (prev != null && prev.mark == run.mark) {
                    normalized[normalized.lastIndex] = prev.copy(text = prev.text + run.text)
                } else normalized += run
            }
            editor.previousRuns[meta.id] = normalized
            result += BlockLine(
                id = meta.id, variant = meta.variant, list = meta.list, align = meta.align,
                lineHeight = meta.lineHeight, runs = normalized,
            )
            offset = end + 1
        }
        return BlockContent(v = 1, lines = result)
    }

    private fun fromStyles(styles: List<SpanStyle>): BlockMark? {
        var bold = false; var italic = false; var underline = false; var strike = false
        var code = false; var sub = false; var sup = false
        var color: String? = null; var bg: String? = null
        var fontSize: Double? = null; var family: String? = null
        for (style in styles) {
            if (style.fontWeight == FontWeight.Bold) bold = true
            if (style.fontStyle == FontStyle.Italic) italic = true
            if (style.textDecoration?.contains(TextDecoration.Underline) == true) underline = true
            if (style.textDecoration?.contains(TextDecoration.LineThrough) == true) strike = true
            if (style.fontFamily == FontFamily.Monospace) code = true
            if (style.baselineShift == BaselineShift.Subscript) sub = true
            if (style.baselineShift == BaselineShift.Superscript) sup = true
            if (style.color != Color.Unspecified) color = toCssColor(style.color)
            if (style.background != Color.Unspecified) bg = toCssColor(style.background)
            if (style.fontSize != TextUnit.Unspecified) fontSize = style.fontSize.value.toDouble()
            if (style.fontFamily != null && style.fontFamily != FontFamily.Monospace) {
                family = when (style.fontFamily) {
                    FontFamily.Serif -> "serif"
                    FontFamily.SansSerif -> "sans-serif"
                    FontFamily.Cursive -> "cursive"
                    else -> null
                }
            }
        }
        if (!bold && !italic && !underline && !strike && !code && !sub && !sup &&
            color == null && bg == null && fontSize == null && family == null) return null
        return BlockMark(
            bold = bold.takeIf { it }, italic = italic.takeIf { it },
            underline = underline.takeIf { it }, strikethrough = strike.takeIf { it },
            code = code.takeIf { it }, color = color, bgColor = bg,
            fontSize = fontSize, fontFamily = family,
            subscript = sub.takeIf { it }, superscript = sup.takeIf { it },
        )
    }

    private fun toCssColor(value: Color): String =
        String.format("#%08X", value.toArgb())

    private fun parseColor(raw: String): Color? = runCatching {
        Color(AndroidColor.parseColor(raw))
    }.getOrNull()

    private fun fontFamily(raw: String): FontFamily? = when (raw.lowercase()) {
        "serif" -> FontFamily.Serif
        "sans-serif", "sans" -> FontFamily.SansSerif
        "monospace", "mono" -> FontFamily.Monospace
        "cursive" -> FontFamily.Cursive
        else -> null
    }
}
