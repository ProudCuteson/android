package top.role.android.feature.note.editor.richtext

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.PopupProperties
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
                RichTextColorMenu(
                    editor = editor,
                    onDismiss = onDismiss,
                )
            }

            RichToolMenuType.Paragraph -> {
                val current = activeLine.variant ?: "paragraph"

                listOf(
                    "paragraph" to "正文",
                    "h1" to "标题1",
                    "h2" to "标题2",
                    "h3" to "标题3",
                ).forEach { (variant, label) ->
                    MenuOption(
                        label,
                        selected = current == variant,
                        onDismiss = onDismiss,
                    ) {
                        editor.setVariant(
                            variant.takeUnless { it == "paragraph" }
                        )
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
            }


            RichToolMenuType.More -> {
                // 文字格式
                MoreSectionTitle("文字格式")

                listOf(
                    RichInlineMark.Underline to "下划线",
                    RichInlineMark.Strike to "删除线",
                    RichInlineMark.Code to "行内代码",
                ).forEach { (mark, label) ->
                    MenuOption(
                        label,
                        selected = editor.markActive(mark),
                        onDismiss = onDismiss,
                    ) {
                        editor.toggleMark(mark)
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                )

                // 段落对齐
                MoreSectionTitle("段落对齐")

                listOf(
                    "left" to "左对齐",
                    "center" to "居中",
                    "right" to "右对齐",
                ).forEach { (align, label) ->
                    MenuOption(
                        label,
                        selected = (activeLine.align ?: "left") == align,
                        onDismiss = onDismiss,
                    ) {
                        editor.setAlignment(align)
                    }
                }

                // 只有列表段落才显示缩进操作
                if (listType != null) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                    )

                    MoreSectionTitle("列表缩进")

                    MenuOption(
                        "减少缩进",
                        onDismiss = onDismiss,
                    ) {
                        editor.adjustIndent(-1)
                    }

                    MenuOption(
                        "增加缩进",
                        onDismiss = onDismiss,
                    ) {
                        editor.adjustIndent(1)
                    }
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

@Composable
private fun MoreSectionTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(
            start = 16.dp,
            end = 16.dp,
            top = 8.dp,
            bottom = 4.dp,
        ),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}