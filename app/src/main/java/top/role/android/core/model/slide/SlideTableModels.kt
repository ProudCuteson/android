package top.role.android.core.model.slide

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class MobileSlideTableHeader(
    val top: Boolean? = null,
    val left: Boolean? = null,
)

@Serializable
data class MobileSlideTableCell(
    val id: String =
        UUID.randomUUID().toString(),

    val type: String? = null,

    val content:
    MobileSlideTableCellTextElement? =
        null,

    val anchorRow: Double = 0.0,
    val anchorCol: Double = 0.0,

    val rowSpan: Double? = null,
    val colSpan: Double? = null,

    val align: String? = null,
    val verticalAlign: String? = null,
)

@Serializable
data class MobileSlideTableCellTextElement(
    val id: String? = null,

    val type: String? = null,

    val rect: MobileSlideRect? = null,

    val rotation: Double? = null,

    val zIndex: Double? = null,

    val content:
    MobileSlideTextContent? =
        null,
)