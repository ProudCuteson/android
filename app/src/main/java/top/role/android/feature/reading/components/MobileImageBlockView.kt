package top.role.android.feature.reading.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent

import top.role.android.core.model.content.MobileImageBlock
import top.role.android.core.model.content.MobileImageFigureBlock

/**
 * 普通图片 Block。
 */
@Composable
fun MobileImageBlockView(
    block: MobileImageBlock,
    modifier: Modifier = Modifier,
) {
    MobileReadingImage(
        src = block.src,
        alt = block.alt,
        ratio = block.ratio,
        modifier = modifier,
    )
}

/**
 * 带布局、裁剪及图注的图片 Block。
 */
@Composable
fun MobileImageFigureBlockView(
    block: MobileImageFigureBlock,
    modifier: Modifier = Modifier,
) {
    val ratio = resolveFigureImageRatio(block)

    val isCropped =
        block.cropMode == "16:9" ||
                block.cropMode == "4:3"

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment =
            resolveImageHorizontalAlignment(
                block.align
            ),
    ) {
        MobileReadingImage(
            src = block.image.src,
            alt = block.image.alt,
            ratio = ratio,
            crop = isCropped,
            cropFocus = block.cropFocus,
        )

        block.caption?.let { caption ->

            if (caption.inlines.isNotEmpty()) {
                MobileInlineText(
                    inlines = caption.inlines,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 8.dp,
                            start = 4.dp,
                            end = 4.dp,
                        ),
                    baseStyle = readingCaptionStyle(),
                    textAlign =
                        resolveMobileTextAlign(
                            caption.align
                        ),
                )
            }
        }
    }
}

/**
 * 公共阅读图片组件。
 *
 * 普通 Image 和 ImageFigure 共用。
 */
@Composable
private fun MobileReadingImage(
    src: String,
    alt: String?,
    ratio: Double?,
    modifier: Modifier = Modifier,
    crop: Boolean = false,
    cropFocus: String? = null,
) {
    val validRatio =
        ratio
            ?.takeIf {
                it.isFinite() && it > 0
            }
            ?.toFloat()
            ?.takeIf {
                it.isFinite() && it > 0f
            }

    val imageModifier =
        modifier
            .fillMaxWidth()
            .then(
                if (validRatio != null) {
                    Modifier.aspectRatio(validRatio)
                } else {
                    Modifier
                }
            )
            .clip(
                RoundedCornerShape(12.dp)
            )

    if (src.isBlank()) {
        ImageErrorPlaceholder(
            modifier = imageModifier,
        )
        return
    }

    SubcomposeAsyncImage(
        model = src,

        contentDescription =
            alt?.takeIf { it.isNotBlank() },

        modifier = imageModifier,

        contentScale =
            when {
                crop ->
                    ContentScale.Crop

                validRatio != null ->
                    ContentScale.Fit

                else ->
                    ContentScale.FillWidth
            },

        alignment =
            if (crop) {
                resolveImageCropAlignment(
                    cropFocus
                )
            } else {
                Alignment.Center
            },

        loading = {
            ImageLoadingPlaceholder(
                modifier =
                    if (validRatio != null) {
                        Modifier.fillMaxSize()
                    } else {
                        Modifier.heightIn(
                            min = 120.dp
                        )
                    }
            )
        },

        error = {
            ImageErrorPlaceholder(
                modifier =
                    if (validRatio != null) {
                        Modifier.fillMaxSize()
                    } else {
                        Modifier.heightIn(
                            min = 120.dp
                        )
                    }
            )
        },

        success = {
            SubcomposeAsyncImageContent()
        },
    )
}

/**
 * ImageFigure 的宽高比。
 *
 * 显式裁剪比例优先，其次是作者指定的尺寸，
 * 最后使用图片本身的比例。
 */
private fun resolveFigureImageRatio(
    block: MobileImageFigureBlock,
): Double? {
    when (block.cropMode) {
        "16:9" -> return 16.0 / 9.0
        "4:3" -> return 4.0 / 3.0
    }

    val width = block.size?.width
    val height = block.size?.height

    if (
        width != null &&
        height != null &&
        width.isFinite() &&
        height.isFinite() &&
        width > 0 &&
        height > 0
    ) {
        return width / height
    }

    return block.image.ratio
}

/**
 * 图片容器的水平对齐。
 */
private fun resolveImageHorizontalAlignment(
    align: String?,
): Alignment.Horizontal {
    return when (align) {
        "left" -> Alignment.Start
        "right" -> Alignment.End

        "center",
        "wide",
        "full" -> Alignment.CenterHorizontally

        else -> Alignment.CenterHorizontally
    }
}

/**
 * 裁剪时保留的图片区域。
 */
private fun resolveImageCropAlignment(
    focus: String?,
): Alignment {
    return when (focus) {
        "top" -> Alignment.TopCenter
        "bottom" -> Alignment.BottomCenter
        else -> Alignment.Center
    }
}

@Composable
private fun ImageLoadingPlaceholder(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .background(
                MaterialTheme.colorScheme
                    .surfaceContainerLow
            ),

        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp,
        )
    }
}

@Composable
private fun ImageErrorPlaceholder(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .background(
                MaterialTheme.colorScheme
                    .surfaceContainerLow
            ),

        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "图片暂不可用",
            style = MaterialTheme.typography.bodySmall,
            color =
                MaterialTheme.colorScheme
                    .onSurfaceVariant,
        )
    }
}