package top.role.android.core.model.slide

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
enum class MobileSlideChartType {

    @SerialName("area")
    Area,

    @SerialName("bar")
    Bar,

    @SerialName("line")
    Line,

    @SerialName("pie")
    Pie,

    @SerialName("radar")
    Radar,

    @SerialName("radial")
    Radial,

    @SerialName("scatter")
    Scatter,

    @SerialName("composed")
    Composed,
}

@Serializable
data class MobileSlideChartRuntimeConfig(
    val chartId: String,

    val chartType:
    MobileSlideChartType,

    val title: String,

    val description: String? = null,

    val data:
    List<Map<String, JsonElement>> =
        emptyList(),

    val xField: String,

    val xFieldLabel: String? = null,

    val yFields:
    List<MobileSlideChartYField> =
        emptyList(),

    val options: JsonElement? = null,
)

@Serializable
data class MobileSlideChartYField(
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