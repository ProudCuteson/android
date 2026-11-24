package top.role.android.feature.reading.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import top.role.android.core.model.content.MobileTableBlock

@Composable
fun MobileTableBlockView(
    block: MobileTableBlock,
    modifier: Modifier = Modifier,
) {
    if (
        block.rows <= 0 ||
        block.cols <= 0
    ) {
        return
    }

    val cells = remember(block) {
        buildTablePlacedCells(block)
    }

    val rowHeights = remember(block) {
        resolveTableRowHeights(block)
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
    ) {

        val viewportWidth =
            if (maxWidth.value.isFinite()) {
                maxWidth
            } else {
                320.dp
            }

        val columnWidths =
            remember(block, viewportWidth) {
                resolveTableColumnWidths(
                    block = block,
                    viewportWidth = viewportWidth,
                )
            }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(
                    rememberScrollState()
                ),
        ) {

            MobileTableGridLayout(
                cells = cells,
                columnWidths = columnWidths,
                rowHeights = rowHeights,
            ) {

                cells.forEach { placed ->

                    key(
                        placed.cell?.id
                            ?: "empty:${placed.row}:${placed.col}"
                    ) {

                        val isHeader =
                            (
                                    block.header?.top == true &&
                                            placed.row == 0
                                    ) ||
                                    (
                                            block.header?.left == true &&
                                                    placed.col == 0
                                            )

                        MobileTableCellView(
                            cell = placed.cell,
                            isHeader = isHeader,
                        )
                    }
                }
            }
        }
    }
}