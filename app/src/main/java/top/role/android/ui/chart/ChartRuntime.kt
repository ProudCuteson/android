package top.role.android.ui.chart

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.jsonArray
import top.role.android.core.model.chart.MobileChartRuntime
import top.role.android.core.model.chart.MobileChartYField

internal data class ChartCategoricalPoint(
    val id: String,
    val xLabel: String,
    val seriesKey: String,
    val seriesLabel: String,
    val yValue: Double,
)

internal data class ChartScatterPoint(
    val id: String,
    val xValue: Double,
    val yValue: Double,
    val name: String?,
)

internal data class ChartPieSlice(
    val id: String,
    val name: String,
    val value: Double,
)

internal enum class ChartCurveType {
    Linear,
    Step,
    StepBefore,
    StepAfter,
    Smooth,
}

internal enum class ChartSeriesType {
    Bar,
    Line,
    Area,
    Scatter,
}

internal val MobileChartYField.resolvedSeriesType:
        ChartSeriesType
    get() =
        when (
            seriesType
                ?.trim()
                ?.lowercase()
        ) {
            "bar" ->
                ChartSeriesType.Bar

            "area" ->
                ChartSeriesType.Area

            "scatter" ->
                ChartSeriesType.Scatter

            else ->
                ChartSeriesType.Line
        }

/**
 * Bar / Line / Area 使用的标准化数据。
 *
 * 一行数据：
 *
 * {
 *   "month": "Jan",
 *   "sales": 100,
 *   "profit": 20
 * }
 *
 * 会按照 yFields 展开成多个 point。
 */
internal fun MobileChartRuntime
        .categoricalPoints(): List<ChartCategoricalPoint> {

    if (
        xField.isBlank() ||
        yFields.isEmpty()
    ) {
        return emptyList()
    }

    val result =
        mutableListOf<ChartCategoricalPoint>()

    data.forEachIndexed { rowIndex, row ->

        val xLabel =
            row[xField]
                ?.chartStringValue()
                ?.trim()
                .orEmpty()

        if (xLabel.isEmpty()) {
            return@forEachIndexed
        }

        yFields.forEach { field ->

            val value =
                row[field.key]
                    ?.chartDoubleValue()
                    ?: return@forEach

            result +=
                ChartCategoricalPoint(
                    id =
                        "$rowIndex:${field.key}",

                    xLabel =
                        xLabel,

                    seriesKey =
                        field.key,

                    seriesLabel =
                        field.label,

                    yValue =
                        value,
                )
        }
    }

    return result
}

internal val MobileChartRuntime.xAxisDisplayName: String
    get() {
        val label =
            xFieldLabel
                ?.trim()
                ?.takeIf {
                    it.isNotEmpty()
                }

        return label
            ?: xField
    }

internal val MobileChartRuntime.yAxisDisplayName: String
    get() {
        if (yFields.size != 1) {
            return "Value"
        }

        val field =
            yFields.first()

        val unit =
            field.unit
                ?.trim()
                ?.takeIf {
                    it.isNotEmpty()
                }

        return if (unit != null) {
            "${field.label} ($unit)"
        } else {
            field.label
        }
    }

/**
 * 默认策略与 iOS 保持一致：
 *
 * - options.legend.enabled 有值时优先使用；
 * - 否则多 series 默认显示；
 * - Pie 默认显示。
 */
internal val MobileChartRuntime.legendVisible: Boolean
    get() {
        val legend =
            options
                ?.chartObject()
                ?.get("legend")
                ?.chartObject()

        val explicit =
            legend
                ?.get("enabled")
                ?.chartBooleanValue()

        if (explicit != null) {
            return explicit
        }

        return yFields.size > 1 ||
                chartTypeName == "pie"
    }

internal val MobileChartRuntime.shouldShowDots: Boolean
    get() =
        yFields.any {
            it.dot == true
        }

/**
 * 暂时先统一 chartType 的读取。
 *
 * 等模型层最终确定 enum 命名以后，
 * 可以再改成直接 when(enum)。
 */
internal val MobileChartRuntime.chartTypeName: String
    get() =
        chartType
            .name
            .lowercase()

// ------------------------------------------------------------------------
// Scatter
// ------------------------------------------------------------------------

internal val MobileChartRuntime.scatterXAxisKey: String
    get() =
        options
            ?.chartObject()
            ?.get("scatterOptions")
            ?.chartObject()
            ?.get("xKey")
            ?.chartStringValue()
            ?.takeIf {
                it.isNotBlank()
            }
            ?: xField

internal val MobileChartRuntime.scatterYAxisKey: String
    get() =
        options
            ?.chartObject()
            ?.get("scatterOptions")
            ?.chartObject()
            ?.get("yKey")
            ?.chartStringValue()
            ?.takeIf {
                it.isNotBlank()
            }
            ?: yFields
                .firstOrNull()
                ?.key
                .orEmpty()


