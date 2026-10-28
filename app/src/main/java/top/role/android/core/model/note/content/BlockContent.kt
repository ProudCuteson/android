package top.role.android.core.model.note.content

import kotlinx.serialization.Serializable

@Serializable
data class BlockContent(
    val v: Int = 1,
    val lines: List<BlockLine> = emptyList(),
)

@Serializable
data class BlockLine(
    val id: String,
    val variant: String? = null,
    val list: BlockList? = null,
    val align: String? = null,
    val lineHeight: Double? = null,
    val runs: List<BlockRun> = emptyList(),
)

@Serializable
data class BlockRun(
    val id: String,
    val text: String,
    val mark: BlockMark? = null,
)

@Serializable
data class BlockMark(
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
)

@Serializable
data class BlockList(
    val type: String,
    val indent: Double? = null,
    val start: Double? = null,
    val checked: Boolean? = null,
)
