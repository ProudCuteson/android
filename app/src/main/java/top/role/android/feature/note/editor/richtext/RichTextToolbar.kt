package top.role.android.feature.note.editor.richtext

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import top.role.android.feature.note.editor.NoteImagePickButton

internal enum class RichToolMenuType { Color, Paragraph, List, More }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RichTextToolbar(
    editor: RichTextEditorState,
    onAddImage: (Uri) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expandedMenu by remember {
        mutableStateOf<RichToolMenuType?>(null)
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // 左侧：富文本工具，可横向滚动
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RichToolIcon(
                icon = Icons.Filled.Undo,
                description = "撤销",
                enabled = editor.field.undoState.canUndo,
                onClick = { editor.undo() },
            )

            RichToolIcon(
                icon = Icons.Outlined.FormatBold,
                description = "加粗",
                selected = editor.markActive(RichInlineMark.Bold),
                onClick = { editor.toggleMark(RichInlineMark.Bold) },
            )

            RichToolIcon(
                icon = Icons.Outlined.FormatItalic,
                description = "斜体",
                selected = editor.markActive(RichInlineMark.Italic),
                onClick = { editor.toggleMark(RichInlineMark.Italic) },
            )

            RichMenuAnchor(
                type = RichToolMenuType.Color,
                icon = Icons.Outlined.Palette,
                description = "颜色与高亮",
                editor = editor,
                expandedMenu = expandedMenu,
                onToggle = { expandedMenu = it },
            )

            Spacer(Modifier.width(8.dp))

            RichMenuAnchor(
                type = RichToolMenuType.Paragraph,
                icon = Icons.Outlined.FormatSize,
                description = "段落格式",
                editor = editor,
                expandedMenu = expandedMenu,
                onToggle = { expandedMenu = it },
                selected = editor.activeLine.variant != null &&
                        editor.activeLine.variant != "paragraph",
            )

            RichMenuAnchor(
                type = RichToolMenuType.List,
                icon = Icons.Outlined.List,
                description = "列表",
                editor = editor,
                expandedMenu = expandedMenu,
                onToggle = { expandedMenu = it },
                selected = editor.activeLine.list != null,
            )
        }

        Spacer(Modifier.width(8.dp))

        // 右侧：图片 + 更多，固定显示
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            NoteImagePickButton(
                onAddImage = onAddImage,
            )

            RichMenuAnchor(
                type = RichToolMenuType.More,
                icon = Icons.Outlined.MoreHoriz,
                description = "更多格式",
                editor = editor,
                expandedMenu = expandedMenu,
                onToggle = { expandedMenu = it },
            )
        }
    }
}

@Composable
private fun RichToolIcon(
    icon: ImageVector,
    description: String,
    selected: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(44.dp)
            .background(
                if (selected) MaterialTheme.colorScheme.secondaryContainer
                else MaterialTheme.colorScheme.surface,
                RoundedCornerShape(10.dp),
            ),
    ) {
        Icon(icon, contentDescription = description)
    }
}

@Composable
private fun RichMenuAnchor(
    type: RichToolMenuType,
    icon: ImageVector,
    description: String,
    editor: RichTextEditorState,
    expandedMenu: RichToolMenuType?,
    onToggle: (RichToolMenuType?) -> Unit,
    selected: Boolean = false,
) {
    val expanded = expandedMenu == type
    Box {
        RichToolIcon(
            icon = icon,
            description = description,
            selected = selected || expanded,
            onClick = { onToggle(if (expanded) null else type) },
        )
        RichTextToolMenu(
            type = type,
            editor = editor,
            expanded = expanded,
            onDismiss = { onToggle(null) },
        )
    }
}