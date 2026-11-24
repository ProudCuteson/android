package top.role.android.feature.reading.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.constrainHeight

import top.role.android.core.model.content.MobileTableBlock
import top.role.android.core.model.content.MobileTableCell

internal data class TablePlacedCell(
    val cell: MobileTableCell?,
    val row: Int,
    val col: Int,
    val rowSpan: Int,
    val colSpan: Int,
)

internal fun buildTablePlacedCells(
    block: MobileTableBlock,
): List<TablePlacedCell> {

    val rowCount = block.rows.toInt()
    val columnCount = block.cols.toInt()

    if (rowCount <= 0 || columnCount <= 0) {
        return emptyList()
    }

    val occupied = Array(rowCount) {
        BooleanArray(columnCount)
    }

    val result = mutableListOf<TablePlacedCell>()

    block.cells
        .sortedWith(
            compareBy<MobileTableCell> { it.row }
                .thenBy { it.col }
        )
        .forEach { cell ->

            val row = cell.row.toInt()
            val col = cell.col.toInt()

            if (
                row !in 0 until rowCount ||
                col !in 0 until columnCount
            ) {
                return@forEach
            }

            val rowSpan =
                cell.rowSpan
                    .toInt()
                    .coerceIn(
                        1,
                        rowCount - row,
                    )

            val colSpan =
                cell.colSpan
                    .toInt()
                    .coerceIn(
                        1,
                        columnCount - col,
                    )

            val overlaps =
                (row until row + rowSpan).any { r ->
                    (col until col + colSpan).any { c ->
                        occupied[r][c]
                    }
                }

            if (overlaps) {
                return@forEach
            }

            result.add(
                TablePlacedCell(
                    cell = cell,
                    row = row,
                    col = col,
                    rowSpan = rowSpan,
                    colSpan = colSpan,
                )
            )

            for (r in row until row + rowSpan) {
                for (c in col until col + colSpan) {
                    occupied[r][c] = true
                }
            }
        }

    /*
     * 补充未被覆盖的空白单元格。
     */
    for (row in 0 until rowCount) {
        for (col in 0 until columnCount) {

            if (!occupied[row][col]) {
                result.add(
                    TablePlacedCell(
                        cell = null,
                        row = row,
                        col = col,
                        rowSpan = 1,
                        colSpan = 1,
                    )
                )
            }
        }
    }

    return result.sortedWith(
        compareBy<TablePlacedCell> { it.row }
            .thenBy { it.col }
    )
}

/**
 * 保留后端列宽比例。
 *
 * 没有有效列宽时才采用等宽布局。
 */
internal fun resolveTableColumnWidths(
    block: MobileTableBlock,
    viewportWidth: Dp,
): List<Dp> {

    val count = block.cols.toInt()

    if (count <= 0) {
        return emptyList()
    }

    val widthPct =
        block.tableWidthPct
            ?.takeIf {
                it.isFinite() && it > 0
            }
            ?.coerceAtMost(100.0)
            ?: 100.0

    val requestedWidth =
        viewportWidth.value.toDouble() *
                widthPct / 100.0

    val minTableWidth =
        block.minTableWidth
            ?.takeIf {
                it.isFinite() && it > 0
            }
            ?: 0.0

    val targetWidth =
        maxOf(
            requestedWidth,
            minTableWidth,
            count * 110.0,
        )

    val weights =
        block.columnWidths
            ?.takeIf { it.size >= count }
            ?.take(count)
            ?.map {
                if (it.isFinite() && it > 0) {
                    it
                } else {
                    110.0
                }
            }
            ?: List(count) { 1.0 }

    val totalWeight = weights.sum()

    return weights.map { weight ->

        maxOf(
            96.0,
            targetWidth * weight / totalWeight,
        ).dp
    }
}

/**
 * 基础行高。
 *
 * 后端高度作为最小值，
 * 最终高度仍由单元格实际内容决定。
 */
