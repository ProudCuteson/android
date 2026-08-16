package top.role.android.ui.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max

internal data class ChartLegendItem(
    val key: String,
    val label: String,
    val color: Color,
)

@Composable
internal fun ChartLegend(
    items: List<ChartLegendItem>,
    scale: Double,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) {
        return
    }

    val density =
        LocalDensity.current

    val safeScale =
        scale.coerceAtLeast(
            0.01
        )

    val textScale =
        safeScale /
                density.density.toDouble() /
                density.fontScale.toDouble()

    val fontSize =
        max(
            8.0,
            10.0 * textScale,
        ).sp

    val markerSize =
        with(density) {
            max(
                6f,
                (8.0 * safeScale)
                    .toFloat(),
            ).toDp()
        }

    val horizontalGap =
        with(density) {
            max(
                6f,
                (10.0 * safeScale)
                    .toFloat(),
            ).toDp()
        }

    val verticalGap =
        with(density) {
            max(
                2f,
                (4.0 * safeScale)
                    .toFloat(),
            ).toDp()
        }

    if (compact) {
        FlowRow(
            modifier =
                modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(
                    horizontalGap,
                    Alignment.CenterHorizontally,
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    verticalGap
                ),
        ) {
            items.forEach { item ->
                ChartLegendRow(
                    item = item,
                    markerSize = markerSize,
                    fontSize = fontSize,
                )
            }
        }
    } else {
        FlowRow(
            modifier =
                modifier,
            horizontalArrangement =
                Arrangement.spacedBy(
                    horizontalGap
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    verticalGap
                ),
        ) {
            items.forEach { item ->
                ChartLegendRow(
                    item = item,
                    markerSize = markerSize,
                    fontSize = fontSize,
                )
            }
        }
    }
}

@Composable
private fun ChartLegendRow(
    item: ChartLegendItem,
    markerSize:
    androidx.compose.ui.unit.Dp,
    fontSize:
    androidx.compose.ui.unit.TextUnit,
) {
    Row(
        verticalAlignment =
            Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(
                        markerSize
                    )
                    .background(
                        color =
                            item.color,
                        shape =
                            CircleShape,
                    ),
        )

        Spacer(
            modifier =
                Modifier.width(
                    4.dp
                )
        )

        Text(
            text =
                item.label,
            fontSize =
                fontSize,
            color =
                MaterialTheme.colorScheme
                    .onSurfaceVariant,
            maxLines =
                1,
            overflow =
                TextOverflow.Ellipsis,
        )
    }
}