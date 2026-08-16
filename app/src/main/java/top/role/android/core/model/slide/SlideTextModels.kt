package top.role.android.core.model.slide

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class MobileSlideTextContent(
    val v: Double = 1.0,
    val lines: List<MobileSlideTextLine> =
        emptyList(),
)

@Serializable
data class MobileSlideTextLine(
    val id: String =
        UUID.randomUUID().toString(),

    val variant: String? = null,

    val list: MobileSlideTextList? = null,

    val align: String? = null,

    val lineHeight: Double? = null,

    val runs: List<MobileSlideTextRun> =
        emptyList(),
)

@Serializable
data class MobileSlideTextList(
    val type: String = "bullet",

    val indent: Double? = null,

    val start: Double? = null,

    val checked: Boolean? = null,
)

@Serializable
data class MobileSlideTextRun(
    val id: String =
        UUID.randomUUID().toString(),

    val text: String = "",

    val mark: MobileSlideTextMark? = null,
)

@Serializable
data class MobileSlideTextMark(
    val bold: Boolean? = null,

    val italic: Boolean? = null,

    val underline: Boolean? = null,

    val strikethrough: Boolean? = null,

    val code: Boolean? = null,

    val color: String? = null,

    val bgColor: String? = null,

    val fontSize: Double? = null,

    val fontFamily: String? = null,

    @SerialName("subscript")
    val subscriptMark: Boolean? = null,

    val superscript: Boolean? = null,
)