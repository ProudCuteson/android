package top.role.android.feature.note.editor.richtext

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.TextRange
import top.role.android.core.model.note.content.BlockContent
import top.role.android.core.model.note.content.BlockList
import androidx.compose.foundation.ExperimentalFoundationApi

/** One native text field owns text/IME/selection; the holder owns edit intent & BlockContent metadata. */
class RichTextEditorState(initial: BlockContent) {
    val field = TextFieldState(RichTextContentMapper.plainText(initial))
    val focusRequester = FocusRequester()
    val lines: SnapshotStateList<RichLineMeta> = mutableStateListOf<RichLineMeta>().also { list ->
        list.addAll(initial.lines.map { RichLineMeta.from(it) })
        if (list.isEmpty()) list.add(RichLineMeta())
    }
    val pendingMarks = mutableStateMapOf<RichInlineMark, Boolean>()

    internal val previousRuns = initial.lines.associate { it.id to it.runs }.toMutableMap()

    private var committedText = field.text.toString()
    private var observedSelection = field.selection
    private var internalChange = false

    init {
        RichTextContentMapper.importInlineStyles(field, initial)
    }

    val activeLineIndex: Int
        get() = lineIndexAt(
            this.field.text.toString(),
            this.field.selection.min
        )

    val activeLine: RichLineMeta
        get() = lines.getOrElse(activeLineIndex) { RichLineMeta() }

    /** Observe via snapshotFlow(text, selection, composition). Do not manipulate IME composition. */
    fun onNativeStateChanged() {
        if (internalChange || field.composition != null) return
        val text = field.text.toString()
        if (text != committedText) {
            val old = committedText
            reconcileLineMetadata(lines, old, text)
            val inserted = committedInsertionRange(old, text)
            if (inserted != null && pendingMarks.isNotEmpty()) {
                // Modify only committed characters, *after* IME composition ends.
                internalChange = true
                try {
                    field.edit {
                        pendingMarks.forEach { (mark, enabled) ->
                            if (enabled && bufferRangeMarked(inserted, mark)) return@forEach
                            clearInlineStyle(inserted) { mark.matches(it) }
                            if (enabled) addStyle(
                                mark.spanStyle, inserted,
                                androidx.compose.foundation.text.input.ExpandPolicy.AtEnd,
                            )
                        }
                    }
                } finally {
                    internalChange = false
                }
            }
            if (text.count { it == '\n' } > old.count { it == '\n' }) {
                // An explicit Enter ends the input-format intent for the following paragraph.
                pendingMarks.clear()
                RichInlineMark.entries.forEach { pendingMarks[it] = false }
            }
            committedText = text
        } else if (field.selection != observedSelection) {
            // Cursor relocation ends explicit input intent; local styles remain intact.
            pendingMarks.clear()
        }
        observedSelection = field.selection
    }

    fun markActive(mark: RichInlineMark): Boolean {
        pendingMarks[mark]?.let { return it }
        val sel = field.selection
        return if (sel.collapsed) markAtCursor(field, mark) else isRangeMarked(field, sel, mark)
    }

    fun toggleMark(mark: RichInlineMark) {
        if (field.selection.collapsed) {
            pendingMarks[mark] = !markActive(mark)
        } else {
            pendingMarks.clear()
            field.toggleInlineMark(mark)
        }
        focusRequester.requestFocus()
    }

    fun setValueMark(style: androidx.compose.ui.text.SpanStyle?, predicate: (androidx.compose.ui.text.SpanStyle) -> Boolean) {
        if (!field.selection.collapsed) field.setInlineValue(style, predicate)
        focusRequester.requestFocus()
    }

    fun editBlocks(transform: (RichLineMeta) -> RichLineMeta) {
        val text = field.text.toString()
        lineIndicesForSelection(text, field.selection).forEach { index ->
            if (index in lines.indices) lines[index] = transform(lines[index])
        }
        focusRequester.requestFocus()
    }

    fun setVariant(value: String?) = editBlocks { it.copy(variant = value, list = null) }
    fun setAlignment(value: String?) = editBlocks { it.copy(align = value) }
    fun setLineHeight(value: Double?) = editBlocks { it.copy(lineHeight = value) }
    fun setList(value: String?) = editBlocks { meta ->
        meta.copy(variant = null, list = if (value == null) null else BlockList(type = value, indent = meta.list?.indent))
    }
    fun adjustIndent(delta: Int) = editBlocks { meta ->
        val list = meta.list ?: return@editBlocks meta
        val current = list.indent?.toInt() ?: 0
        meta.copy(list = list.copy(indent = (current + delta).coerceIn(0, 8).toDouble()))
    }
    fun toggleChecked() = editBlocks { meta ->
        val list = meta.list
        if (list?.type == "checkbox") meta.copy(list = list.copy(checked = list.checked != true)) else meta
    }

    fun export(): BlockContent = RichTextContentMapper.export(this)

    @OptIn(ExperimentalFoundationApi::class)
    fun undo() {
        if (!field.undoState.canUndo) return

        val before = field.text.toString()

        pendingMarks.clear()
        field.undoState.undo()

        syncAfterHistory(before)
    }

    @OptIn(ExperimentalFoundationApi::class)
    fun redo() {
        if (!field.undoState.canRedo) return

        val before = field.text.toString()

        pendingMarks.clear()
        field.undoState.redo()

        syncAfterHistory(before)
    }

    private fun syncAfterHistory(before: String) {
        val after = field.text.toString()

        if (before != after) {
            reconcileLineMetadata(
                lines = lines,
                before = before,
                after = after,
            )
        }

        // 历史操作已经处理完毕，
        // 避免随后的 snapshotFlow 再把它当成一次新输入。
        committedText = after
        observedSelection = field.selection
    }

}
