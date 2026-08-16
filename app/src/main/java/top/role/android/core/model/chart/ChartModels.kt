package top.role.android.core.model.chart

import kotlinx.serialization.json.JsonElement

enum class MobileChartType {
    Area,
    Bar,
    Line,
    Pie,
    Radar,
    Radial,
    Scatter,
    Composed,
}

data class MobileChartYField(
    val key: String,
    val label: String,
    val color: String? = null,
    val unit: String? = null,
    val axis: String? = null,
    val axisId: String? = null,
    val seriesType: String? = null,
    val dot: Boolean? = null,
    val strokeDasharray: String? = null,
    val opacity: Double? = null,
    val strokeWidth: Double? = null,
)

data class MobileChartRuntime(
    val chartId: String,
    val chartType: MobileChartType,
    val title: String,
    val description: String? = null,
    val data: List<Map<String, JsonElement>> = emptyList(),
    val xField: String,
    val xFieldLabel: String? = null,
    val yFields: List<MobileChartYField> = emptyList(),
    val options: JsonElement? = null,
)