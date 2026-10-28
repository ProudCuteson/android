
package top.role.android.feature.note.editor

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.ExpandPolicy
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.text.font.FontFamily

private enum class RichMark {
    Bold,
    Italic,
    Underline;

    val style: SpanStyle
        get() = when (this) {
            Bold -> SpanStyle(
                fontWeight = FontWeight.Bold
            )
            Italic -> SpanStyle(
                fontStyle = FontStyle.Italic
            )
            Underline -> SpanStyle(
                textDecoration = TextDecoration.Underline
            )
        }

    fun matches(style: SpanStyle): Boolean =
        when (this) {
            Bold ->
                style.fontWeight == FontWeight.Bold

            Italic ->
                style.fontStyle == FontStyle.Italic

            Underline ->
                style.textDecoration == TextDecoration.Underline
        }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteRichTextPlayground() {

    val editorState = rememberTextFieldState(
        initialText =
            "今天是美好的一天。\n" +
                    "请选择一段文字，尝试加粗、斜体和下划线。\n" +
                    "也可以测试中文输入、删除和换行。"
    )

    val selection = editorState.selection

    val selectedStyles =
        editorState.textStyles.getSpanStyles(
            selection
        )

    val boldActive = selectedStyles.any {
        RichMark.Bold.matches(it.item)
    }

    val italicActive = selectedStyles.any {
        RichMark.Italic.matches(it.item)
    }

    val underlineActive = selectedStyles.any {
        RichMark.Underline.matches(it.item)
    }

    val allStyles =
        editorState.textStyles.getSpanStyles(
            TextRange(
                0,
                editorState.text.length
            )
        )

    // 直接实时生成 JSON，不需要展开状态。
    val contentJson =
        buildRichTextDebugJson(editorState)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .imePadding()
            .verticalScroll(
                rememberScrollState()
            )
            .padding(
                horizontal = 20.dp,
                vertical = 12.dp
            ),

        verticalArrangement =
            Arrangement.spacedBy(16.dp),
    ) {

        Text(
            text = "Rich Text Playground",
            style =
                MaterialTheme.typography.titleMedium,
        )

        // MARK: - Toolbar

        Row(
            horizontalArrangement =
                Arrangement.spacedBy(8.dp),
        ) {

            OutlinedButton(
                enabled = !selection.collapsed,
                onClick = {
                    toggleMark(
                        editorState,
                        RichMark.Bold
                    )
                },
            ) {
                Text(
                    text = if (boldActive) "B ✓" else "B",
                    fontWeight = FontWeight.Bold,
                )
            }

            OutlinedButton(
                enabled = !selection.collapsed,
                onClick = {
                    toggleMark(
                        editorState,
                        RichMark.Italic
                    )
                },
            ) {
                Text(
                    text = if (italicActive) "I ✓" else "I",
                    fontStyle = FontStyle.Italic,
                )
            }

            OutlinedButton(
                enabled = !selection.collapsed,
                onClick = {
                    toggleMark(
                        editorState,
                        RichMark.Underline
                    )
                },
            ) {
                Text(
                    text = if (underlineActive) {
                        "U ✓"
                    } else {
                        "U"
                    },
                    textDecoration =
                        TextDecoration.Underline,
                )
            }
        }

        Text(
            text = "选中文字后设置格式",
            style =
                MaterialTheme.typography.bodySmall,
            color =
                MaterialTheme.colorScheme
                    .onSurfaceVariant,
        )

        // MARK: - Editor

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape =
                MaterialTheme.shapes.medium,
            color =
                MaterialTheme.colorScheme
                    .surfaceVariant,
        ) {

            BasicTextField(
                state = editorState,

                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 240.dp)
                    .padding(16.dp),

                textStyle =
                    MaterialTheme.typography
                        .bodyLarge.copy(
                            color =
                                MaterialTheme.colorScheme
                                    .onSurface,
                        ),

                cursorBrush =
                    SolidColor(
                        MaterialTheme.colorScheme
                            .primary
                    ),

                lineLimits =
                    TextFieldLineLimits.MultiLine(),

                decorator = { innerTextField ->

                    Box(
                        modifier =
                            Modifier.fillMaxWidth()
                    ) {
                        if (editorState.text.isEmpty()) {
                            Text(
                                text = "开始输入……",
                                style =
                                    MaterialTheme
                                        .typography
                                        .bodyLarge,
                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .onSurfaceVariant,
                            )
                        }

                        innerTextField()
                    }
                },
            )
        }

        // MARK: - Debug Summary

        HorizontalDivider()

        Text(
            text =
                "Selection: ${selection.min} – ${selection.max}" +
                        if (selection.collapsed) {
                            " (光标)"
                        } else {
                            " (选区)"
                        },

            style =
                MaterialTheme.typography.labelSmall,
        )

        Text(
            text =
                "Text length: ${editorState.text.length}" +
                        " · Style ranges: ${allStyles.size}",

            style =
                MaterialTheme.typography.labelSmall,

            color =
                MaterialTheme.colorScheme
                    .onSurfaceVariant,
        )

        // MARK: - BlockContent JSON

        Text(
            text = "BlockContent v1",
            style =
                MaterialTheme.typography.titleMedium,
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape =
                MaterialTheme.shapes.medium,
            color =
                MaterialTheme.colorScheme
                    .surfaceVariant,
        ) {
            SelectionContainer {
                Text(
                    text = contentJson,

                    modifier =
                        Modifier.padding(16.dp),

                    style =
                        MaterialTheme.typography
                            .bodySmall,

                    fontFamily =
                        FontFamily.Monospace,
                )
            }
        }

        Text(
            text = "仅内存测试 · 返回后不保存",
            style =
                MaterialTheme.typography.labelSmall,
            color =
                MaterialTheme.colorScheme
                    .onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun toggleMark(
    state: TextFieldState,
    mark: RichMark,
) {
    state.edit {

        if (selection.collapsed) {
            return@edit
        }

        val start = selection.min
        val end = selection.max

        val matchingStyles =
            getSpanStyles(selection).filter {
                mark.matches(it.spanStyle)
            }

        if (matchingStyles.isEmpty()) {

            addStyle(
                spanStyle = mark.style,
                range = TextRange(start, end),
                expandPolicy = ExpandPolicy.AtEnd,
            )

            return@edit
        }

        // 取消选区内的对应格式，保留选区外的部分。
        for (tracked in matchingStyles) {

            val range = tracked.textRange

            when {
                range.start >= start &&
                        range.end <= end -> {

                    removeStyle(tracked)
                }

                range.start < start &&
                        range.end > end -> {

                    val oldEnd = range.end

                    tracked.textRange =
                        TextRange(range.start, start)

                    addStyle(
                        spanStyle = mark.style,
                        range = TextRange(end, oldEnd),
                        expandPolicy = ExpandPolicy.AtEnd,
                    )
                }

                range.start < start -> {

                    tracked.textRange =
                        TextRange(range.start, start)
                }

                else -> {

                    tracked.textRange =
                        TextRange(end, range.end)
                }
            }
        }
    }
}