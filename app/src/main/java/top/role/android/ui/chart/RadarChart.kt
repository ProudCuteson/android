package top.role.android.ui.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.sp
import top.role.android.core.model.chart.MobileChartRuntime
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

private data class RadarSeries(
    val key: String,
    val label: String,
    val values: Map<String, Double>,
)

@Composable
internal fun RadarChart(
    chart: MobileChartRuntime,
    modifier: Modifier = Modifier,
    display: MobileChartDisplay =
        MobileChartDisplay(),
) {
    val points =
        chart.categoricalPoints()

    if (points.isEmpty()) {
        UnsupportedChartContent(
            title = "暂无雷达图数据",
            modifier = modifier,
        )
        return
    }

    val categories =
        points
            .map {
                it.xLabel
            }
            .distinct()

    if (categories.size < 3) {
        UnsupportedChartContent(
            title = "雷达图至少需要 3 个维度",
            modifier = modifier,
        )
        return
    }

    val series =
        points
            .groupBy {
                it.seriesKey
            }
            .map { (key, values) ->
                RadarSeries(
                    key =
                        key,
                    label =
                        values
                            .firstOrNull()
                            ?.seriesLabel
                            ?: key,
                    values =
                        values.associate {
                            it.xLabel to
                                    it.yValue
                        },
                )
            }

    if (series.isEmpty()) {
        UnsupportedChartContent(
            title = "暂无雷达图数据",
            modifier = modifier,
        )
        return
    }

    val maxValue =
        points
            .maxOfOrNull {
                it.yValue
            }
            ?.takeIf {
                it.isFinite() &&
                        it > 0.0
            }
            ?: 1.0

    val theme =
        rememberMobileChartTheme()

    val density =
        LocalDensity.current

    val scale =
        display.scale
            .coerceAtLeast(
                0.01
            )

    val labelTextPx =
        with(density) {
            max(
                8.0,
                10.0 * scale,
            ).sp.toPx()
        }

    val legendItems =
        series.mapIndexed { index, item ->
            ChartLegendItem(
                key =
                    item.key,
                label =
                    item.label,
                color =
                    theme.seriesColor(
                        index
                    ),
            )
        }

    Column(
        modifier =
            modifier.fillMaxSize(),
    ) {
        Canvas(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxSize(),
        ) {
            if (
                size.width < 48f ||
                size.height < 48f
            ) {
                return@Canvas
            }

            drawRadarChart(
                categories =
                    categories,
                series =
                    series,
                maxValue =
                    maxValue,
                theme =
                    theme,
                labelTextPx =
                    labelTextPx,
                compact =
                    display.compact,
            )
        }

        if (
            display.showLegend &&
            chart.legendVisible &&
            legendItems.size > 1
        ) {
            ChartLegend(
                items =
                    legendItems,
                scale =
                    display.scale,
                compact =
                    display.compact,
            )
        }
    }
}

private fun DrawScope.drawRadarChart(
    categories: List<String>,
    series: List<RadarSeries>,
    maxValue: Double,
    theme: MobileChartTheme,
    labelTextPx: Float,
    compact: Boolean,
) {
    val count =
        categories.size

    if (count < 3) {
        return
    }

    /*
     * 给外围 category label 留一点空间。
     */
    val labelMargin =
        if (compact) {
            labelTextPx * 2.2f
        } else {
            labelTextPx * 2.8f
        }

    val radius =
        (
                min(
                    size.width,
                    size.height,
                ) /
                        2f -
                        labelMargin
                )
            .coerceAtLeast(
                4f
            )

    val center =
        Offset(
            x =
                size.width /
                        2f,
            y =
                size.height /
                        2f,
        )

    /*
     * 4 层 polygon grid：
     *
     * 25%
     * 50%
     * 75%
     * 100%
     */
    val gridLevels =
        4

    for (
    level in
    1..gridLevels
    ) {
        val ratio =
            level.toFloat() /
                    gridLevels.toFloat()

        val gridPath =
            buildRadarPolygon(
                center =
                    center,
                radius =
                    radius *
                            ratio,
                count =
                    count,
            )

        drawPath(
            path =
                gridPath,
            color =
                theme.grid,
            style =
                Stroke(
                    width =
                        1f,
                    join =
                        StrokeJoin.Round,
                ),
        )
    }

    /*
     * radial axes。
     */
    repeat(count) { index ->

        val end =
            radarPoint(
                center =
                    center,
                radius =
                    radius,
                index =
                    index,
                count =
                    count,
            )

        drawLine(
            color =
                theme.axis,
            start =
                center,
            end =
                end,
            strokeWidth =
                1f,
        )
    }

    /*
     * series polygon。
     */
    series.forEachIndexed { seriesIndex, item ->

        val color =
            theme.seriesColor(
                seriesIndex
            )

        val polygon =
            Path()

        categories
            .forEachIndexed { index, category ->

                val raw =
                    item.values[category]
                        ?: 0.0

                val ratio =
                    (
                            raw /
                                    maxValue
                            )
                        .coerceIn(
                            0.0,
                            1.0,
                        )

                val point =
                    radarPoint(
                        center =
                            center,
                        radius =
                            radius *
                                    ratio.toFloat(),
                        index =
                            index,
                        count =
                            count,
                    )

                if (index == 0) {
                    polygon.moveTo(
                        point.x,
                        point.y,
                    )
                } else {
                    polygon.lineTo(
                        point.x,
                        point.y,
                    )
                }
            }

        polygon.close()

        drawPath(
            path =
                polygon,
            color =
                color.copy(
                    alpha =
                        0.16f
                ),
            style =
                Fill,
        )

        drawPath(
            path =
                polygon,
            color =
                color,
            style =
                Stroke(
                    width =
                        2.2f,
                    join =
                        StrokeJoin.Round,
                ),
        )

        /*
         * 真实数据点。
         */
        categories
            .forEachIndexed { index, category ->

                val raw =
                    item.values[category]
                        ?: 0.0

                val ratio =
                    (
                            raw /
                                    maxValue
                            )
                        .coerceIn(
                            0.0,
                            1.0,
                        )

                val point =
                    radarPoint(
                        center =
                            center,
                        radius =
                            radius *
                                    ratio.toFloat(),
                        index =
                            index,
                        count =
                            count,
                    )

                drawCircle(
                    color =
                        color,
                    radius =
                        3f,
                    center =
                        point,
                )
            }
    }

    /*
     * category labels。
     */
    drawRadarLabels(
        categories =
            categories,
        center =
            center,
        radius =
            radius,
        color =
            theme.secondaryLabel,
        textSizePx =
            labelTextPx,
    )
}

