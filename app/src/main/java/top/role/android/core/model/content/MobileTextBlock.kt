package top.role.android.core.model.content

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Paragraph / Heading / Caption
 */
@Serializable
@SerialName("text")
data class MobileTextBlock(
    override val id: String,
    override val sourceId: String? = null,

    val role: String,
    val align: String,

    val level: Double? = null,
    val empty: Boolean? = null,

    val inlines: List<MobileInline>,
) : MobileRenderBlock

/**
 * Quote
 */
@Serializable
@SerialName("quote")
data class MobileQuoteBlock(
    override val id: String,
    override val sourceId: String? = null,

    val align: String,

    val inlines: List<MobileInline>,
) : MobileRenderBlock

/**
 * List Item
 */
@Serializable
data class MobileListItem(
    override val id: String,
    override val sourceId: String? = null,

    val indent: Double? = null,
    val checked: Boolean? = null,

    val inlines: List<MobileInline>,
) : MobileNodeIdentity

/**
 * Bulleted / Numbered / Todo
 */
@Serializable
@SerialName("list")
data class MobileListBlock(
    override val id: String,
    override val sourceId: String? = null,

    val listKind: String,
    val align: String,

    val items: List<MobileListItem>,
) : MobileRenderBlock

/**
 * Divider
 */
@Serializable
@SerialName("divider")
data class MobileDividerBlock(
    override val id: String,
    override val sourceId: String? = null,

    val spacingBefore: Double? = null,
    val spacingAfter: Double? = null,
) : MobileRenderBlock

/**
 * Soft Break
 */
@Serializable
@SerialName("soft-break")
data class MobileSoftBreakBlock(
    override val id: String,
    override val sourceId: String? = null,

    val isInline: Boolean? = null,
) : MobileRenderBlock