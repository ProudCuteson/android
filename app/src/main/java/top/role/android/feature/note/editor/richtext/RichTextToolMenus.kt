package top.role.android.feature.note.editor.richtext

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.window.PopupProperties

/** 菜单不获取文本焦点，尽量保留正在编辑的选区及软键盘。 */
@Composable
internal fun RichTextToolMenu(
    type: RichToolMenuType,
    editor: RichTextEditorState,
    expanded: Boolean,
    onDismiss: () -> Unit,
) {
    val hasSelection = !editor.field.selection.collapsed
    val activeLine = editor.activeLine
    val listType = activeLine.list?.type

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = false),
    ) {
        when (type) {
            RichToolMenuType.Color -> {
                if (!hasSelection) MenuHint("请先选中文字")
                MenuOption("红色文字", enabled = hasSelection, onDismiss = onDismiss) {
                    editor.setValueMark(colorSpan(Color.Red)) { it.color != Color.Unspecified }
                }
                MenuOption("蓝色文字", enabled = hasSelection, onDismiss = onDismiss) {
                    editor.setValueMark(colorSpan(Color.Blue)) { it.color != Color.Unspecified }
                }
                MenuOption("清除文字颜色", enabled = hasSelection, onDismiss = onDismiss) {
                    editor.setValueMark(null) { it.color != Color.Unspecified }
                }
                HorizontalDivider()
                MenuOption("黄色高亮", enabled = hasSelection, onDismiss = onDismiss) {
                    editor.setValueMark(highlightSpan(Color.Yellow)) {
                        it.background != Color.Unspecified
                    }
                }
                MenuOption("清除高亮", enabled = hasSelection, onDismiss = onDismiss) {
                    editor.setValueMark(null) { it.background != Color.Unspecified }
                }
            }

            RichToolMenuType.Paragraph -> {
                val current = activeLine.variant ?: "paragraph"
                listOf(
                    "paragraph" to "正文",
                    "title" to "标题",
                    "h1" to "H1",
                    "h2" to "H2",
                    "h3" to "H3",
                    "small" to "Small",
                    "caption" to "Caption",
                ).forEach { (variant, title) ->
                    MenuOption(title, selected = current == variant, onDismiss = onDismiss) {
                        editor.setVariant(variant.takeUnless { it == "paragraph" })
                    }
                }
            }

            RichToolMenuType.List -> {
                MenuOption("无列表", selected = listType == null, onDismiss = onDismiss) {
                    editor.setList(null)
                }
                MenuOption("项目符号", selected = listType == "bullet", onDismiss = onDismiss) {
                    editor.setList("bullet")
                }
                MenuOption("编号列表", selected = listType == "numbered", onDismiss = onDismiss) {
                    editor.setList("numbered")
                }
                MenuOption("待办清单", selected = listType == "checkbox", onDismiss = onDismiss) {
                    editor.setList("checkbox")
                }
                if (listType == "checkbox") {
                    HorizontalDivider()
                    MenuOption(
                        "已完成",
                        selected = activeLine.list?.checked == true,
                        onDismiss = onDismiss,
                    ) { editor.toggleChecked() }
                }
            }

            RichToolMenuType.More -> {
                listOf(
                    RichInlineMark.Underline to "下划线",
                    RichInlineMark.Strike to "删除线",
                    RichInlineMark.Code to "行内代码",
                    RichInlineMark.Subscript to "下标",
                    RichInlineMark.Superscript to "上标",
                ).forEach { (mark, label) ->
                    MenuOption(label, selected = editor.markActive(mark), onDismiss = onDismiss) {
                        editor.toggleMark(mark)
                    }
                }

                HorizontalDivider()
                listOf(
                    "left" to "左对齐",
                    "center" to "居中",
                    "right" to "右对齐",
                    "justify" to "两端对齐",
                ).forEach { (align, label) ->
                    MenuOption(
                        label,
                        selected = (activeLine.align ?: "left") == align,
                        onDismiss = onDismiss,
                    ) { editor.setAlignment(align) }
                }
                MenuOption(
                    "1.5 倍行距",
                    selected = activeLine.lineHeight == 1.5,
                    onDismiss = onDismiss,
                ) {
                    editor.setLineHeight(if (activeLine.lineHeight == 1.5) null else 1.5)
                }
                HorizontalDivider()
                MenuOption("减少缩进", enabled = listType != null, onDismiss = onDismiss) {
                    editor.adjustIndent(-1)
                }
                MenuOption("增加缩进", enabled = listType != null, onDismiss = onDismiss) {
                    editor.adjustIndent(1)
                }
                HorizontalDivider()
                if (!hasSelection) MenuHint("字号、字体需要先选中文字")
                MenuOption("18sp 字号", enabled = hasSelection, onDismiss = onDismiss) {
                    editor.setValueMark(fontSizeSpan(18)) { it.fontSize != TextUnit.Unspecified }
                }
                MenuOption("默认字号", enabled = hasSelection, onDismiss = onDismiss) {
                    editor.setValueMark(null) { it.fontSize != TextUnit.Unspecified }
                }
                MenuOption("Serif 字体", enabled = hasSelection, onDismiss = onDismiss) {
                    editor.setValueMark(fontFamilySpan(FontFamily.Serif)) { it.fontFamily != null }
                }
                MenuOption("默认字体", enabled = hasSelection, onDismiss = onDismiss) {
                    editor.setValueMark(null) { it.fontFamily != null }
                }
            }
        }
    }
}

@Composable
private fun MenuHint(text: String) {
    DropdownMenuItem(text = { Text(text) }, enabled = false, onClick = {})
}

@Composable
private fun MenuOption(
    label: String,
    selected: Boolean = false,
    enabled: Boolean = true,
    onDismiss: () -> Unit,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = { Text(label) },
        enabled = enabled,
        trailingIcon = if (selected) {
            { Icon(Icons.Filled.Check, contentDescription = null) }
        } else null,
        onClick = {
            onDismiss()
            onClick()
        },
    )
}
