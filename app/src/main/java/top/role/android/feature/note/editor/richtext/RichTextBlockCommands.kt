package top.role.android.feature.note.editor.richtext

import java.util.UUID
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.text.TextRange
import top.role.android.core.model.note.content.BlockLine
import top.role.android.core.model.note.content.BlockList

/** Note: block metadata is kept separately; no bullet or heading characters are inserted in text. */
data class RichLineMeta(
    val id: String = UUID.randomUUID().toString(),
    val variant: String? = null,
    val list: BlockList? = null,
    val align: String? = null,
    val lineHeight: Double? = null,
) {
    companion object {
        fun from(line: BlockLine) = RichLineMeta(
            id = line.id, variant = line.variant, list = line.list,
            align = line.align, lineHeight = line.lineHeight,
        )
    }
}

fun lineIndexAt(text: String, offset: Int): Int =
    text.take(offset.coerceIn(0, text.length)).count { it == '\n' }

fun lineIndicesForSelection(text: String, selection: TextRange): IntRange {
    val first = lineIndexAt(text, selection.min)
    val last = lineIndexAt(text, if (selection.collapsed) selection.min else (selection.max - 1))
    return first..last
}

/** Preserve unaffected prefix/suffix lines and the first edited line's identity. */

fun reconcileLineMetadata(
    lines: SnapshotStateList<RichLineMeta>,
    before: String,
    after: String,
) {
    if (before == after) return

    val old = before.split('\n')
    val next = after.split('\n')
    val previousMeta = lines.toList()
    var prefix = 0

    while (
        prefix < old.size &&
        prefix < next.size &&
        old[prefix] == next[prefix]
    ) {
        prefix++
    }

    var suffix = 0

    while (
        suffix < old.size - prefix &&
        suffix < next.size - prefix &&
        old[old.lastIndex - suffix] ==
        next[next.lastIndex - suffix]
    ) {
        suffix++
    }

    val oldChangedCount = old.size - prefix - suffix
    val newChangedCount = next.size - prefix - suffix
    val replacement = mutableListOf<RichLineMeta>()

    replacement.addAll(
        previousMeta.take(prefix)
    )

    val original = if (oldChangedCount > 0) {
            previousMeta.getOrNull(prefix)
        } else {
            null
        }

    val inherited = original ?: previousMeta.getOrNull(prefix - 1)

    repeat(newChangedCount) { index ->
        if (index == 0 && original != null) {
            replacement.add(original)
        } else {
            replacement.add(
                RichLineMeta(
                    list = inherited
                        ?.list
                        ?.copy(checked = false)
                )
            )
        }
    }

    if (suffix > 0) {
        replacement.addAll(
            previousMeta.takeLast(suffix)
        )
    }

    if (replacement.isEmpty()) {
        replacement.add(RichLineMeta())
    }

    lines.clear()
    lines.addAll(replacement)
}

fun blockLabel(meta: RichLineMeta): String = when {
    meta.list != null -> when (meta.list.type) {
        "bullet" -> "• 列表"
        "numbered" -> "1. 列表"
        "checkbox" -> if (meta.list.checked == true) "☑ 待办" else "☐ 待办"
        else -> meta.list.type
    }
    meta.variant == null || meta.variant == "paragraph" -> "正文"
    else -> meta.variant.uppercase()
}
