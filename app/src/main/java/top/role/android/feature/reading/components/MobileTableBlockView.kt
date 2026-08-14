package top.role.android.feature.reading.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.role.android.core.model.doc.MobileTableBlock
import top.role.android.core.model.doc.MobileTableCell
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxHeight

private data class TablePlacedCell(
    val cell: MobileTableCell,
    val rowIndex: Int,
    val colIndex: Int,
    val rowSpan: Int,
    val colSpan: Int,
)

private data class TableGridInfo(
    val cells: List<TablePlacedCell>,
    val rowCount: Int,
    val columnCount: Int,
)

@Composable
fun MobileTableBlockView(
    block: MobileTableBlock,
    modifier: Modifier = Modifier,
) {
    val grid =
        buildTableGridInfo(
            block = block,
        )

    if (
        grid.rowCount <= 0 ||
        grid.columnCount <= 0
    ) {
        return
    }

    val columnWidths =
        resolveColumnWidths(
            block = block,
            columnCount =
                grid.columnCount,
        )

    val tableWidth =
        columnWidths.fold(
            0.dp
        ) { total, width ->
            total + width
        }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(
                rememberScrollState()
            ),
    ) {
        Column(
            modifier =
                Modifier.width(
                    tableWidth
                ),
        ) {
            repeat(
                grid.rowCount
            ) { rowIndex ->

                Row(
                    modifier = Modifier
                        .height(IntrinsicSize.Min),
                ) {
                    var currentColumn = 0

                    while (
                        currentColumn <
                        grid.columnCount
                    ) {
                        val placed =
                            grid.cells
                                .firstOrNull {
                                    it.rowIndex == rowIndex &&
                                            it.colIndex == currentColumn
                                }

                        if (placed != null) {
                            val width =
                                spanWidth(
                                    columnWidths =
                                        columnWidths,
                                    start =
                                        placed.colIndex,
                                    span =
                                        placed.colSpan,
                                )

                            TableCellView(
                                cell = placed.cell,
                                width = width,
                            )

                            currentColumn +=
                                placed.colSpan
                        } else {
                            val covered =
                                grid.cells.any {
                                    rowIndex >= it.rowIndex &&
                                            rowIndex <
                                            it.rowIndex +
                                            it.rowSpan &&
                                            currentColumn >=
                                            it.colIndex &&
                                            currentColumn <
                                            it.colIndex +
                                            it.colSpan
                                }

                            if (covered) {
                                currentColumn += 1
                            } else {
                                TableCellView(
                                    cell =
                                        MobileTableCell(
                                            text = "",
                                        ),
                                    width =
                                        columnWidths[
                                            currentColumn
                                        ],
                                )

                                currentColumn += 1
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TableCellView(
    cell: MobileTableCell,
    width: Dp,
) {
    val textAlign =
        when (
            cell.align
        ) {
            "center" ->
                TextAlign.Center

            "right" ->
                TextAlign.Right

            else ->
                TextAlign.Left
        }

    val horizontalAlignment =
        when (
            cell.align
        ) {
            "center" ->
                Alignment.CenterHorizontally

            "right" ->
                Alignment.End

            else ->
                Alignment.Start
        }

    Column(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .background(
                MaterialTheme
                    .colorScheme
                    .surfaceContainerLowest
            )
            .padding(
                horizontal = 10.dp,
                vertical = 8.dp,
            ),
        horizontalAlignment =
            horizontalAlignment,
    ) {
        if (
            !cell.inlines.isNullOrEmpty()
        ) {
            MobileInlineText(
                inlines =
                    cell.inlines,
                modifier =
                    Modifier.fillMaxWidth(),
                baseStyle =
                    MaterialTheme
                        .typography
                        .bodySmall,
                textAlign =
                    textAlign,
            )
        } else {
            Text(
                text =
                    cell.text,
                modifier =
                    Modifier.fillMaxWidth(),
                style =
                    MaterialTheme
                        .typography
                        .bodySmall,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurface,
                textAlign =
                    textAlign,
            )
        }
    }
}

private fun buildTableGridInfo(
    block: MobileTableBlock,
): TableGridInfo {
    val occupied =
        mutableListOf<
                MutableList<Boolean>
                >()

    val cells =
        mutableListOf<
                TablePlacedCell
                >()

    var columnCount = 0

    block.rows
        .forEachIndexed {
                rowIndex,
                row,
            ->

            while (
                occupied.size <=
                rowIndex
            ) {
                occupied.add(
                    mutableListOf()
                )
            }

            row.cells
                .forEach { cell ->

                    val rowSpan =
                        cell.rowSpan
                            ?.toInt()
                            ?.coerceAtLeast(1)
                            ?: 1

                    val colSpan =
                        cell.colSpan
                            ?.toInt()
                            ?.coerceAtLeast(1)
                            ?: 1

                    val colIndex =
                        firstFreeColumn(
                            occupied[
                                rowIndex
                            ]
                        )

                    markOccupied(
                        occupied =
                            occupied,
                        rowStart =
                            rowIndex,
                        colStart =
                            colIndex,
                        rowSpan =
                            rowSpan,
                        colSpan =
                            colSpan,
                    )

                    cells.add(
                        TablePlacedCell(
                            cell = cell,
                            rowIndex =
                                rowIndex,
                            colIndex =
                                colIndex,
                            rowSpan =
                                rowSpan,
                            colSpan =
                                colSpan,
                        )
                    )

                    columnCount =
                        maxOf(
                            columnCount,
                            colIndex +
                                    colSpan,
                        )
                }
        }

    return TableGridInfo(
        cells = cells,
        rowCount =
            maxOf(
                block.rows.size,
                occupied.size,
            ),
        columnCount =
            columnCount,
    )
}

private fun firstFreeColumn(
    row: List<Boolean>,
): Int {
    var index = 0

    while (
        index < row.size &&
        row[index]
    ) {
        index += 1
    }

    return index
}

private fun markOccupied(
    occupied:
    MutableList<
            MutableList<Boolean>
            >,
    rowStart: Int,
    colStart: Int,
    rowSpan: Int,
    colSpan: Int,
) {
    for (
    row in
    rowStart until
            rowStart + rowSpan
    ) {
        while (
            occupied.size <= row
        ) {
            occupied.add(
                mutableListOf()
            )
        }

        val current =
            occupied[row]

        while (
            current.size <
            colStart + colSpan
        ) {
            current.add(
                false
            )
        }

        for (
        col in
        colStart until
                colStart + colSpan
        ) {
            current[col] = true
        }
    }
}

private fun resolveColumnWidths(
    block: MobileTableBlock,
    columnCount: Int,
): List<Dp> {
    if (
        columnCount <= 0
    ) {
        return emptyList()
    }

    val backend =
        block.columnWidths
            ?.takeIf {
                it.size >=
                        columnCount
            }
            ?.take(
                columnCount
            )
            ?.map {
                it.coerceAtLeast(
                    1.0
                )
            }

    if (
        backend != null
    ) {
        val total =
            backend.sum()
                .takeIf {
                    it > 0
                }
                ?: columnCount
                    .toDouble()

        val targetWidth =
            maxOf(
                320.0,
                columnCount *
                        110.0,
            )

        return backend.map {
            maxOf(
                96.0,
                targetWidth *
                        (it / total),
            ).dp
        }
    }

    return List(
        columnCount
    ) {
        110.dp
    }
}

private fun spanWidth(
    columnWidths:
    List<Dp>,
    start: Int,
    span: Int,
): Dp {
    var total =
        0.dp

    for (
    index in
    start until
            start + span
    ) {
        total +=
            columnWidths
                .getOrElse(
                    index
                ) {
                    110.dp
                }
    }

    return total
}