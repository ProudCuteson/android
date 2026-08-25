package top.role.android.ui.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.InsertChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.role.android.core.model.chart.MobileChartRuntime
import top.role.android.core.model.chart.MobileChartType
import kotlin.math.max

@Immutable
data class MobileChartDisplay(

    /**
     * 是否显示图表标题。
     */
    val showTitle: Boolean = true,

    /**
     * 是否显示 description。
     */
    val showDescription: Boolean = true,

    /**
     * 是否允许显示 legend。
     *
     * 最终仍会受 runtime options.legend.enabled 影响。
     */
    val showLegend: Boolean = true,

    /**
     * Slide 一般 true。
     * Doc 一般 false。
     */
    val compact: Boolean = false,

    /**
     * Slide 使用 canvas scale。
     *
     * Doc 等普通流式布局可以保持 1。
     */
    val scale: Double = 1.0,

    /**
     * card / panel / transparent。
     *
     * Slide 可以由 element.backgroundMode 映射进来。
     */
    val backgroundMode: String? = null,
)

@Composable
fun MobileChart(
    chart: MobileChartRuntime,
    modifier: Modifier = Modifier,
    display: MobileChartDisplay =
        MobileChartDisplay(),
) {
    val density =
        LocalDensity.current

    val scale =
        display.scale
            .coerceAtLeast(
                0.01
            )

    /*
     * 与 Slide Text 一样：
     *
     * design px
     *     × scale
     *     ÷ density
     *     ÷ fontScale
     *     = Compose sp
     */
    val textScale =
        scale /
                density.density.toDouble() /
                density.fontScale.toDouble()

    val hasCardBackground =
        display.backgroundMode == "card" ||
                display.backgroundMode == "panel"

    val paddingDp =
        with(density) {
            if (hasCardBackground) {
                max(
                    4f,
                    (10.0 * scale)
                        .toFloat(),
                ).toDp()
            } else {
                0.dp
            }
        }

    val cornerRadiusDp =
        with(density) {
            max(
                6f,
                (10.0 * scale)
                    .toFloat(),
            ).toDp()
        }

    val borderWidthDp =
        with(density) {
            max(
                0.5f,
                scale.toFloat(),
            ).toDp()
        }

    val backgroundColor =
        resolveChartBackground(
            display.backgroundMode
        )

    val shape =
        RoundedCornerShape(
            cornerRadiusDp
        )

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .clip(
                    shape
                )
                .background(
                    backgroundColor
                )
                .let { base ->
                    if (hasCardBackground) {
                        base.border(
                            width =
                                borderWidthDp,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .outlineVariant
                                    .copy(
                                        alpha = 0.25f
                                    ),

                            shape =
                                shape,
                        )
                    } else {
                        base
                    }
                }
                .padding(
                    paddingDp
                ),

        verticalArrangement =
            Arrangement.spacedBy(
                with(density) {
                    max(
                        3f,
                        (6.0 * scale)
                            .toFloat(),
                    ).toDp()
                }
            ),
    ) {
        if (
            display.showTitle ||
            display.showDescription
        ) {
            MobileChartHeader(
                chart =
                    chart,

                display =
                    display,

                textScale =
                    textScale,
            )
        }

        Box(
            modifier =
                Modifier
                    .weight(
                        1f
                    )
                    .fillMaxSize(),
        ) {
            MobileChartBody(
                chart =
                    chart,

                display =
                    display,
            )
        }
    }
}

@Composable
private fun MobileChartHeader(
    chart: MobileChartRuntime,
    display: MobileChartDisplay,
    textScale: Double,
) {
    val showTitle =
        display.showTitle &&
                chart.title.isNotBlank()

    val description =
        chart.description
            ?.trim()
            ?.takeIf {
                it.isNotEmpty()
            }

    val showDescription =
        display.showDescription &&
                description != null

    if (
        !showTitle &&
        !showDescription
    ) {
        return
    }

    Column(
        verticalArrangement =
            Arrangement.spacedBy(
                1.dp
            ),
    ) {
        if (showTitle) {
            Text(
                text =
                    chart.title,

                fontSize =
                    max(
                        8.0,
                        15.0 * textScale,
                    ).sp,

                fontWeight =
                    FontWeight.SemiBold,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurface,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis,
            )
        }

        if (
            showDescription
        ) {
            Text(
                text =
                    description,

                fontSize =
                    max(
                        7.0,
                        11.0 * textScale,
                    ).sp,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun MobileChartBody(
    chart: MobileChartRuntime,
    display: MobileChartDisplay,
) {
    when (
        chart.chartType
    ) {
        MobileChartType.Bar -> {
            CartesianChart(
                chart =
                    chart,

                type =
                    CartesianChartType.Bar,

                modifier =
                    Modifier.fillMaxSize(),

                display =
                    display,
            )
        }

        MobileChartType.Line -> {
            CartesianChart(
                chart =
                    chart,

                type =
                    CartesianChartType.Line,

                modifier =
                    Modifier.fillMaxSize(),

                display =
                    display,
            )
        }

        MobileChartType.Area -> {
            CartesianChart(
                chart =
                    chart,

                type =
                    CartesianChartType.Area,

                modifier =
                    Modifier.fillMaxSize(),

                display =
                    display,
            )
        }

        MobileChartType.Scatter -> {
            ScatterChart(
                chart =
                    chart,

                modifier =
                    Modifier.fillMaxSize(),

                display =
                    display,
            )
        }

        MobileChartType.Pie -> {
            PieChart(
                chart =
                    chart,

                modifier =
                    Modifier.fillMaxSize(),

                display =
                    display,
            )
        }

        MobileChartType.Radar -> {
            RadarChart(
                chart =
                    chart,

                modifier =
                    Modifier.fillMaxSize(),

                display =
                    display,
            )
        }

        MobileChartType.Radial -> {
            RadialChart(
                chart =
                    chart,

                modifier =
                    Modifier.fillMaxSize(),

                display =
                    display,
            )
        }

        MobileChartType.Composed -> {
            CartesianChart(
                chart =
                    chart,

                type =
                    CartesianChartType.Composed,

                modifier =
                    Modifier.fillMaxSize(),

                display =
                    display,
            )
        }
    }
}

@Composable
internal fun UnsupportedChartContent(
    title: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(
                    MaterialTheme
                        .colorScheme
                        .surfaceVariant
                        .copy(
                            alpha = 0.65f
                        )
                ),

        contentAlignment =
            Alignment.Center,
    ) {
        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally,

            verticalArrangement =
                Arrangement.spacedBy(
                    4.dp
                ),
        ) {
            Icon(
                imageVector =
                    Icons.Outlined.InsertChart,

                contentDescription =
                    null,

                tint =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant,
            )

            Text(
                text =
                    title,

                style =
                    MaterialTheme
                        .typography
                        .labelSmall,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant,

                textAlign =
                    TextAlign.Center,

                maxLines =
                    2,
            )
        }
    }
}

@Composable
private fun resolveChartBackground(
    backgroundMode: String?,
): Color {
    return when (
        backgroundMode
            ?.trim()
            ?.lowercase()
    ) {
        "card" ->
            MaterialTheme
                .colorScheme
                .background
                .copy(
                    alpha = 0.96f
                )

        "panel" ->
            MaterialTheme
                .colorScheme
                .surfaceVariant
                .copy(
                    alpha = 0.96f
                )

        else ->
            Color.Transparent
    }
}