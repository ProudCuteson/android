
package top.role.android.feature.note.editor.richtext

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

private data class ColorPreset(
    val name: String,
    val hex: String,
)

private val foregroundLight = listOf(
    ColorPreset("红", "#B91C1C"),
    ColorPreset("橙", "#9A3412"),
    ColorPreset("绿", "#008236"),
    ColorPreset("蓝", "#1D4ED8"),
    ColorPreset("紫", "#7E22CE"),
)

private val foregroundDark = listOf(
    ColorPreset("红", "#FCA5A5"),
    ColorPreset("橙", "#FDBA74"),
    ColorPreset("绿", "#86EFAC"),
    ColorPreset("蓝", "#93C5FD"),
    ColorPreset("紫", "#D8B4FE"),
)

private val backgroundLight = listOf(
    ColorPreset("红", "#FEE2E2"),
    ColorPreset("橙", "#FFEDD5"),
    ColorPreset("绿", "#DCFCE7"),
    ColorPreset("蓝", "#DBEAFE"),
    ColorPreset("紫", "#F3E8FF"),
)

private val backgroundDark = listOf(
    ColorPreset("红", "#7F1D1D"),
    ColorPreset("橙", "#7C2D12"),
    ColorPreset("绿", "#14532D"),
    ColorPreset("蓝", "#1E3A8A"),
    ColorPreset("紫", "#581C87"),
)

@Composable
fun RichTextColorMenu(
    editor: RichTextEditorState,
    onDismiss: () -> Unit,
) {
    val hasSelection = !editor.field.selection.collapsed

    Column(
        modifier = Modifier.width(272.dp),
    ) {
        if (!hasSelection) {
            Text(
                text = "请先选中文字",
                modifier = Modifier.padding(
                    horizontal = 12.dp,
                    vertical = 8.dp,
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        ColorPaletteGroup(
            title = "前景色 · Light",
            colors = foregroundLight,
            enabled = hasSelection,
        ) { hex ->
            applyTextColor(editor, hex)
            onDismiss()
        }

        ColorPaletteGroup(
            title = "前景色 · Dark",
            colors = foregroundDark,
            enabled = hasSelection,
        ) { hex ->
            applyTextColor(editor, hex)
            onDismiss()
        }

        HorizontalDivider()

        ColorPaletteGroup(
            title = "背景色 · Light",
            colors = backgroundLight,
            enabled = hasSelection,
        ) { hex ->
            applyBackgroundColor(editor, hex)
            onDismiss()
        }

        ColorPaletteGroup(
            title = "背景色 · Dark",
            colors = backgroundDark,
            enabled = hasSelection,
        ) { hex ->
            applyBackgroundColor(editor, hex)
            onDismiss()
        }

        HorizontalDivider()

        DropdownMenuItem(
            text = { Text("清除前景色") },
            enabled = hasSelection,
            onClick = {
                editor.setValueMark(null) {
                    it.color != Color.Unspecified
                }
                onDismiss()
            },
        )

        DropdownMenuItem(
            text = { Text("清除背景色") },
            enabled = hasSelection,
            onClick = {
                editor.setValueMark(null) {
                    it.background != Color.Unspecified
                }
                onDismiss()
            },
        )
    }
}

@Composable
private fun ColorPaletteGroup(
    title: String,
    colors: List<ColorPreset>,
    enabled: Boolean,
    onSelect: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            colors.forEach { item ->
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clickable(enabled = enabled) {
                            onSelect(item.hex)
                        }
                        .semantics {
                            contentDescription = "$title ${item.name}"
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                Color(AndroidColor.parseColor(item.hex)),
                                CircleShape,
                            )
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant,
                                CircleShape,
                            ),
                    )
                }
            }
        }
    }
}

private fun applyTextColor(
    editor: RichTextEditorState,
    hex: String,
) {
    val color = Color(AndroidColor.parseColor(hex))

    editor.setValueMark(colorSpan(color)) {
        it.color != Color.Unspecified
    }
}

private fun applyBackgroundColor(
    editor: RichTextEditorState,
    hex: String,
) {
    val color = Color(AndroidColor.parseColor(hex))

    editor.setValueMark(highlightSpan(color)) {
        it.background != Color.Unspecified
    }
}
