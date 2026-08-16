package top.role.android.ui.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.sp
import top.role.android.core.model.chart.MobileChartRuntime
import java.util.Locale
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

@Composable
internal fun ScatterChart(
    chart: MobileChartRuntime,
    modifier: Modifier = Modifier,
    display: MobileChartDisplay =
        MobileChartDisplay(),
) {
    val points =
        chart.scatterPoints()

    if (points.isEmpty()) {
        UnsupportedChartContent(
            title = "散点图数据格式不适合渲染",
            modifier = modifier,
        )
        return
    }

    val xRange =
        calculateScatterRange(
            points.map {
                it.xValue
            }
        )

    val yRange =
        calculateScatterRange(
            points.map {
                it.yValue
            }
        )

    val density =
        LocalDensity.current

    val scale =
        display.scale
            .coerceAtLeast(0.01)

    val labelTextPx =
        with(density) {
            max(
                8.0,
                10.0 * scale,
            ).sp.toPx()
        }

    val pointRadius =
        max(
            2.5f,
            (
                    chart.scatterDotSize *
                            scale
                    )
                .toFloat() /
                    2f,
        )

    val theme =
        rememberMobileChartTheme()

    val axisColor =
        theme.axis

    val gridColor =
        theme.grid

    val labelColor =
        theme.secondaryLabel

    val pointColor =
        theme.seriesColor(0)

    Box(
        modifier =
            modifier.fillMaxSize(),
    ) {
        Canvas(
            modifier =
                Modifier.fillMaxSize(),
        ) {
            if (
                size.width < 32f ||
                size.height < 32f
            ) {
                return@Canvas
            }

            val geometry =
                createScatterGeometry(
                    width =
                        size.width,
                    height =
                        size.height,
                    xMin =
                        xRange.first,
                    xMax =
                        xRange.second,
                    yMin =
                        yRange.first,
                    yMax =
                        yRange.second,
                    labelTextPx =
                        labelTextPx,
                    compact =
                        display.compact,
                )

            drawScatterGrid(
                geometry =
                    geometry,
                axisColor =
                    axisColor,
                gridColor =
                    gridColor,
            )

            drawScatterYAxisLabels(
                geometry =
                    geometry,
                color =
                    labelColor,
                textSizePx =
                    labelTextPx,
            )

            drawScatterXAxisLabels(
                geometry =
                    geometry,
                color =
                    labelColor,
                textSizePx =
                    labelTextPx,
            )

            points.forEach { point ->

                val x =
                    scatterX(
                        geometry =
                            geometry,
                        value =
                            point.xValue,
                    )

                val y =
                    scatterY(
                        geometry =
                            geometry,
                        value =
                            point.yValue,
                    )

                drawCircle(
                    color =
                        pointColor,
                    radius =
                        pointRadius,
                    center =
                        Offset(
                            x = x,
                            y = y,
                        ),
                )
            }
        }
    }
}

private data class ScatterGeometry(
    val plot: Rect,
    val xMin: Double,
    val xMax: Double,
    val yMin: Double,
    val yMax: Double,
    val gridCount: Int,
)

