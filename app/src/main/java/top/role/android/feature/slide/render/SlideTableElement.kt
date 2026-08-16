package top.role.android.feature.slide.render

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import top.role.android.core.model.slide.MobileSlideElement
import top.role.android.core.model.slide.MobileSlideTableCell
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun SlideTableElement(
    element: MobileSlideElement,
    scale: Double,
    modifier: Modifier = Modifier,
) {
    val rows =
        max(
            0,
            (element.rows ?: 0.0).toInt(),
        )

    val cols =
        max(
            0,
            (element.cols ?: 0.0).toInt(),
        )

    if (rows <= 0 || cols <= 0) {
        return
    }

    val totalW =
        max(
            0.0,
            element.rect.w,
        )

    val totalH =
        max(
            0.0,
            element.rect.h,
        )

    val columnWidths =
        normalizeToTotal(
            values =
                ensureColumnWidths(
                    columnWidths =
                        element.columnWidths,
                    cols =
                        cols,
                    totalW =
                        totalW,
                ),
            total =
                totalW,
        )

    val rowHeights =
        normalizeToTotal(
            values =
                ensureRowHeights(
                    rowHeights =
                        element.rowHeights,
                    rows =
                        rows,
                    totalH =
                        totalH,
                ),
            total =
                totalH,
        )

    val cells =
        element.cells
            .orEmpty()
            .values
            .sortedWith(
                compareBy<MobileSlideTableCell> {
                    it.anchorRow
                }.thenBy {
                    it.anchorCol
                }.thenBy {
                    it.id
                }
            )

    val density =
        LocalDensity.current

    val borderWidthDp =
        with(density) {
            max(
                0.5f,
                scale.toFloat(),
            ).toDp()
        }

    val shape =
        RoundedCornerShape(
            6.dp
        )

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(shape)
            .background(
                MaterialTheme.colorScheme.background
                    .copy(alpha = 0.96f)
            )
            .border(
                width =
                    borderWidthDp,
                color =
                    MaterialTheme.colorScheme.outlineVariant
                        .copy(alpha = 0.45f),
                shape =
                    shape,
            ),
    ) {
        Layout(
            modifier =
                Modifier.fillMaxSize(),
            content = {
                cells.forEach { cell ->
                    SlideTableCell(
                        cell = cell,
                        header = element.header,
                        scale = scale,
                    )
                }
            },
        ) { measurables, constraints ->

            val viewW =
                constraints.maxWidth

            val viewH =
                constraints.maxHeight

            val sx =
                if (totalW > 0.0) {
                    viewW.toDouble() /
                            totalW
                } else {
                    scale
                }

            val sy =
                if (totalH > 0.0) {
                    viewH.toDouble() /
                            totalH
                } else {
                    scale
                }

            val placeables =
                measurables.mapIndexed { index, measurable ->
                    val cell =
                        cells[index]

                    val anchorRow =
                        cell.anchorRow
                            .toInt()
                            .coerceAtLeast(0)

                    val anchorCol =
                        cell.anchorCol
                            .toInt()
                            .coerceAtLeast(0)

                    val rowSpan =
                        (cell.rowSpan ?: 1.0)
                            .toInt()
                            .coerceAtLeast(1)

                    val colSpan =
                        (cell.colSpan ?: 1.0)
                            .toInt()
                            .coerceAtLeast(1)

                    val originalX =
                        sumBefore(
                            columnWidths,
                            anchorCol,
                        )

                    val originalY =
                        sumBefore(
                            rowHeights,
                            anchorRow,
                        )

                    val originalCellW =
                        sumRange(
                            columnWidths,
                            start =
                                anchorCol,
                            count =
                                colSpan,
                        )

                    val originalCellH =
                        sumRange(
                            rowHeights,
                            start =
                                anchorRow,
                            count =
                                rowSpan,
                        )

                    val x =
                        (originalX * sx)
                            .roundToInt()

                    val y =
                        (originalY * sy)
                            .roundToInt()

                    val w =
                        (originalCellW * sx)
                            .roundToInt()
                            .coerceAtLeast(1)

                    val h =
                        (originalCellH * sy)
                            .roundToInt()
                            .coerceAtLeast(1)

                    val placeable =
                        measurable.measure(
                            Constraints.fixed(
                                width =
                                    w,
                                height =
                                    h,
                            )
                        )

                    PositionedCell(
                        placeable =
                            placeable,
                        x =
                            x,
                        y =
                            y,
                    )
                }

            layout(
                width =
                    viewW,
                height =
                    viewH,
            ) {
                placeables.forEach {
                    it.placeable.place(
                        x = it.x,
                        y = it.y,
                    )
                }
            }
        }
    }
}

private data class PositionedCell(
    val placeable:
    androidx.compose.ui.layout.Placeable,
    val x: Int,
    val y: Int,
)

private fun ensureColumnWidths(
    columnWidths: List<Double>?,
    cols: Int,
    totalW: Double,
): List<Double> {
    if (
        columnWidths != null &&
        columnWidths.size == cols
    ) {
        return columnWidths.map {
            max(
                0.0,
                it,
            )
        }
    }

    if (cols <= 0) {
        return emptyList()
    }

    if (totalW <= 0.0) {
        return List(cols) {
            120.0
        }
    }

    val base =
        floor(
            totalW /
                    cols.toDouble()
        )

    val remainder =
        (
                totalW -
                        base *
                        cols.toDouble()
                )
            .toInt()

    return List(cols) { index ->
        base +
                if (index < remainder) {
                    1.0
                } else {
                    0.0
                }
    }
}

private fun ensureRowHeights(
    rowHeights: List<Double>?,
    rows: Int,
    totalH: Double,
): List<Double> {
    if (
        rowHeights != null &&
        rowHeights.size == rows
    ) {
        return rowHeights.map {
            max(
                0.0,
                it,
            )
        }
    }

    if (rows <= 0) {
        return emptyList()
    }

    if (totalH <= 0.0) {
        return List(rows) {
            32.0
        }
    }

    val base =
        floor(
            totalH /
                    rows.toDouble()
        )

    val remainder =
        (
                totalH -
                        base *
                        rows.toDouble()
                )
            .toInt()

    return List(rows) { index ->
        base +
                if (index < remainder) {
                    1.0
                } else {
                    0.0
                }
    }
}

private fun normalizeToTotal(
    values: List<Double>,
    total: Double,
): List<Double> {
    if (values.isEmpty()) {
        return emptyList()
    }

    if (total <= 0.0) {
        return values
    }

    val sum =
        values.sum()

    if (sum <= 0.0) {
        return values
    }

    val factor =
        total /
                sum

    return values.map {
        it * factor
    }
}

private fun sumBefore(
    values: List<Double>,
    index: Int,
): Double {
    if (index <= 0) {
        return 0.0
    }

    return values
        .take(
            minOf(
                index,
                values.size,
            )
        )
        .sum()
}

private fun sumRange(
    values: List<Double>,
    start: Int,
    count: Int,
): Double {
    if (
        start < 0 ||
        start >= values.size ||
        count <= 0
    ) {
        return 0.0
    }

    val end =
        minOf(
            values.size,
            start + count,
        )

    return values
        .subList(
            start,
            end,
        )
        .sum()
}