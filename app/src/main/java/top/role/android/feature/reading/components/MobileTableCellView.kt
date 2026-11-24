package top.role.android.feature.reading.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import top.role.android.core.model.content.MobileTableCell
import top.role.android.core.model.content.MobileTableLine

@Composable
internal fun MobileTableCellView(
    cell: MobileTableCell?,
    isHeader: Boolean,
    modifier: Modifier = Modifier,
) {
    val background =
        if (isHeader) {
            MaterialTheme.colorScheme
                .surfaceContainerLow
        } else {
            MaterialTheme.colorScheme
                .surfaceContainerLowest
        }

    val baseStyle =
        MaterialTheme.typography.bodySmall.copy(
            color =
                MaterialTheme.colorScheme.onSurface,

            fontWeight =
                if (isHeader) {
                    FontWeight.SemiBold
                } else {
                    FontWeight.Normal
                },
        )

    val verticalAlignment =
        when (cell?.verticalAlign) {
            "middle" -> Alignment.CenterStart
            "bottom" -> Alignment.BottomStart
            else -> Alignment.TopStart
        }

    Box(
        modifier = modifier
            .background(background)
            .padding(
                horizontal = 10.dp,
                vertical = 8.dp,
            ),

        contentAlignment = verticalAlignment,
    ) {

        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {

            if (cell == null || cell.lines.isEmpty()) {

                Text(
                    text = " ",
                    style = baseStyle,
                )

            } else {

                cell.lines.forEachIndexed { index, line ->

                    key(line.id) {

                        MobileTableLineView(
                            line = line,
                            cellAlign = cell.align,
                            baseStyle = baseStyle,
                            lineIndex = index,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MobileTableLineView(
    line: MobileTableLine,
    cellAlign: String?,
    baseStyle: TextStyle,
    lineIndex: Int,
) {
    val style =
        resolveTableLineStyle(
            line = line,
            baseStyle = baseStyle,
        )

    val textAlign =
        resolveMobileTextAlign(
            line.align ?: cellAlign
        )

    val list = line.list

    if (list == null) {

        if (
            line.inlines.isEmpty() ||
            line.inlines.all {
                it.text.isEmpty()
            }
        ) {
            Text(
                text = " ",
                modifier = Modifier.fillMaxWidth(),
                style = style,
            )
        } else {
            MobileInlineText(
                inlines = line.inlines,
                modifier = Modifier.fillMaxWidth(),
                baseStyle = style,
                textAlign = textAlign,
            )
        }

        return
    }

    /*
     * 单元格内部的列表。
     */
    val indent = (list.indent?.toInt() ?: 0).coerceIn(0, 8)

    val marker =
        when (list.type) {

            "numbered" ->
                "${list.start?.toInt() ?: (lineIndex + 1)}."

            "checkbox" ->
                if (list.checked == true) {
                    "☑"
                } else {
                    "☐"
                }

            else -> "•"
        }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = (indent * 12).dp
            ),

        verticalAlignment = Alignment.Top,
    ) {

        Text(
            text = marker,
            modifier = Modifier
                .widthIn(min = 18.dp)
                .padding(end = 4.dp),

            style = style,
        )

        if (line.inlines.isEmpty()) {

            Text(
                text = " ",
                modifier = Modifier.weight(1f),
                style = style,
            )

        } else {

            MobileInlineText(
                inlines = line.inlines,
                modifier = Modifier.weight(1f),
                baseStyle = style,
                textAlign = textAlign,
            )
        }
    }
}

private fun resolveTableLineStyle(
    line: MobileTableLine,
    baseStyle: TextStyle,
): TextStyle {

    val variantStyle =
        when (line.variant) {

            "title",
            "h1" -> baseStyle.copy(
                fontSize = 15.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Bold,
            )

            "h2",
            "h3" -> baseStyle.copy(
                fontSize = 14.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.SemiBold,
            )

            "small",
            "caption" -> baseStyle.copy(
                fontSize = 11.sp,
                lineHeight = 17.sp,
            )

            else -> baseStyle
        }

    return variantStyle
}