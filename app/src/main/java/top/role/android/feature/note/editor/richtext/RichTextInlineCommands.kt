package top.role.android.feature.note.editor.richtext

import androidx.compose.foundation.text.input.ExpandPolicy
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

enum class RichInlineMark(val label: String) {
    Bold("B"), Italic("I"), Underline("U"), Strike("S"), Code("Code"), Subscript("X₂"), Superscript("X²");

    val spanStyle: SpanStyle
        get() = when (this) {
            Bold -> SpanStyle(fontWeight = FontWeight.Bold)
            Italic -> SpanStyle(fontStyle = FontStyle.Italic)
            Underline -> SpanStyle(textDecoration = TextDecoration.Underline)
            Strike -> SpanStyle(textDecoration = TextDecoration.LineThrough)
            Code -> SpanStyle(fontFamily = FontFamily.Monospace)
            Subscript -> SpanStyle(baselineShift = BaselineShift.Subscript)
            Superscript -> SpanStyle(baselineShift = BaselineShift.Superscript)
        }

    fun matches(style: SpanStyle): Boolean = when (this) {
        Bold -> style.fontWeight == FontWeight.Bold
        Italic -> style.fontStyle == FontStyle.Italic
        Underline -> style.textDecoration?.contains(TextDecoration.Underline) == true
        Strike -> style.textDecoration?.contains(TextDecoration.LineThrough) == true
        Code -> style.fontFamily == FontFamily.Monospace
        Subscript -> style.baselineShift == BaselineShift.Subscript
        Superscript -> style.baselineShift == BaselineShift.Superscript
    }
}

fun isRangeMarked(state: TextFieldState, range: TextRange, mark: RichInlineMark): Boolean {
    if (range.collapsed) return false
    val spans = state.textStyles.getSpanStyles(range).filter { mark.matches(it.item) }
    var cursor = range.min
    while (cursor < range.max) {
        cursor = spans.filter { it.start <= cursor && it.end > cursor }
            .maxOfOrNull { it.end } ?: return false
    }
    return true
}

fun markAtCursor(state: TextFieldState, mark: RichInlineMark): Boolean {
    val length = state.text.length
    if (length == 0) return false
    val cursor = state.selection.min
    val from = if (cursor == 0) 0 else cursor - 1
    return state.textStyles.getSpanStyles(TextRange(from, from + 1))
        .any { mark.matches(it.item) }
}

/** Used during a committed input edit to avoid creating a new style span for every keystroke. */
fun TextFieldBuffer.bufferRangeMarked(range: TextRange, mark: RichInlineMark): Boolean {
    if (range.collapsed) return false
    val spans = getSpanStyles(range).filter { it.isValid && mark.matches(it.spanStyle) }
    var pos = range.min
    while (pos < range.max) {
        pos = spans.filter { it.textRange.start <= pos && it.textRange.end > pos }
            .maxOfOrNull { it.textRange.end } ?: return false
    }
    return true
}

/** Playground input applies one property per SpanStyle. Never remove unrelated styles. */
fun TextFieldBuffer.clearInlineStyle(target: TextRange, predicate: (SpanStyle) -> Boolean) {
    if (target.collapsed) return
    val from = target.min
    val to = target.max
    val matching = getSpanStyles(TextRange(from, to))
        .filter { predicate(it.spanStyle) }
    for (tracked in matching) {
        if (!tracked.isValid) continue
        val current = tracked.textRange
        when {
            current.start >= from && current.end <= to -> removeStyle(tracked)
            current.start < from && current.end > to -> {
                val oldEnd = current.end
                val style = tracked.spanStyle
                tracked.textRange = TextRange(current.start, from)
                addStyle(style, TextRange(to, oldEnd), ExpandPolicy.AtEnd)
            }
            current.start < from -> tracked.textRange = TextRange(current.start, from)
            else -> tracked.textRange = TextRange(to, current.end)
        }
    }
}

fun TextFieldState.toggleInlineMark(mark: RichInlineMark) {
    val range = selection
    if (range.collapsed) return
    val already = isRangeMarked(this, range, mark)
    edit {
        clearInlineStyle(range) { mark.matches(it) }
        if (!already) addStyle(mark.spanStyle, range, ExpandPolicy.AtEnd)
    }
}

/** Format a selection with a single-value property, preserving all other properties. */
fun TextFieldState.setInlineValue(
    style: SpanStyle?,
    predicate: (SpanStyle) -> Boolean,
) {
    val range = selection
    if (range.collapsed) return
    edit {
        clearInlineStyle(range, predicate)
        if (style != null) addStyle(style, range, ExpandPolicy.AtEnd)
    }
}

fun colorSpan(color: Color) = SpanStyle(color = color)
fun highlightSpan(color: Color) = SpanStyle(background = color)
fun fontSizeSpan(size: Int) = SpanStyle(fontSize = size.sp)
fun fontFamilySpan(family: FontFamily) = SpanStyle(fontFamily = family)
