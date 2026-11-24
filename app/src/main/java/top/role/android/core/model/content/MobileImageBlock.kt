package top.role.android.core.model.content

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MobileImage(
    override val id: String,
    override val sourceId: String? = null,

    val src: String,
    val imageId: String? = null,
    val alt: String? = null,
    val ratio: Double? = null,
) : MobileNodeIdentity

@Serializable
data class MobileCaption(
    override val id: String,
    override val sourceId: String? = null,

    val align: String,
    val inlines: List<MobileInline>,
) : MobileNodeIdentity

@Serializable
data class MobileContentSize(
    val width: Double? = null,
    val height: Double? = null,
)

@Serializable
@SerialName("image")
data class MobileImageBlock(
    override val id: String,
    override val sourceId: String? = null,

    val src: String,
    val imageId: String? = null,
    val alt: String? = null,
    val ratio: Double? = null,

    val align: String,
) : MobileRenderBlock

@Serializable
@SerialName("image-figure")
data class MobileImageFigureBlock(
    override val id: String,
    override val sourceId: String? = null,

    val align: String,

    val size: MobileContentSize? = null,

    val cropMode: String? = null,
    val cropFocus: String? = null,

    val image: MobileImage,
    val caption: MobileCaption? = null,
) : MobileRenderBlock