internal fun resolveTableRowHeights(
    block: MobileTableBlock,
): List<Dp> {

    val rowCount = block.rows.toInt()

    if (rowCount <= 0) {
        return emptyList()
    }

    val minHeight =
        block.minRowHeight
            ?.takeIf {
                it.isFinite() && it > 0
            }
            ?: 36.0

    return List(rowCount) { row ->

        val configured =
            block.rowHeights
                ?.getOrNull(row)
                ?.takeIf {
                    it.isFinite() && it > 0
                }
                ?: 0.0

        maxOf(
            36.0,
            minHeight,
            configured,
        ).dp
    }
}

/**
 * 支持 rowSpan / colSpan 的实际布局。
 *
 * 此处已经是纯 Android 布局计算，
 * 所有像素尺寸及索引继续使用 Int。
 */
@Composable
internal fun MobileTableGridLayout(
    cells: List<TablePlacedCell>,
    columnWidths: List<Dp>,
    rowHeights: List<Dp>,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Layout(
        modifier = modifier,
        content = content,
    ) { measurables, constraints ->

        val widths =
            columnWidths.map {
                it.roundToPx().coerceAtLeast(1)
            }

        val heights =
            rowHeights.map {
                it.roundToPx().coerceAtLeast(1)
            }.toMutableList()

        fun cellWidth(
            placed: TablePlacedCell,
        ): Int {
            return (
                    placed.col until
                            placed.col + placed.colSpan
                    ).sumOf { widths[it] }
        }

        fun cellHeight(
            placed: TablePlacedCell,
        ): Int {
            return (
                    placed.row until
                            placed.row + placed.rowSpan
                    ).sumOf { heights[it] }
        }

        /*
         * 第一阶段：
         * 计算普通单元格所需行高。
         */
        cells.forEachIndexed { index, placed ->

            if (placed.rowSpan == 1) {

                val requiredHeight =
                    measurables[index]
                        .minIntrinsicHeight(
                            cellWidth(placed)
                        )

                heights[placed.row] =
                    maxOf(
                        heights[placed.row],
                        requiredHeight,
                    )
            }
        }

        /*
         * 第二阶段：
         * 确保跨行单元格拥有足够高度。
         */
        cells.forEachIndexed { index, placed ->

            if (placed.rowSpan > 1) {

                val requiredHeight =
                    measurables[index]
                        .minIntrinsicHeight(
                            cellWidth(placed)
                        )

                val availableHeight =
                    cellHeight(placed)

                val extra =
                    (requiredHeight - availableHeight)
                        .coerceAtLeast(0)

                if (extra > 0) {

                    val each =
                        extra / placed.rowSpan

                    val remainder =
                        extra % placed.rowSpan

                    repeat(placed.rowSpan) { offset ->

                        heights[placed.row + offset] +=
                            each +
                                    if (offset < remainder) {
                                        1
                                    } else {
                                        0
                                    }
                    }
                }
            }
        }

        /*
         * 第三阶段：
         * 使用最终确定的宽度和高度测量。
         */
        val placeables =
            measurables.mapIndexed { index, measurable ->

                val placed = cells[index]

                measurable.measure(
                    Constraints.fixed(
                        width = cellWidth(placed),
                        height = cellHeight(placed),
                    )
                )
            }

        /*
         * 计算每一列的起始位置。
         */
        val columnOffsets =
            IntArray(widths.size + 1)

        widths.forEachIndexed { index, width ->

            columnOffsets[index + 1] =
                columnOffsets[index] + width
        }

        /*
         * 计算每一行的起始位置。
         */
        val rowOffsets =
            IntArray(heights.size + 1)

        heights.forEachIndexed { index, height ->

            rowOffsets[index + 1] =
                rowOffsets[index] + height
        }

        /*
         * 最终放置单元格。
         */
        layout(
            width = constraints.constrainWidth(
                columnOffsets.last()
            ),
            height = constraints.constrainHeight(
                rowOffsets.last()
            ),
        ) {

            placeables.forEachIndexed { index, placeable ->

                val placed = cells[index]

                placeable.place(
                    x = columnOffsets[placed.col],
                    y = rowOffsets[placed.row],
                )
            }
        }
    }
}