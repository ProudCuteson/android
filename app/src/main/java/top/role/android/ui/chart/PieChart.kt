package top.role.android.ui.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.sp
import top.role.android.core.model.chart.MobileChartRuntime
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

@Composable
internal fun PieChart(
    chart: MobileChartRuntime,
    modifier: Modifier = Modifier,
    display: MobileChartDisplay =
        MobileChartDisplay(),
) {
    val slices =
        chart.pieSlices()

    if (slices.isEmpty()) {
        UnsupportedChartContent(
            title = "暂无饼图数据",
            modifier = modifier,
        )
        return
    }

    val total =
        slices.sumOf {
            it.value
        }

    if (
        total <= 0.0 ||
        !total.isFinite()
    ) {
        UnsupportedChartContent(
            title = "暂无饼图数据",
            modifier = modifier,
        )
        return
    }

    val theme =
        rememberMobileChartTheme()

    val scale =
        display.scale
            .coerceAtLeast(0.01)

    val density =
        LocalDensity.current

    val labelTextPx =
        with(density) {
            max(
                8.0,
                10.0 * scale,
            ).sp.toPx()
        }

    /*
     * Pie slice 与 Legend 必须使用完全相同的
     * series index → color 映射。
     */
    val legendItems =
        slices.mapIndexed { index, slice ->
            ChartLegendItem(
                key =
                    slice.id,
                label =
                    slice.name,
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
                size.width < 32f ||
                size.height < 32f
            ) {
                return@Canvas
            }

            drawPieChart(
                slices =
                    slices,
                total =
                    total,
                theme =
                    theme,
                donut =
                    chart.isDonutChart,
                innerRadiusRatio =
                    chart.pieInnerRadius,
                paddingAngle =
                    chart.piePaddingAngle,
                labelTextPx =
                    labelTextPx,
                compact =
                    display.compact,
            )
        }

        if (
            display.showLegend &&
            chart.legendVisible &&
            legendItems.isNotEmpty()
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

private fun DrawScope.drawPieChart(
    slices: List<ChartPieSlice>,
    total: Double,
    theme: MobileChartTheme,
    donut: Boolean,
    innerRadiusRatio: Double,
    paddingAngle: Double,
    labelTextPx: Float,
    compact: Boolean,
) {
    /*
     * Legend 已经移出 Canvas，
     * 因此整个 Canvas 都可以拿来画 Pie。
     */
    val diameter =
        min(
            size.width,
            size.height,
        )

    val outerRadius =
        (
                diameter *
                        if (compact) {
                            0.42f
                        } else {
                            0.44f
                        }
                )
            .coerceAtLeast(
                1f
            )

    val center =
        Offset(
            x =
                size.width / 2f,
            y =
                size.height / 2f,
        )

    val outerRect =
        Rect(
            left =
                center.x -
                        outerRadius,
            top =
                center.y -
                        outerRadius,
            right =
                center.x +
                        outerRadius,
            bottom =
                center.y +
                        outerRadius,
        )

    /*
     * paddingAngle 目前按角度理解。
     *
     * 后续如果桌面端协议明确单位不同，
     * 再统一修改 runtime adapter。
     */
    val safePaddingAngle =
        paddingAngle
            .toFloat()
            .coerceIn(
                0f,
                20f,
            )

    var startAngle =
        -90f

    slices.forEachIndexed { index, slice ->

        val ratio =
            slice.value /
                    total

        val fullSweep =
            (
                    ratio *
                            360.0
                    )
                .toFloat()

        /*
         * 间隙分到扇区两侧，
         * 但逻辑上的 startAngle 仍然按 fullSweep 推进，
         * 避免累计漂移。
         */
        val sweep =
            (
                    fullSweep -
                            safePaddingAngle
                    )
                .coerceAtLeast(
                    0f
                )

        val color =
            theme.seriesColor(
                index
            )

        if (sweep > 0f) {
            if (donut) {
                drawDonutSlice(
                    center =
                        center,
                    outerRadius =
                        outerRadius,
                    innerRadiusRatio =
                        innerRadiusRatio,
                    color =
                        color,
                    startAngle =
                        startAngle +
                                safePaddingAngle /
                                2f,
                    sweepAngle =
                        sweep,
                )
            } else {
                drawArc(
                    color =
                        color,
                    startAngle =
                        startAngle +
                                safePaddingAngle /
                                2f,
                    sweepAngle =
                        sweep,
                    useCenter =
                        true,
                    topLeft =
                        outerRect.topLeft,
                    size =
                        outerRect.size,
                )
            }

            /*
             * Slide compact 模式不显示 slice 内百分比，
             * 防止小图表过于拥挤。
             *
             * Doc / 普通模式下，
             * 只有占比 >= 8% 才显示。
             */
            if (
                !compact &&
                ratio >= 0.08 &&
                outerRadius >= 55f
            ) {
                drawPiePercentageLabel(
                    center =
                        center,

                    radius =
                        resolvePercentageRadius(
                            outerRadius =
                                outerRadius,
                            donut =
                                donut,
                            innerRadiusRatio =
                                innerRadiusRatio,
                        ),

                    startAngle =
                        startAngle,

                    sweepAngle =
                        fullSweep,

                    ratio =
                        ratio,

                    textSizePx =
                        labelTextPx,
                )
            }
        }

        startAngle +=
            fullSweep
    }
}

private fun DrawScope.drawDonutSlice(
    center: Offset,
    outerRadius: Float,
    innerRadiusRatio: Double,
    color: Color,
    startAngle: Float,
    sweepAngle: Float,
) {
    val safeInnerRatio =
        innerRadiusRatio
            .coerceIn(
                0.05,
                0.9,
            )
            .toFloat()

    val innerRadius =
        outerRadius *
                safeInnerRatio

    /*
     * Stroke 绘制的实际区域：
     *
     * innerRadius
     *     ← stroke →
     * outerRadius
     *
     * 所以 path radius 是二者中点。
     */
    val strokeWidth =
        outerRadius -
                innerRadius

    val pathRadius =
        (
                outerRadius +
                        innerRadius
                ) /
                2f

    val rect =
        Rect(
            left =
                center.x -
                        pathRadius,
            top =
                center.y -
                        pathRadius,
            right =
                center.x +
                        pathRadius,
            bottom =
                center.y +
                        pathRadius,
        )

    drawArc(
        color =
            color,
        startAngle =
            startAngle,
        sweepAngle =
            sweepAngle,
        useCenter =
            false,
        topLeft =
            rect.topLeft,
        size =
            rect.size,
        style =
            Stroke(
                width =
                    strokeWidth,
            ),
    )
}

private fun resolvePercentageRadius(
    outerRadius: Float,
    donut: Boolean,
    innerRadiusRatio: Double,
): Float {
    if (!donut) {
        return outerRadius *
                0.63f
    }

    val inner =
        innerRadiusRatio
            .coerceIn(
                0.05,
                0.9,
            )
            .toFloat()

    return outerRadius *
            (
                    inner +
                            (1f - inner) /
                            2f
                    )
}

private fun DrawScope.drawPiePercentageLabel(
    center: Offset,
    radius: Float,
    startAngle: Float,
    sweepAngle: Float,
    ratio: Double,
    textSizePx: Float,
) {
    val middleAngleDegrees =
        startAngle +
                sweepAngle /
                2f

    val middleAngleRadians =
        middleAngleDegrees *
                PI /
                180.0

    val x =
        center.x +
                cos(
                    middleAngleRadians
                )
                    .toFloat() *
                radius

    val y =
        center.y +
                sin(
                    middleAngleRadians
                )
                    .toFloat() *
                radius

    val text =
        String.format(
            Locale.US,
            "%.0f%%",
            ratio *
                    100.0,
        )

    val paint =
        android.graphics.Paint()
            .apply {
                isAntiAlias = true

                color =
                    android.graphics.Color.WHITE

                textSize =
                    textSizePx

                textAlign =
                    android.graphics.Paint.Align.CENTER

                isFakeBoldText =
                    true
            }

    drawContext.canvas
        .nativeCanvas
        .drawText(
            text,
            x,
            y +
                    textSizePx *
                    0.34f,
            paint,
        )
}