private fun buildRadarPolygon(
    center: Offset,
    radius: Float,
    count: Int,
): Path {
    val path =
        Path()

    repeat(count) { index ->

        val point =
            radarPoint(
                center =
                    center,
                radius =
                    radius,
                index =
                    index,
                count =
                    count,
            )

        if (index == 0) {
            path.moveTo(
                point.x,
                point.y,
            )
        } else {
            path.lineTo(
                point.x,
                point.y,
            )
        }
    }

    path.close()

    return path
}

private fun radarPoint(
    center: Offset,
    radius: Float,
    index: Int,
    count: Int,
): Offset {
    /*
     * index 0 从正上方开始。
     */
    val angle =
        -PI /
                2.0 +
                (
                        index.toDouble() *
                                2.0 *
                                PI /
                                count.toDouble()
                        )

    return Offset(
        x =
            center.x +
                    cos(angle)
                        .toFloat() *
                    radius,

        y =
            center.y +
                    sin(angle)
                        .toFloat() *
                    radius,
    )
}

private fun DrawScope.drawRadarLabels(
    categories: List<String>,
    center: Offset,
    radius: Float,
    color: Color,
    textSizePx: Float,
) {
    val paint =
        android.graphics.Paint()
            .apply {
                isAntiAlias =
                    true

                this.color =
                    color.toRadarArgb()

                textSize =
                    textSizePx
            }

    val labelRadius =
        radius +
                textSizePx *
                1.25f

    categories.forEachIndexed { index, label ->

        val point =
            radarPoint(
                center =
                    center,
                radius =
                    labelRadius,
                index =
                    index,
                count =
                    categories.size,
            )

        val deltaX =
            point.x -
                    center.x

        paint.textAlign =
            when {
                deltaX >
                        textSizePx * 0.5f ->
                    android.graphics.Paint.Align.LEFT

                deltaX <
                        -textSizePx * 0.5f ->
                    android.graphics.Paint.Align.RIGHT

                else ->
                    android.graphics.Paint.Align.CENTER
            }

        val display =
            if (
                label.length > 10
            ) {
                label.take(9) +
                        "…"
            } else {
                label
            }

        /*
         * Android Canvas 的 drawText 用 baseline，
         * 所以略微向下校正。
         */
        drawContext.canvas
            .nativeCanvas
            .drawText(
                display,
                point.x,
                point.y +
                        textSizePx *
                        0.34f,
                paint,
            )
    }
}

private fun Color.toRadarArgb(): Int {
    return android.graphics.Color.argb(
        (alpha * 255f)
            .toInt()
            .coerceIn(
                0,
                255,
            ),

        (red * 255f)
            .toInt()
            .coerceIn(
                0,
                255,
            ),

        (green * 255f)
            .toInt()
            .coerceIn(
                0,
                255,
            ),

        (blue * 255f)
            .toInt()
            .coerceIn(
                0,
                255,
            ),
    )
}