internal val MobileChartRuntime.scatterNameKey: String?
    get() =
        options
            ?.chartObject()
            ?.get("scatterOptions")
            ?.chartObject()
            ?.get("nameKey")
            ?.chartStringValue()
            ?.takeIf {
                it.isNotBlank()
            }

internal val MobileChartRuntime.scatterXAxisDisplayName: String
    get() {
        if (
            scatterXAxisKey == xField &&
            !xFieldLabel.isNullOrBlank()
        ) {
            return xFieldLabel!!
        }

        return scatterXAxisKey
    }

internal val MobileChartRuntime.scatterYAxisDisplayName: String
    get() {
        val field =
            yFields.firstOrNull {
                it.key ==
                        scatterYAxisKey
            }

        if (field != null) {
            val unit =
                field.unit
                    ?.trim()
                    ?.takeIf {
                        it.isNotEmpty()
                    }

            return if (unit != null) {
                "${field.label} ($unit)"
            } else {
                field.label
            }
        }

        return scatterYAxisKey
    }

internal val MobileChartRuntime.scatterDotSize: Double
    get() {
        val raw =
            options
                ?.chartObject()
                ?.get("scatterOptions")
                ?.chartObject()
                ?.get("dotSize")
                ?.chartDoubleValue()

        return raw
            ?.takeIf {
                it > 0.0
            }
            ?: 6.0
    }

internal val MobileChartRuntime.curveType:
        ChartCurveType
    get() {
        val raw =
            options
                ?.chartObject()
                ?.get("curveType")
                ?.chartStringValue()
                ?.trim()

        return when (raw) {
            "linear" ->
                ChartCurveType.Linear

            "step" ->
                ChartCurveType.Step

            "stepBefore" ->
                ChartCurveType.StepBefore

            "stepAfter" ->
                ChartCurveType.StepAfter

            "natural" ->
                ChartCurveType.Smooth

            else ->
                ChartCurveType.Smooth
        }
    }

internal fun MobileChartRuntime
        .scatterPoints(): List<ChartScatterPoint> {

    val xKey =
        scatterXAxisKey

    val yKey =
        scatterYAxisKey

    if (
        xKey.isBlank() ||
        yKey.isBlank()
    ) {
        return emptyList()
    }

    return buildList {

        data.forEachIndexed { rowIndex, row ->

            val x =
                row[xKey]
                    ?.chartDoubleValue()
                    ?: return@forEachIndexed

            val y =
                row[yKey]
                    ?.chartDoubleValue()
                    ?: return@forEachIndexed

            val name =
                scatterNameKey
                    ?.let { key ->
                        row[key]
                            ?.chartStringValue()
                    }

            add(
                ChartScatterPoint(
                    id =
                        rowIndex.toString(),

                    xValue =
                        x,

                    yValue =
                        y,

                    name =
                        name,
                )
            )
        }
    }
}

// ------------------------------------------------------------------------
// Pie / Donut
// ------------------------------------------------------------------------

internal val MobileChartRuntime.pieValueKey: String
    get() =
        options
            ?.chartObject()
            ?.get("pieOptions")
            ?.chartObject()
            ?.get("selectedYKey")
            ?.chartStringValue()
            ?.takeIf {
                it.isNotBlank()
            }
            ?: yFields
                .firstOrNull()
                ?.key
                .orEmpty()

internal val MobileChartRuntime.isDonutChart: Boolean
    get() {
        val innerRadius =
            options
                ?.chartObject()
                ?.get("pieOptions")
                ?.chartObject()
                ?.get("innerRadius")
                ?.chartDoubleValue()

        return (
                innerRadius
                    ?: 0.0
                ) > 0.0
    }

internal val MobileChartRuntime.pieInnerRadius: Double
    get() =
        options
            ?.chartObject()
            ?.get("pieOptions")
            ?.chartObject()
            ?.get("innerRadius")
            ?.chartDoubleValue()
            ?.coerceIn(
                0.0,
                0.9,
            )
            ?: if (isDonutChart) {
                0.52
            } else {
                0.0
            }

internal val MobileChartRuntime.piePaddingAngle: Double
    get() =
        options
            ?.chartObject()
            ?.get("pieOptions")
            ?.chartObject()
            ?.get("paddingAngle")
            ?.chartDoubleValue()
            ?.coerceAtLeast(0.0)
            ?: 0.0

