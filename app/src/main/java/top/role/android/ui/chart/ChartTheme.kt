package top.role.android.ui.chart

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
internal data class MobileChartTheme(
    val background: Color,
    val axis: Color,
    val grid: Color,
    val label: Color,
    val secondaryLabel: Color,
    val series: List<Color>,
)

@Composable
internal fun rememberMobileChartTheme(): MobileChartTheme {
    return MobileChartTheme(
        background =
            MaterialTheme.colorScheme.background,

        axis =
            MaterialTheme.colorScheme.outlineVariant,

        grid =
            MaterialTheme.colorScheme.outlineVariant
                .copy(alpha = 0.35f),

        label =
            MaterialTheme.colorScheme.onSurface,

        secondaryLabel =
            MaterialTheme.colorScheme.onSurfaceVariant,

        series =
            listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.tertiary,
                MaterialTheme.colorScheme.secondary,
                MaterialTheme.colorScheme.error,
                MaterialTheme.colorScheme.primaryContainer,
                MaterialTheme.colorScheme.tertiaryContainer,
                MaterialTheme.colorScheme.secondaryContainer,
            ),
    )
}

internal fun MobileChartTheme.seriesColor(
    index: Int,
): Color {
    if (series.isEmpty()) {
        return Color.Gray
    }

    return series[
        index.mod(
            series.size
        )
    ]
}