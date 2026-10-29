package top.role.android.feature.note.editor.richtext

import androidx.compose.ui.text.TextRange

/**
 * Committed-text diff used after Compose IME composition finishes. The editor never edits
 * characters during a composing session. This intentionally has no timing/debounce heuristic.
 */
fun committedInsertionRange(old: String, next: String): TextRange? {
    var prefix = 0
    while (prefix < old.length && prefix < next.length && old[prefix] == next[prefix]) prefix++
    var suffix = 0
    while (suffix < old.length - prefix && suffix < next.length - prefix &&
        old[old.length - 1 - suffix] == next[next.length - 1 - suffix]
    ) suffix++
    val end = next.length - suffix
    return if (end > prefix) TextRange(prefix, end) else null
}