internal fun MobileChartRuntime
        .pieSlices(): List<ChartPieSlice> {

    val valueKey =
        pieValueKey

    if (
        xField.isBlank() ||
        valueKey.isBlank()
    ) {
        return emptyList()
    }

    val result =
        buildList {

            data.forEachIndexed { rowIndex, row ->

                val value =
                    row[valueKey]
                        ?.chartDoubleValue()
                        ?: return@forEachIndexed

                if (value <= 0.0) {
                    return@forEachIndexed
                }

                val name =
                    row[xField]
                        ?.chartStringValue()
                        ?.trim()
                        ?.takeIf {
                            it.isNotEmpty()
                        }
                        ?: "Item ${rowIndex + 1}"

                add(
                    ChartPieSlice(
                        id =
                            rowIndex.toString(),

                        name =
                            name,

                        value =
                            value,
                    )
                )
            }
        }

    val sortByValue =
        options
            ?.chartObject()
            ?.get("pieOptions")
            ?.chartObject()
            ?.get("sortByValue")
            ?.chartBooleanValue()
            ?: false

    return if (sortByValue) {
        result.sortedByDescending {
            it.value
        }
    } else {
        result
    }
}

// ------------------------------------------------------------------------
// Options helpers
// ------------------------------------------------------------------------

internal fun MobileChartRuntime
        .optionString(
    group: String,
    key: String,
): String? {

    return options
        ?.chartObject()
        ?.get(group)
        ?.chartObject()
        ?.get(key)
        ?.chartStringValue()
}

internal fun MobileChartRuntime
        .optionDouble(
    group: String,
    key: String,
): Double? {

    return options
        ?.chartObject()
        ?.get(group)
        ?.chartObject()
        ?.get(key)
        ?.chartDoubleValue()
}

internal fun MobileChartRuntime
        .optionBoolean(
    group: String,
    key: String,
): Boolean? {

    return options
        ?.chartObject()
        ?.get(group)
        ?.chartObject()
        ?.get(key)
        ?.chartBooleanValue()
}

// ------------------------------------------------------------------------
// Radial
// ------------------------------------------------------------------------

internal data class ChartRadialItem(
    val id: String,
    val key: String,
    val label: String,
    val value: Double,
)

internal data class ChartRadialData(
    val items: List<ChartRadialItem>,
)

internal val MobileChartRuntime.radialMode: String
    get() =
        optionString(
            group = "radialOptions",
            key = "mode",
        )
            ?.takeIf {
                it == "tableRows" ||
                        it == "rowFields"
            }
            ?: "tableRows"

internal val MobileChartRuntime.radialValueKeys: List<String>
    get() {
        val explicit =
            options
                ?.chartObject()
                ?.get("radialOptions")
                ?.chartObject()
                ?.get("valueKeys")
                ?.let { value ->
                    runCatching {
                        value
                            .jsonArray
                            .mapNotNull {
                                it.chartStringValue()
                                    ?.takeIf(String::isNotBlank)
                            }
                    }.getOrNull()
                }
                .orEmpty()

        if (explicit.isNotEmpty()) {
            return explicit
        }

        return yFields.map {
            it.key
        }
    }

internal fun MobileChartRuntime.radialData(): ChartRadialData {
    return when (radialMode) {
        "rowFields" ->
            radialRowFieldsData()

        else ->
            radialTableRowsData()
    }
}

private fun MobileChartRuntime.radialTableRowsData():
        ChartRadialData {

    val valueKey =
        radialValueKeys
            .firstOrNull()
            ?: return ChartRadialData(
                items = emptyList()
            )

    val field =
        yFields.firstOrNull {
            it.key == valueKey
        }

    val items =
        buildList {
            data.forEachIndexed { index, row ->

                val value =
                    row[valueKey]
                        ?.chartDoubleValue()
                        ?: return@forEachIndexed

                if (
                    !value.isFinite() ||
                    value < 0.0
                ) {
                    return@forEachIndexed
                }

                val label =
                    row[xField]
                        ?.chartStringValue()
                        ?.trim()
                        ?.takeIf {
                            it.isNotEmpty()
                        }
                        ?: "Item ${index + 1}"

                add(
                    ChartRadialItem(
                        id =
                            "$index:$valueKey",

                        key =
                            valueKey,

                        label =
                            label,

                        value =
                            value,
                    )
                )
            }
        }

    return ChartRadialData(
        items = items
    )
}

private fun MobileChartRuntime.radialRowFieldsData():
        ChartRadialData {

    val row =
        selectedRadialRow()
            ?: return ChartRadialData(
                items = emptyList()
            )

    val valueKeys =
        radialValueKeys

    val items =
        buildList {

            valueKeys.forEach { key ->

                val value =
                    row[key]
                        ?.chartDoubleValue()
                        ?: return@forEach

                if (
                    !value.isFinite() ||
                    value < 0.0
                ) {
                    return@forEach
                }

                val field =
                    yFields.firstOrNull {
                        it.key == key
                    }

                add(
                    ChartRadialItem(
                        id =
                            "field:$key",

                        key =
                            key,

                        label =
                            field?.label
                                ?.takeIf {
                                    it.isNotBlank()
                                }
                                ?: key,

                        value =
                            value,
                    )
                )
            }
        }

    return ChartRadialData(
        items = items
    )
}

