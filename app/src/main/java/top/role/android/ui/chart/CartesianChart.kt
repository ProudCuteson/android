package top.role.android.ui.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.sp
import top.role.android.core.model.chart.MobileChartRuntime
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.log10
import kotlin.math.pow
import java.util.Locale

internal enum class CartesianChartType {
    Bar,
    Line,
    Area,
    Composed,
}

private data class CartesianSeries(
    val key: String,
    val label: String,
    val points: List<ChartCategoricalPoint>,

    val fieldIndex: Int,

    val seriesType: ChartSeriesType,
)

private data class CartesianGeometry(
    val plot: Rect,
    val yMin: Double,
    val yMax: Double,
    val xLabels: List<String>,
    val gridCount: Int,
)
@Composable
internal fun CartesianChart(
    chart: MobileChartRuntime,
    type: CartesianChartType,
    modifier: Modifier = Modifier,
    display: MobileChartDisplay =
        MobileChartDisplay(),
) {
    val points =
        chart.categoricalPoints()

    if (points.isEmpty()) {
        UnsupportedChartContent(
            title = "暂无图表数据",
            modifier = modifier,
        )
        return
    }

    val series =
        points
            .groupBy {
                it.seriesKey
            }
            .mapNotNull { (key, values) ->

                val fieldIndex =
                    chart.yFields
                        .indexOfFirst {
                            it.key == key
                        }

                if (fieldIndex < 0) {
                    return@mapNotNull null
                }

                val field =
                    chart.yFields[
                        fieldIndex
                    ]

                CartesianSeries(
                    key =
                        key,

                    label =
                        values
                            .firstOrNull()
                            ?.seriesLabel
                            ?: key,

                    points =
                        values,

                    fieldIndex =
                        fieldIndex,

                    seriesType =
                        field.resolvedSeriesType,
                )
            }

    if (series.isEmpty()) {
        UnsupportedChartContent(
            title = "暂无图表数据",
            modifier = modifier,
        )
        return
    }

    val xLabels =
        points
            .map {
                it.xLabel
            }
            .distinct()

    val values =
        points.map {
            it.yValue
        }

    val range =
        calculateChartRange(
            values = values,
            includeZero =
                type == CartesianChartType.Bar ||
                        type == CartesianChartType.Area ||
                        (
                                type == CartesianChartType.Composed &&
                                        series.any {
                                            it.seriesType ==
                                                    ChartSeriesType.Bar ||
                                                    it.seriesType ==
                                                    ChartSeriesType.Area
                                        }
                                )
        )

    val density =
        LocalDensity.current

    val scale =
        display.scale
            .coerceAtLeast(0.01)

    val labelTextPx =
        with(density) {
            max(
                9.0,
                11.0 * scale,
            ).sp.toPx()
        }

    /*
     * Chart 统一主题。
     */
    val theme =
        rememberMobileChartTheme()

    val axisColor =
        theme.axis

    val gridColor =
        theme.grid

    val axisTextColor =
        theme.secondaryLabel

    val seriesColors =
        theme.series

    /*
     * Legend 与 series 使用同一套颜色序号。
     */
    val legendItems =
        chart.yFields
            .mapIndexed { index, field ->
                ChartLegendItem(
                    key =
                        field.key,
                    label =
                        field.label,
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

            val geometry =
                createCartesianGeometry(
                    width =
                        size.width,
                    height =
                        size.height,
                    xLabels =
                        xLabels,
                    yMin =
                        range.first,
                    yMax =
                        range.second,
                    labelTextPx =
                        labelTextPx,
                    compact =
                        display.compact,
                )

            drawCartesianGrid(
                geometry =
                    geometry,
                axisColor =
                    axisColor,
                gridColor =
                    gridColor,
            )

            drawYAxisLabels(
                geometry =
                    geometry,
                color =
                    axisTextColor,
                textSizePx =
                    labelTextPx,
            )

            drawXAxisLabels(
                geometry =
                    geometry,
                color =
                    axisTextColor,
                textSizePx =
                    labelTextPx,
            )

            when (type) {
                CartesianChartType.Line -> {
                    drawLineSeries(
                        geometry =
                            geometry,
                        series =
                            series,
                        colors =
                            seriesColors,
                        showDots =
                            chart.shouldShowDots,
                        curveType =
                            chart.curveType,
                    )
                }

                CartesianChartType.Area -> {
                    drawAreaSeries(
                        geometry =
                            geometry,
                        series =
                            series,
                        colors =
                            seriesColors,
                        showDots =
                            chart.shouldShowDots,
                        curveType =
                            chart.curveType,
                    )
                }

                CartesianChartType.Bar -> {
                    drawBarSeries(
                        geometry =
                            geometry,
                        series =
                            series,
                        colors =
                            seriesColors,
                    )
                }

                CartesianChartType.Composed -> {
                    drawComposedSeries(
                        chart =
                            chart,

                        geometry =
                            geometry,

                        series =
                            series,

                        theme =
                            theme,
                    )
                }

            }
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

private fun calculateChartRange(
    values: List<Double>,
    includeZero: Boolean,
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

    if (includeZero) {
        minValue =
            min(
                0.0,
                minValue,
            )

        maxValue =
            max(
                0.0,
                maxValue,
            )
    }

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

        minValue -= padding
        maxValue += padding
    } else {
        val padding =
            (
                    maxValue -
                            minValue
                    ) *
                    0.08

        minValue -= padding
        maxValue += padding
    }

    return niceRange(
        minValue,
        maxValue,
    )
}

private fun niceRange(
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
        span / 4.0

    val step =
        niceNumber(
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

    return niceMin to niceMax
}

private fun niceNumber(
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
            log10(value)
        )

    val magnitude: Double =
        10.0.pow(
            exponent
        )

    val fraction: Double =
        value / magnitude

    val niceFraction: Double =
        when {
            fraction <= 1.0 -> 1.0
            fraction <= 2.0 -> 2.0
            fraction <= 5.0 -> 5.0
            else -> 10.0
        }

    return niceFraction *
            magnitude
}

private fun createCartesianGeometry(
    width: Float,
    height: Float,
    xLabels: List<String>,
    yMin: Double,
    yMax: Double,
    labelTextPx: Float,
    compact: Boolean,
): CartesianGeometry {

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

    val plot =
        Rect(
            left =
                leftPadding,
            top =
                topPadding,
            right =
                max(
                    leftPadding + 1f,
                    width -
                            rightPadding,
                ),
            bottom =
                max(
                    topPadding + 1f,
                    height -
                            bottomPadding,
                ),
        )

    return CartesianGeometry(
        plot =
            plot,
        yMin =
            yMin,
        yMax =
            yMax,
        xLabels =
            xLabels,
        gridCount =
            4,
    )
}

private fun DrawScope.drawCartesianGrid(
    geometry: CartesianGeometry,
    axisColor: Color,
    gridColor: Color,
) {
    val plot =
        geometry.plot

    /*
     * 横向 grid。
     */
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
    }

    /*
     * Y axis。
     */
    drawLine(
        color =
            axisColor,
        start =
            Offset(
                plot.left,
                plot.top,
            ),
        end =
            Offset(
                plot.left,
                plot.bottom,
            ),
        strokeWidth =
            1f,
    )

    /*
     * X axis。
     */
    drawLine(
        color =
            axisColor,
        start =
            Offset(
                plot.left,
                plot.bottom,
            ),
        end =
            Offset(
                plot.right,
                plot.bottom,
            ),
        strokeWidth =
            1f,
    )
}

private fun DrawScope.drawYAxisLabels(
    geometry: CartesianGeometry,
    color: Color,
    textSizePx: Float,
) {
    val plot =
        geometry.plot

    val paint =
        android.graphics.Paint()
            .apply {
                isAntiAlias = true
                this.color =
                    color.toArgb()
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
            plot.bottom -
                    plot.height *
                    ratio.toFloat()

        drawContext.canvas.nativeCanvas
            .drawText(
                formatChartValue(
                    value
                ),
                plot.left - 6f,
                y +
                        textSizePx *
                        0.34f,
                paint,
            )
    }
}

private fun DrawScope.drawXAxisLabels(
    geometry: CartesianGeometry,
    color: Color,
    textSizePx: Float,
) {
    val labels =
        geometry.xLabels

    if (labels.isEmpty()) {
        return
    }

    val plot =
        geometry.plot

    val paint =
        android.graphics.Paint()
            .apply {
                isAntiAlias = true
                this.color =
                    color.toArgb()
                textSize =
                    textSizePx
                textAlign =
                    android.graphics.Paint.Align.CENTER
            }

    /*
     * 手机上 category 过多时不能全部显示，
     * 否则标签一定重叠。
     */
    val maxLabels =
        max(
            2,
            (
                    plot.width /
                            max(
                                42f,
                                textSizePx * 4f,
                            )
                    )
                .toInt(),
        )

    val step =
        max(
            1,
            ceil(
                labels.size.toDouble() /
                        maxLabels.toDouble()
            ).toInt(),
        )

    labels.forEachIndexed { index, label ->

        if (
            index % step != 0 &&
            index !=
            labels.lastIndex
        ) {
            return@forEachIndexed
        }

        val x =
            categoryX(
                geometry =
                    geometry,
                index =
                    index,
            )

        val display =
            if (label.length > 12) {
                label.take(11) +
                        "…"
            } else {
                label
            }

        drawContext.canvas.nativeCanvas
            .drawText(
                display,
                x,
                plot.bottom +
                        textSizePx *
                        1.5f,
                paint,
            )
    }
}

private fun DrawScope.drawLineSeries(
    geometry: CartesianGeometry,
    series: List<CartesianSeries>,
    colors: List<Color>,
    showDots: Boolean,
    curveType: ChartCurveType,
) {
    series.forEachIndexed { seriesIndex, item ->

        val color =
            colors[
                seriesIndex %
                        colors.size
            ]

        val points =
            orderedSeriesOffsets(
                geometry =
                    geometry,
                points =
                    item.points,
            )

        if (points.isEmpty()) {
            return@forEachIndexed
        }

        if (points.size == 1) {
            drawCircle(
                color =
                    color,
                radius =
                    4f,
                center =
                    points.first(),
            )

            return@forEachIndexed
        }

        val path =
            buildCartesianPath(
                points =
                    points,
                curveType =
                    curveType,
            )

        drawPath(
            path =
                path,
            color =
                color,
            style =
                Stroke(
                    width =
                        2.5f,
                    cap =
                        StrokeCap.Round,
                    join =
                        StrokeJoin.Round,
                ),
        )

        if (showDots) {
            points.forEach { point ->
                drawCircle(
                    color =
                        color,
                    radius =
                        3.5f,
                    center =
                        point,
                )
            }
        }
    }
}

private fun DrawScope.drawAreaSeries(
    geometry: CartesianGeometry,
    series: List<CartesianSeries>,
    colors: List<Color>,
    showDots: Boolean,
    curveType: ChartCurveType,
) {
    series.forEachIndexed { seriesIndex, item ->

        val color =
            colors[
                seriesIndex %
                        colors.size
            ]

        val points =
            orderedSeriesOffsets(
                geometry =
                    geometry,
                points =
                    item.points,
            )

        if (points.isEmpty()) {
            return@forEachIndexed
        }

        val zeroY =
            valueY(
                geometry =
                    geometry,
                value =
                    0.0.coerceIn(
                        geometry.yMin,
                        geometry.yMax,
                    ),
            )

        val fillPath =
            buildCartesianAreaPath(
                points =
                    points,
                baselineY =
                    zeroY,
                curveType =
                    curveType,
            )

        drawPath(
            path =
                fillPath,
            color =
                color.copy(
                    alpha = 0.18f
                ),
            style =
                Fill,
        )

        val linePath =
            buildCartesianPath(
                points =
                    points,
                curveType =
                    curveType,
            )

        drawPath(
            path =
                linePath,
            color =
                color,
            style =
                Stroke(
                    width =
                        2.5f,
                    cap =
                        StrokeCap.Round,
                    join =
                        StrokeJoin.Round,
                ),
        )

        if (showDots) {
            points.forEach { point ->
                drawCircle(
                    color =
                        color,
                    radius =
                        3.5f,
                    center =
                        point,
                )
            }
        }
    }
}

private fun DrawScope.drawBarSeries(
    geometry: CartesianGeometry,
    series: List<CartesianSeries>,
    colors: List<Color>,
) {
    val categoryCount =
        geometry.xLabels.size

    if (
        categoryCount <= 0 ||
        series.isEmpty()
    ) {
        return
    }

    val plot =
        geometry.plot

    val categoryWidth =
        plot.width /
                categoryCount.toFloat()

    val groupWidth =
        categoryWidth *
                0.72f

    val barWidth =
        (
                groupWidth /
                        series.size.toFloat()
                )
            .coerceAtLeast(
                1f
            )

    val zeroY =
        valueY(
            geometry =
                geometry,
            value =
                0.0.coerceIn(
                    geometry.yMin,
                    geometry.yMax,
                ),
        )

    series.forEachIndexed { seriesIndex, item ->

        val color =
            colors[
                seriesIndex %
                        colors.size
            ]

        val byCategory =
            item.points
                .associateBy {
                    it.xLabel
                }

        geometry.xLabels
            .forEachIndexed { categoryIndex, label ->

                val point =
                    byCategory[label]
                        ?: return@forEachIndexed

                val categoryCenter =
                    categoryX(
                        geometry =
                            geometry,
                        index =
                            categoryIndex,
                    )

                val groupLeft =
                    categoryCenter -
                            groupWidth / 2f

                val left =
                    groupLeft +
                            seriesIndex *
                            barWidth

                val y =
                    valueY(
                        geometry =
                            geometry,
                        value =
                            point.yValue,
                    )

                val top =
                    min(
                        zeroY,
                        y,
                    )

                val bottom =
                    max(
                        zeroY,
                        y,
                    )

                drawRect(
                    color =
                        color,
                    topLeft =
                        Offset(
                            x =
                                left +
                                        1f,
                            y =
                                top,
                        ),
                    size =
                        androidx.compose.ui.geometry.Size(
                            width =
                                max(
                                    1f,
                                    barWidth - 2f,
                                ),
                            height =
                                max(
                                    1f,
                                    bottom - top,
                                ),
                        ),
                )
            }
    }
}

private fun DrawScope.drawComposedSeries(
    chart: MobileChartRuntime,
    geometry: CartesianGeometry,
    series: List<CartesianSeries>,
    theme: MobileChartTheme,
) {
    /*
     * 绘制顺序很重要：
     *
     * Area 最底层
     * Bar
     * Line
     * Scatter 最上层
     *
     * 避免 Area 把其它 series 遮住。
     */

    val areaSeries =
        series.filter {
            it.seriesType ==
                    ChartSeriesType.Area
        }

    val barSeries =
        series.filter {
            it.seriesType ==
                    ChartSeriesType.Bar
        }

    val lineSeries =
        series.filter {
            it.seriesType ==
                    ChartSeriesType.Line
        }

    val scatterSeries =
        series.filter {
            it.seriesType ==
                    ChartSeriesType.Scatter
        }

    if (areaSeries.isNotEmpty()) {
        drawAreaSeries(
            geometry =
                geometry,

            series =
                areaSeries,

            colors =
                areaSeries.map {
                    theme.seriesColor(
                        it.fieldIndex
                    )
                },

            showDots =
                false,

            curveType =
                chart.curveType,
        )
    }

    if (barSeries.isNotEmpty()) {
        drawBarSeries(
            geometry =
                geometry,

            series =
                barSeries,

            colors =
                barSeries.map {
                    theme.seriesColor(
                        it.fieldIndex
                    )
                },
        )
    }

    if (lineSeries.isNotEmpty()) {
        drawLineSeries(
            geometry =
                geometry,

            series =
                lineSeries,

            colors =
                lineSeries.map {
                    theme.seriesColor(
                        it.fieldIndex
                    )
                },

            showDots =
                lineSeries.any { item ->

                    chart.yFields
                        .getOrNull(
                            item.fieldIndex
                        )
                        ?.dot == true
                },

            curveType =
                chart.curveType,
        )
    }

    if (scatterSeries.isNotEmpty()) {
        drawCategoricalScatterSeries(
            chart =
                chart,

            geometry =
                geometry,

            series =
                scatterSeries,

            theme =
                theme,
        )
    }
}

private fun DrawScope.drawCategoricalScatterSeries(
    chart: MobileChartRuntime,
    geometry: CartesianGeometry,
    series: List<CartesianSeries>,
    theme: MobileChartTheme,
) {
    series.forEach { item ->

        val field =
            chart.yFields
                .getOrNull(
                    item.fieldIndex
                )

        val color =
            theme.seriesColor(
                item.fieldIndex
            )

        val radius =
            (
                    field
                        ?.strokeWidth
                        ?.takeIf {
                            it > 0.0
                        }
                        ?.times(2.0)
                        ?: 4.5
                    )
                .toFloat()

        val points =
            orderedSeriesOffsets(
                geometry =
                    geometry,

                points =
                    item.points,
            )

        points.forEach { point ->

            drawCircle(
                color =
                    color,

                radius =
                    radius,

                center =
                    point,
            )
        }
    }
}

private fun orderedSeriesOffsets(
    geometry: CartesianGeometry,
    points: List<ChartCategoricalPoint>,
): List<Offset> {

    val byCategory =
        points.associateBy {
            it.xLabel
        }

    return geometry.xLabels
        .mapIndexedNotNull { index, label ->

            val point =
                byCategory[label]
                    ?: return@mapIndexedNotNull null

            Offset(
                x =
                    categoryX(
                        geometry =
                            geometry,
                        index =
                            index,
                    ),

                y =
                    valueY(
                        geometry =
                            geometry,
                        value =
                            point.yValue,
                    ),
            )
        }
}

private fun categoryX(
    geometry: CartesianGeometry,
    index: Int,
): Float {
    val count =
        geometry.xLabels.size

    if (count <= 1) {
        return geometry.plot.center.x
    }

    val segment =
        geometry.plot.width /
                count.toFloat()

    return geometry.plot.left +
            segment *
            (
                    index.toFloat() +
                            0.5f
                    )
}

private fun valueY(
    geometry: CartesianGeometry,
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

private fun buildCartesianPath(
    points: List<Offset>,
    curveType: ChartCurveType,
): Path {
    val path =
        Path()

    if (points.isEmpty()) {
        return path
    }

    path.moveTo(
        points.first().x,
        points.first().y,
    )

    if (points.size == 1) {
        return path
    }

    when (curveType) {
        ChartCurveType.Linear ->
            appendLinearPath(
                path = path,
                points = points,
            )

        ChartCurveType.Step ->
            appendStepCenterPath(
                path = path,
                points = points,
            )

        ChartCurveType.StepBefore ->
            appendStepBeforePath(
                path = path,
                points = points,
            )

        ChartCurveType.StepAfter ->
            appendStepAfterPath(
                path = path,
                points = points,
            )

        ChartCurveType.Smooth ->
            appendSmoothPath(
                path = path,
                points = points,
            )
    }

    return path
}

private fun buildCartesianAreaPath(
    points: List<Offset>,
    baselineY: Float,
    curveType: ChartCurveType,
): Path {
    val path =
        Path()

    if (points.isEmpty()) {
        return path
    }

    path.moveTo(
        points.first().x,
        baselineY,
    )

    path.lineTo(
        points.first().x,
        points.first().y,
    )

    when (curveType) {
        ChartCurveType.Linear ->
            appendLinearPath(
                path = path,
                points = points,
            )

        ChartCurveType.Step ->
            appendStepCenterPath(
                path = path,
                points = points,
            )

        ChartCurveType.StepBefore ->
            appendStepBeforePath(
                path = path,
                points = points,
            )

        ChartCurveType.StepAfter ->
            appendStepAfterPath(
                path = path,
                points = points,
            )

        ChartCurveType.Smooth ->
            appendSmoothPath(
                path = path,
                points = points,
            )
    }

    path.lineTo(
        points.last().x,
        baselineY,
    )

    path.close()

    return path
}

private fun appendLinearPath(
    path: Path,
    points: List<Offset>,
) {
    points
        .drop(1)
        .forEach { point ->
            path.lineTo(
                point.x,
                point.y,
            )
        }
}

private fun appendStepCenterPath(
    path: Path,
    points: List<Offset>,
) {
    for (index in 1 until points.size) {
        val previous =
            points[index - 1]

        val current =
            points[index]

        val middleX =
            (
                    previous.x +
                            current.x
                    ) /
                    2f

        path.lineTo(
            middleX,
            previous.y,
        )

        path.lineTo(
            middleX,
            current.y,
        )

        path.lineTo(
            current.x,
            current.y,
        )
    }
}

private fun appendStepBeforePath(
    path: Path,
    points: List<Offset>,
) {
    for (index in 1 until points.size) {
        val previous =
            points[index - 1]

        val current =
            points[index]

        path.lineTo(
            current.x,
            previous.y,
        )

        path.lineTo(
            current.x,
            current.y,
        )
    }
}

private fun appendStepAfterPath(
    path: Path,
    points: List<Offset>,
) {
    for (index in 1 until points.size) {
        val previous =
            points[index - 1]

        val current =
            points[index]

        path.lineTo(
            previous.x,
            current.y,
        )

        path.lineTo(
            current.x,
            current.y,
        )
    }
}

private fun appendSmoothPath(
    path: Path,
    points: List<Offset>,
) {
    if (points.size < 3) {
        appendLinearPath(
            path = path,
            points = points,
        )
        return
    }

    for (index in 0 until points.lastIndex) {
        val p0 =
            points[
                max(
                    0,
                    index - 1,
                )
            ]

        val p1 =
            points[index]

        val p2 =
            points[index + 1]

        val p3 =
            points[
                min(
                    points.lastIndex,
                    index + 2,
                )
            ]

        val control1 =
            Offset(
                x =
                    p1.x +
                            (
                                    p2.x -
                                            p0.x
                                    ) /
                            6f,
                y =
                    p1.y +
                            (
                                    p2.y -
                                            p0.y
                                    ) /
                            6f,
            )

        val control2 =
            Offset(
                x =
                    p2.x -
                            (
                                    p3.x -
                                            p1.x
                                    ) /
                            6f,
                y =
                    p2.y -
                            (
                                    p3.y -
                                            p1.y
                                    ) /
                            6f,
            )

        path.cubicTo(
            control1.x,
            control1.y,
            control2.x,
            control2.y,
            p2.x,
            p2.y,
        )
    }
}

private fun formatChartValue(
    value: Double,
): String {
    val absolute =
        abs(value)

    return when {
        absolute >= 1_000_000_000 ->
            String.format(
                Locale.US,
                "%.1fB",
                value / 1_000_000_000.0,
            )

        absolute >= 1_000_000 ->
            String.format(
                Locale.US,
                "%.1fM",
                value / 1_000_000.0,
            )

        absolute >= 1_000 ->
            String.format(
                Locale.US,
                "%.1fK",
                value / 1_000.0,
            )

        absolute >= 100 ->
            String.format(
                Locale.US,
                "%.0f",
                value,
            )

        absolute >= 10 ->
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

private fun Color.toArgb(): Int =
    android.graphics.Color.argb(
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