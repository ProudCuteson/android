package top.role.android.core.model.slide

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import java.util.UUID

@Serializable
data class MobileSlideDetailResponse(
    val viewer: MobileSlideViewer,
    val slide: MobileSlideSummary,
    val pages: List<MobileSlidePage> = emptyList(),
)

@Serializable
data class MobileSlideViewer(
    val isAuthed: Boolean = false,
    val userId: String? = null,
    val canAsk: Boolean = false,
    val canEdit: Boolean = false,
)

@Serializable
data class MobileSlideSummary(
    val slideId: String,

    val title: String,
    val description: String? = null,
    val cover: String? = null,

    val status: String? = null,
    val visibility: String? = null,

    val publishedAt: Double? = null,
    val updatedAt: Double? = null,

    val version: Double? = null,

    val canvasPreset: String? = null,

    val canvasW: Double,
    val canvasH: Double,
)

@Serializable
data class MobileSlidePage(
    val id: String,

    val order: Double = 0.0,
    val index: Double = 0.0,

    val layout: String? = null,
    val notes: String? = null,

    val isLocked: Boolean = false,

    val level: String? = null,
    val relatedDocId: String? = null,

    val background: MobileSlidePageBackground =
        MobileSlidePageBackground(),

    val elements: List<MobileSlideElement> =
        emptyList(),
)

@Serializable
data class MobileSlidePageBackground(
    val kind: String = "none",

    val color: String? = null,

    val imageId: String? = null,
    val imageFit: String? = null,
    val imageOpacity: Double? = null,

    val gradientPreset: String? = null,
)

@Serializable
data class MobileSlideRect(
    val x: Double = 0.0,
    val y: Double = 0.0,
    val w: Double = 0.0,
    val h: Double = 0.0,
)

@Serializable
enum class MobileSlideElementKind {

    @SerialName("text")
    Text,

    @SerialName("image")
    Image,

    @SerialName("shape")
    Shape,

    @SerialName("table")
    Table,

    @SerialName("chart")
    Chart,

    @SerialName("group")
    Group,

    @SerialName("box")
    Box,

    @SerialName("connector")
    Connector,

    @SerialName("unsupported")
    Unsupported,
}

@Serializable
data class MobileSlideElement(
    val id: String =
        UUID.randomUUID().toString(),

    val type: MobileSlideElementKind =
        MobileSlideElementKind.Unsupported,

    val originalType: String? = null,
    val reason: String? = null,

    val rect: MobileSlideRect =
        MobileSlideRect(),

    val rotation: Double = 0.0,

    val zIndex: Double = 0.0,

    // text
    val content: MobileSlideTextContent? = null,

    // image
    val src: String? = null,
    val imageId: String? = null,
    val variantKind: String? = null,
    val alt: String? = null,

    val objectFit: String? = null,

    val focalX: Double? = null,
    val focalY: Double? = null,

    val borderRadius: Double? = null,
    val borderWidth: Double? = null,
    val borderColor: String? = null,

    val boxShadow: String? = null,

    val opacity: Double? = null,

    // shape
    val shapeType: String? = null,

    val fillColor: String? = null,

    val strokeColor: String? = null,
    val strokeWidth: Double? = null,

    val dashStyle: String? = null,

    val cornerRadius: Double? = null,

    val shadow: JsonElement? = null,

    val arrowHeadStart: Boolean? = null,
    val arrowHeadEnd: Boolean? = null,

    val arrowHeadStyle: String? = null,

    val label: JsonElement? = null,

    // table
    val rows: Double? = null,
    val cols: Double? = null,

    val columnWidths: List<Double>? = null,
    val rowHeights: List<Double>? = null,

    val header: MobileSlideTableHeader? = null,

    val cells:
    Map<String, MobileSlideTableCell>? =
        null,

    // chart
    val chartId: String? = null,

    val showTitle: Boolean? = null,

    val backgroundMode: String? = null,

    val chart:
    MobileSlideChartRuntimeConfig? =
        null,

    // group / box
    val direction: String? = null,

    val gap: Double? = null,

    val children:
    List<MobileSlideElement>? =
        null,

    // connector
    val from: JsonElement? = null,
    val to: JsonElement? = null,

    val routing: String? = null,

    val graphId: String? = null,
)