package top.role.android.core.model.content

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
data class MobileDocInfo(
    val id: String,
    val title: String,
    val description: String? = null,
    val coverUrl: String? = null,
    val updatedAt: Double? = null,
    val publishedAt: Double? = null,
)

@Serializable
data class MobileProfileRef(
    val id: String,
    val name: String,
    val avatarUrl: String? = null,
    val subtitle: String? = null,
    val number: Double? = null,
)

@Serializable
data class MobileDocSpaceRef(
    val id: String,
    val number: Double? = null,
    val name: String? = null,
    val avatarUrl: String? = null,
)

@Serializable
data class MobileDocDetailResponse(
    val doc: MobileDocInfo? = null,
    val userId: String? = null,
    val author: MobileProfileRef? = null,
    val space: MobileDocSpaceRef? = null,
    val renderDoc: MobileRenderDoc? = null,
)

@Serializable
data class MobileRenderDoc(
    val version: Double,
    val blocks: List<MobileRenderBlock> = emptyList(),
)

@Serializable
data class MobileInlineLink(
    val href: String,
    val title: String? = null,
    val openInNewTab: Boolean? = null,
)

@Serializable
data class MobileInlineMention(
    val targetType: String,
    val targetId: String,
    val label: String,
)

@Serializable
data class MobileInline(
    val text: String,

    val bold: Boolean? = null,
    val italic: Boolean? = null,
    val underline: Boolean? = null,
    val strikethrough: Boolean? = null,
    val code: Boolean? = null,

    val color: String? = null,
    val bgColor: String? = null,
    val fontSize: Double? = null,

    val subscript: Boolean? = null,
    val superscript: Boolean? = null,

    val link: MobileInlineLink? = null,
    val mention: MobileInlineMention? = null,
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("kind")
sealed interface MobileRenderBlock

@Serializable
@SerialName("text")
data class MobileTextBlock(
    val role: String,
    val level: Double? = null,
    val align: String,
    val inlines: List<MobileInline> = emptyList(),
    val hasRichMark: Boolean? = null,
) : MobileRenderBlock

@Serializable
@SerialName("quote")
data class MobileQuoteBlock(
    val align: String,
    val inlines: List<MobileInline> = emptyList(),
) : MobileRenderBlock

@Serializable
data class MobileListItem(
    val checked: Boolean? = null,
    val inlines: List<MobileInline> = emptyList(),
)

@Serializable
@SerialName("list")
data class MobileListBlock(
    val align: String,
    val ordered: Boolean? = null,
    val items: List<MobileListItem> = emptyList(),
) : MobileRenderBlock

@Serializable
@SerialName("image")
data class MobileImageBlock(
    val align: String? = null,
    val src: String,
    val previewSrc: String? = null,
    val ratio: Double? = null,
    val alt: String? = null,
    val caption: List<MobileInline>? = null,
) : MobileRenderBlock

@Serializable
@SerialName("code")
data class MobileCodeBlock(
    val align: String,
    val language: String? = null,
    val lines: List<String> = emptyList(),
) : MobileRenderBlock

@Serializable
data class MobileTableCell(
    val text: String,
    val align: String? = null,
    val rowSpan: Double? = null,
    val colSpan: Double? = null,
    val inlines: List<MobileInline>? = null,
)

@Serializable
data class MobileTableRow(
    val cells: List<MobileTableCell> = emptyList(),
)

@Serializable
@SerialName("table")
data class MobileTableBlock(
    val align: String,
    val rows: List<MobileTableRow> = emptyList(),
    val columnWidths: List<Double>? = null,
    val rowHeights: List<Double>? = null,
) : MobileRenderBlock

@Serializable
@SerialName("chart")
data class MobileChartBlock(
    val align: String? = null,
    val chartId: String,
    val title: String? = null,
    val caption: String? = null,
    val height: Double? = null,
) : MobileRenderBlock