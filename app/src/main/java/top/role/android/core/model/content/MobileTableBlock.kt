package top.role.android.core.model.content

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MobileTableLineList(
    val type: String,
    val indent: Double? = null,
    val start: Double? = null,
    val checked: Boolean? = null,
)

@Serializable
data class MobileTableLine(
    override val id: String,
    override val sourceId: String? = null,

    val variant: String? = null,

    val list: MobileTableLineList? = null,

    val align: String? = null,
    val lineHeight: Double? = null,

    val inlines: List<MobileInline>,
) : MobileNodeIdentity

@Serializable
data class MobileTableCell(
    override val id: String,
    override val sourceId: String? = null,

    val row: Double,
    val col: Double,

    val rowSpan: Double,
    val colSpan: Double,

    val align: String? = null,
    val verticalAlign: String? = null,

    val lines: List<MobileTableLine>,
) : MobileNodeIdentity

@Serializable
data class MobileTableHeader(
    val top: Boolean? = null,
    val left: Boolean? = null,
)

@Serializable
@SerialName("table")
data class MobileTableBlock(
    override val id: String,
    override val sourceId: String? = null,

    val rows: Double,
    val cols: Double,

    val size: MobileContentSize? = null,

    val columnWidths: List<Double>? = null,
    val rowHeights: List<Double>? = null,

    val tableWidthPct: Double? = null,
    val minRowHeight: Double? = null,
    val minTableWidth: Double? = null,

    val header: MobileTableHeader? = null,

    val cells: List<MobileTableCell>,
) : MobileRenderBlock