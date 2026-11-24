package top.role.android.core.model.content

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MobileCodeLine(
    override val id: String,
    override val sourceId: String? = null,

    val inlines: List<MobileInline>,
) : MobileNodeIdentity

@Serializable
@SerialName("code")
data class MobileCodeBlock(
    override val id: String,
    override val sourceId: String? = null,

    val align: String,

    val language: String? = null,
    val wrap: Boolean? = null,
    val showLineNumbers: Boolean? = null,
    val startLine: Double? = null,

    val lines: List<MobileCodeLine>,
) : MobileRenderBlock