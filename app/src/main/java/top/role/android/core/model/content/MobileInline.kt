package top.role.android.core.model.content

import kotlinx.serialization.Serializable

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
    override val id: String,
    override val sourceId: String? = null,

    val text: String,

    val bold: Boolean? = null,
    val italic: Boolean? = null,
    val underline: Boolean? = null,
    val strikethrough: Boolean? = null,
    val code: Boolean? = null,

    val color: String? = null,
    val bgColor: String? = null,
    val fontSize: Double? = null,
    val fontFamily: String? = null,

    val subscript: Boolean? = null,
    val superscript: Boolean? = null,

    val link: MobileInlineLink? = null,
    val mention: MobileInlineMention? = null,
) : MobileNodeIdentity