private fun MobileChartRuntime.selectedRadialRow():
        Map<String, JsonElement>? {

    if (data.isEmpty()) {
        return null
    }

    val options =
        options
            ?.chartObject()
            ?.get("radialOptions")
            ?.chartObject()

    val selectedRowIndex =
        options
            ?.get("selectedRowIndex")
            ?.chartDoubleValue()
            ?.toInt()

    if (
        selectedRowIndex != null &&
        selectedRowIndex in data.indices
    ) {
        return data[selectedRowIndex]
    }

    val selectedRowKey =
        options
            ?.get("selectedRowKey")
            ?.chartStringValue()
            ?.takeIf {
                it.isNotBlank()
            }

    if (
        selectedRowKey != null &&
        xField.isNotBlank()
    ) {
        val matched =
            data.firstOrNull { row ->
                row[xField]
                    ?.chartStringValue() ==
                        selectedRowKey
            }

        if (matched != null) {
            return matched
        }
    }

    return data.firstOrNull()
}

internal val MobileChartRuntime.radialInnerRadius:
        Double
    get() =
        radialRadiusOption(
            key = "innerRadius",
            fallback = 0.35,
        )

internal val MobileChartRuntime.radialOuterRadius:
        Double
    get() =
        radialRadiusOption(
            key = "outerRadius",
            fallback = 0.90,
        )

internal val MobileChartRuntime.radialBarSize: Double?
    get() =
        optionDouble(
            group = "radialOptions",
            key = "barSize",
        )
            ?.takeIf {
                it > 0.0
            }

internal val MobileChartRuntime.radialStartAngle: Double
    get() =
        optionDouble(
            group = "radialOptions",
            key = "startAngle",
        )
            ?: -90.0

internal val MobileChartRuntime.radialEndAngle: Double
    get() =
        optionDouble(
            group = "radialOptions",
            key = "endAngle",
        )
            ?: 270.0

internal val MobileChartRuntime.radialLinesVisible: Boolean
    get() =
        optionBoolean(
            group = "radialOptions",
            key = "radialLines",
        )
            ?: false

internal val MobileChartRuntime.radialBackgroundVisible: Boolean
    get() =
        optionBoolean(
            group = "radialOptions",
            key = "background",
        )
            ?: true

internal val MobileChartRuntime.radialLabelVisible: Boolean
    get() =
        optionBoolean(
            group = "radialOptions",
            key = "label",
        )
            ?: false

internal val MobileChartRuntime.radialLabelPosition: String
    get() =
        optionString(
            group = "radialOptions",
            key = "labelPosition",
        )
            ?: "center"

private fun MobileChartRuntime.radialRadiusOption(
    key: String,
    fallback: Double,
): Double {

    val raw =
        options
            ?.chartObject()
            ?.get("radialOptions")
            ?.chartObject()
            ?.get(key)
            ?: return fallback

    raw.chartDoubleValue()
        ?.let {
            /*
             * 数字：
             * 0~1 直接按 ratio；
             * >1 暂时按百分比。
             */
            return if (it <= 1.0) {
                it.coerceIn(
                    0.0,
                    1.0,
                )
            } else {
                (it / 100.0)
                    .coerceIn(
                        0.0,
                        1.0,
                    )
            }
        }

    val text =
        raw.chartStringValue()
            ?.trim()
            ?: return fallback

    if (text.endsWith("%")) {
        return text
            .removeSuffix("%")
            .toDoubleOrNull()
            ?.div(100.0)
            ?.coerceIn(
                0.0,
                1.0,
            )
            ?: fallback
    }

    return text
        .toDoubleOrNull()
        ?.let {
            if (it <= 1.0) {
                it
            } else {
                it / 100.0
            }
        }
        ?.coerceIn(
            0.0,
            1.0,
        )
        ?: fallback
}

// ------------------------------------------------------------------------
// JSON helpers
// ------------------------------------------------------------------------

private fun JsonElement.chartObject(): JsonObject? =
    runCatching {
        jsonObject
    }.getOrNull()

private fun JsonElement.chartStringValue(): String? =
    runCatching {
        jsonPrimitive.content
    }.getOrNull()

private fun JsonElement.chartDoubleValue(): Double? =
    runCatching {
        jsonPrimitive.doubleOrNull
    }.getOrNull()

private fun JsonElement.chartBooleanValue(): Boolean? =
    runCatching {
        jsonPrimitive.booleanOrNull
    }.getOrNull()