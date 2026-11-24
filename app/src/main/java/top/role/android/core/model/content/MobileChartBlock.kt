package top.role.android.core.model.content

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive

@Serializable
@SerialName("chart")
data class MobileChartBlock(
    override val id: String,
    override val sourceId: String? = null,

    val chartId: String,

    val title: String? = null,
    val caption: String? = null,

    val align: String? = null,

    val width: JsonPrimitive? = null,
    val height: Double? = null,
) : MobileRenderBlock