private fun createScatterGeometry(
    width: Float,
    height: Float,
    xMin: Double,
    xMax: Double,
    yMin: Double,
    yMax: Double,
    labelTextPx: Float,
    compact: Boolean,
): ScatterGeometry {

    val leftPadding =
        if (compact) {
            max(
                30f,
                labelTextPx * 3.2f,
            )
        } else {
            max(
                38f,
                labelTextPx * 3.8f,
            )
        }

    val rightPadding =
        if (compact) {
            8f
        } else {
            12f
        }

    val topPadding =
        if (compact) {
            6f
        } else {
            10f
        }

    val bottomPadding =
        if (compact) {
            max(
                22f,
                labelTextPx * 2.2f,
            )
        } else {
            max(
                28f,
                labelTextPx * 2.5f,
            )
        }

    return ScatterGeometry(
        plot =
            Rect(
                left =
                    leftPadding,
                top =
                    topPadding,
                right =
                    max(
                        leftPadding + 1f,
                        width - rightPadding,
                    ),
                bottom =
                    max(
                        topPadding + 1f,
                        height - bottomPadding,
                    ),
            ),

        xMin =
            xMin,

        xMax =
            xMax,

        yMin =
            yMin,

        yMax =
            yMax,

        gridCount =
            4,
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope
        .drawScatterGrid(
    geometry: ScatterGeometry,
    axisColor: Color,
    gridColor: Color,
) {

    val plot =
        geometry.plot

    for (
    index in
    0..geometry.gridCount
    ) {
        val ratio =
            index.toFloat() /
                    geometry.gridCount.toFloat()

        val y =
            plot.bottom -
                    plot.height *
                    ratio

        drawLine(
            color =
                gridColor,
            start =
                Offset(
                    x = plot.left,
                    y = y,
                ),
            end =
                Offset(
                    x = plot.right,
                    y = y,
                ),
            strokeWidth =
                1f,
        )

        val x =
            plot.left +
                    plot.width *
                    ratio

        drawLine(
            color =
                gridColor,
            start =
                Offset(
                    x = x,
                    y = plot.top,
                ),
            end =
                Offset(
                    x = x,
                    y = plot.bottom,
                ),
            strokeWidth =
                1f,
        )
    }

    drawLine(
        color =
            axisColor,
        start =
            Offset(
                x = plot.left,
                y = plot.top,
            ),
        end =
            Offset(
                x = plot.left,
                y = plot.bottom,
            ),
        strokeWidth =
            1f,
    )

    drawLine(
        color =
            axisColor,
        start =
            Offset(
                x = plot.left,
                y = plot.bottom,
            ),
        end =
            Offset(
                x = plot.right,
                y = plot.bottom,
            ),
        strokeWidth =
            1f,
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope
        .drawScatterYAxisLabels(
    geometry: ScatterGeometry,
    color: Color,
    textSizePx: Float,
) {

    val paint =
        android.graphics.Paint()
            .apply {
                isAntiAlias = true
                this.color =
                    color.toArgbInt()
                textSize =
                    textSizePx
                textAlign =
                    android.graphics.Paint.Align.RIGHT
            }

    for (
    index in
    0..geometry.gridCount
    ) {
        val ratio =
            index.toDouble() /
                    geometry.gridCount.toDouble()

        val value =
            geometry.yMin +
                    (
                            geometry.yMax -
                                    geometry.yMin
                            ) *
                    ratio

        val y =
            geometry.plot.bottom -
                    geometry.plot.height *
                    ratio.toFloat()

        drawContext.canvas
            .nativeCanvas
            .drawText(
                formatScatterValue(
                    value
                ),
                geometry.plot.left - 6f,
                y +
                        textSizePx *
                        0.34f,
                paint,
            )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope
        .drawScatterXAxisLabels(
    geometry: ScatterGeometry,
    color: Color,
    textSizePx: Float,
) {

    val paint =
        android.graphics.Paint()
            .apply {
                isAntiAlias = true
                this.color =
                    color.toArgbInt()
                textSize =
                    textSizePx
                textAlign =
                    android.graphics.Paint.Align.CENTER
            }

    for (
    index in
    0..geometry.gridCount
    ) {
        val ratio =
            index.toDouble() /
                    geometry.gridCount.toDouble()

        val value =
            geometry.xMin +
                    (
                            geometry.xMax -
                                    geometry.xMin
                            ) *
                    ratio

        val x =
            geometry.plot.left +
                    geometry.plot.width *
                    ratio.toFloat()

        drawContext.canvas
            .nativeCanvas
            .drawText(
                formatScatterValue(
                    value
                ),
                x,
                geometry.plot.bottom +
                        textSizePx *
                        1.5f,
                paint,
            )
    }
}

private fun scatterX(
    geometry: ScatterGeometry,
    value: Double,
): Float {

    val span =
        geometry.xMax -
                geometry.xMin

    if (
        span <= 0.0 ||
        !span.isFinite()
    ) {
        return geometry.plot.center.x
    }

    val ratio =
        (
                (
                        value -
                                geometry.xMin
                        ) /
                        span
                )
            .coerceIn(
                0.0,
                1.0,
            )

    return geometry.plot.left +
            geometry.plot.width *
            ratio.toFloat()
}

private fun scatterY(
    geometry: ScatterGeometry,
    value: Double,
): Float {

    val span =
        geometry.yMax -
                geometry.yMin

    if (
        span <= 0.0 ||
        !span.isFinite()
    ) {
        return geometry.plot.center.y
    }

    val ratio =
        (
                (
                        value -
                                geometry.yMin
                        ) /
                        span
                )
            .coerceIn(
                0.0,
                1.0,
            )

    return geometry.plot.bottom -
            geometry.plot.height *
            ratio.toFloat()
}

private fun calculateScatterRange(
    values: List<Double>,
): Pair<Double, Double> {

    if (values.isEmpty()) {
        return 0.0 to 1.0
    }

    var minValue =
        values.minOrNull()
            ?: 0.0

    var maxValue =
        values.maxOrNull()
            ?: 1.0

    if (
        !minValue.isFinite() ||
        !maxValue.isFinite()
    ) {
        return 0.0 to 1.0
    }

    if (
        abs(
            maxValue -
                    minValue
        ) < 0.000001
    ) {
        val padding =
            if (
                abs(maxValue) < 1.0
            ) {
                1.0
            } else {
                abs(maxValue) *
                        0.1
            }

        minValue -=
            padding

        maxValue +=
            padding
    } else {
        val padding =
            (
                    maxValue -
                            minValue
                    ) *
                    0.08

        minValue -=
            padding

        maxValue +=
            padding
    }

    return niceScatterRange(
        minValue =
            minValue,
        maxValue =
            maxValue,
    )
}

private fun niceScatterRange(
    minValue: Double,
    maxValue: Double,
): Pair<Double, Double> {

    val span =
        maxValue -
                minValue

    if (
        span <= 0.0 ||
        !span.isFinite()
    ) {
        return minValue to maxValue
    }

    val roughStep =
        span /
                4.0

    val step =
        niceScatterNumber(
            roughStep
        )

    val niceMin =
        floor(
            minValue /
                    step
        ) *
                step

    val niceMax =
        ceil(
            maxValue /
                    step
        ) *
                step

    return niceMin to
            niceMax
}

private fun niceScatterNumber(
    value: Double,
): Double {

    if (
        value <= 0.0 ||
        !value.isFinite()
    ) {
        return 1.0
    }

    val exponent: Double =
        floor(
            kotlin.math.log10(
                value
            )
        )

    val magnitude: Double =
        10.0.pow(
            exponent
        )

    val fraction: Double =
        value /
                magnitude

    val niceFraction: Double =
        when {
            fraction <= 1.0 ->
                1.0

            fraction <= 2.0 ->
                2.0

            fraction <= 5.0 ->
                5.0

            else ->
                10.0
        }

    return niceFraction *
            magnitude
}

private fun formatScatterValue(
    value: Double,
): String {

    val absolute =
        abs(value)

    return when {
        absolute >=
                1_000_000_000 -> {
            String.format(
                Locale.US,
                "%.1fB",
                value /
                        1_000_000_000.0,
            )
        }

        absolute >=
                1_000_000 -> {
            String.format(
                Locale.US,
                "%.1fM",
                value /
                        1_000_000.0,
            )
        }

        absolute >=
                1_000 -> {
            String.format(
                Locale.US,
                "%.1fK",
                value /
                        1_000.0,
            )
        }

        absolute >=
                100 -> {
            String.format(
                Locale.US,
                "%.0f",
                value,
            )
        }

        absolute >=
                10 -> {
            String.format(
                Locale.US,
                "%.1f",
                value,
            )
        }

        else -> {
            String.format(
                Locale.US,
                "%.2f",
                value,
            )
        }
    }
}

private fun Color.toArgbInt(): Int {
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