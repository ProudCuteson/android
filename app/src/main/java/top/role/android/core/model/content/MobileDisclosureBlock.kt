package top.role.android.core.model.content

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MobileDisclosureTitle(
    override val id: String,
    override val sourceId: String? = null,

    val inlines: List<MobileInline>,
) : MobileNodeIdentity

@Serializable
@SerialName("disclosure")
data class MobileDisclosureBlock(
    override val id: String,
    override val sourceId: String? = null,

    val defaultOpen: Boolean? = null,

    val title: MobileDisclosureTitle,

    val children: List<MobileRenderBlock>,
) : MobileRenderBlock