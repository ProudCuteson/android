package top.role.android.ui.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.sp
import top.role.android.core.model.chart.MobileChartRuntime
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

@Composable
internal fun RadialChart(
    chart: MobileChartRuntime,
    modifier: Modifier = Modifier,
    display: MobileChartDisplay =
        MobileChartDisplay(),
) {
    val radialData =
        chart.radialData()

    val items =
        radialData.items

    if (items.isEmpty()) {
        UnsupportedChartContent(
            title = "暂无径向图数据",
            modifier = modifier,
        )
        return
    }

    val maxValue =
        items
            .maxOfOrNull {
                it.value
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
        items.mapIndexed { index, item ->
            ChartLegendItem(
                key =
                    item.id,

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

            drawRadialChart(
                chart =
                    chart,

                items =
                    items,

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

private fun DrawScope.drawRadialChart(
    chart: MobileChartRuntime,
    items: List<ChartRadialItem>,
    maxValue: Double,
    theme: MobileChartTheme,
    labelTextPx: Float,
    compact: Boolean,
) {
    if (items.isEmpty()) {
        return
    }

    val minSide =
        min(
            size.width,
            size.height,
        )

    val center =
        Offset(
            x =
                size.width / 2f,

            y =
                size.height / 2f,
        )

    val maxRadius =
        minSide /
                2f

    val outerRadius =
        (
                maxRadius *
                        chart.radialOuterRadius
                            .toFloat()
                )
            .coerceAtLeast(
                4f
            )

    val innerRadius =
        (
                maxRadius *
                        chart.radialInnerRadius
                            .toFloat()
                )
            .coerceIn(
                0f,
                outerRadius -
                        1f,
            )

    val radialSpan =
        (
                outerRadius -
                        innerRadius
                )
            .coerceAtLeast(
                1f
            )

    val gap =
        if (items.size > 1) {
            max(
                2f,
                radialSpan *
                        0.04f,
            )
        } else {
            0f
        }

    val automaticBarSize =
        (
                radialSpan -
                        gap *
                        (
                                items.size -
                                        1
                                )
                ) /
                items.size.toFloat()

    val requestedBarSize =
        chart.radialBarSize
            ?.toFloat()

    val barSize =
        (
                requestedBarSize
                    ?: automaticBarSize
                )
            .coerceAtLeast(
                2f
            )
            .coerceAtMost(
                automaticBarSize
                    .coerceAtLeast(
                        2f
                    )
            )

    val startAngle =
        chart.radialStartAngle
            .toFloat()

    val totalSweep =
        (
                chart.radialEndAngle -
                        chart.radialStartAngle
                )
            .toFloat()

    items.forEachIndexed { index, item ->

        val radius =
            outerRadius -
                    barSize / 2f -
                    index *
                    (
                            barSize +
                                    gap
                            )

        if (
            radius <=
            innerRadius
        ) {
            return@forEachIndexed
        }

        val ratio =
            (
                    item.value /
                            maxValue
                    )
                .coerceIn(
                    0.0,
                    1.0,
                )

        val rect =
            Rect(
                left =
                    center.x -
                            radius,

                top =
                    center.y -
                            radius,

                right =
                    center.x +
                            radius,

                bottom =
                    center.y +
                            radius,
            )

        if (
            chart.radialBackgroundVisible
        ) {
            drawArc(
                color =
                    theme.grid
                        .copy(
                            alpha =
                                0.38f
                        ),

                startAngle =
                    startAngle,

                sweepAngle =
                    totalSweep,

                useCenter =
                    false,

                topLeft =
                    rect.topLeft,

                size =
                    rect.size,

                style =
                    Stroke(
                        width =
                            barSize,

                        cap =
                            StrokeCap.Round,
                    ),
            )
        }

        drawArc(
            color =
                theme.seriesColor(
                    index
                ),

            startAngle =
                startAngle,

            sweepAngle =
                totalSweep *
                        ratio.toFloat(),

            useCenter =
                false,

            topLeft =
                rect.topLeft,

            size =
                rect.size,

            style =
                Stroke(
                    width =
                        barSize,

                    cap =
                        StrokeCap.Round,
                ),
        )
    }

    if (
        chart.radialLinesVisible
    ) {
        drawRadialGuideLines(
            center =
                center,

            innerRadius =
                innerRadius,

            outerRadius =
                outerRadius,

            color =
                theme.grid,
        )
    }

    if (
        chart.radialLabelVisible
    ) {
        drawRadialLabel(
            chart =
                chart,

            item =
                items.first(),

            center =
                center,

            radius =
                innerRadius,

            maxValue =
                maxValue,

            color =
                theme.label,

            secondaryColor =
                theme.secondaryLabel,

            textSizePx =
                labelTextPx,

            compact =
                compact,
        )
    }
}

private fun DrawScope.drawRadialGuideLines(
    center: Offset,
    innerRadius: Float,
    outerRadius: Float,
    color: Color,
) {
    val count =
        4

    for (
    index in
    1..count
    ) {
        val ratio =
            index.toFloat() /
                    count.toFloat()

        val radius =
            innerRadius +
                    (
                            outerRadius -
                                    innerRadius
                            ) *
                    ratio

        drawCircle(
            color =
                color.copy(
                    alpha =
                        0.35f
                ),

            radius =
                radius,

            center =
                center,

            style =
                Stroke(
                    width =
                        1f
                ),
        )
    }
}

private fun DrawScope.drawRadialLabel(
    chart: MobileChartRuntime,
    item: ChartRadialItem,
    center: Offset,
    radius: Float,
    maxValue: Double,
    color: Color,
    secondaryColor: Color,
    textSizePx: Float,
    compact: Boolean,
) {
    val valueText =
        formatRadialValue(
            item.value
        )

    val valuePaint =
        android.graphics.Paint()
            .apply {
                isAntiAlias =
                    true

                this.color =
                    color.toRadialArgb()

                textSize =
                    textSizePx *
                            if (compact) {
                                1.2f
                            } else {
                                1.45f
                            }

                textAlign =
                    android.graphics.Paint
                        .Align.CENTER

                isFakeBoldText =
                    true
            }

    val labelPaint =
        android.graphics.Paint()
            .apply {
                isAntiAlias =
                    true

                this.color =
                    secondaryColor
                        .toRadialArgb()

                textSize =
                    textSizePx *
                            0.85f

                textAlign =
                    android.graphics.Paint
                        .Align.CENTER
            }

    val position =
        chart.radialLabelPosition

    val y =
        when (position) {
            "insideStart" ->
                center.y -
                        radius *
                        0.45f

            "insideEnd" ->
                center.y +
                        radius *
                        0.45f

            "outside" ->
                center.y +
                        radius +
                        valuePaint.textSize

            else ->
                center.y
        }

    drawContext.canvas
        .nativeCanvas
        .drawText(
            valueText,
            center.x,
            y +
                    valuePaint.textSize *
                    0.12f,
            valuePaint,
        )

    if (
        !compact &&
        position == "center"
    ) {
        drawContext.canvas
            .nativeCanvas
            .drawText(
                item.label,
                center.x,
                y +
                        valuePaint.textSize *
                        0.95f,
                labelPaint,
            )
    }
}

private fun formatRadialValue(
    value: Double,
): String {
    return when {
        value >= 1_000_000 ->
            String.format(
                Locale.US,
                "%.1fM",
                value /
                        1_000_000.0,
            )

        value >= 1_000 ->
            String.format(
                Locale.US,
                "%.1fK",
                value /
                        1_000.0,
            )

        value >= 100 ->
            String.format(
                Locale.US,
                "%.0f",
                value,
            )

        value >= 10 ->
            String.format(
                Locale.US,
                "%.1f",
                value,
            )

        else ->
            String.format(
                Locale.US,
                "%.2f",
                value,
            )
    }
}

private fun Color.toRadialArgb(): Int {
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