package top.role.android.feature.slide.render

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import top.role.android.core.model.slide.MobileSlideElement

@Composable
fun SlideImageElement(
    element: MobileSlideElement,
    scale: Double,
    modifier: Modifier = Modifier,
) {
    val density =
        LocalDensity.current

    val borderRadiusDp =
        with(density) {
            (
                    (element.borderRadius ?: 0.0) *
                            scale
                    )
                .toFloat()
                .toDp()
        }

    val borderWidthDp =
        with(density) {
            (
                    (element.borderWidth ?: 0.0) *
                            scale
                    )
                .toFloat()
                .toDp()
        }

    val shape =
        RoundedCornerShape(
            borderRadiusDp
        )

    val borderColor =
        parseSlideColor(
            element.borderColor
        )
            ?: MaterialTheme.colorScheme.outlineVariant

    val opacity =
        (element.opacity ?: 1.0)
            .coerceIn(
                0.0,
                1.0,
            )
            .toFloat()

    val contentScale =
        resolveImageContentScale(
            element.objectFit
        )

    val src =
        element.src
            ?.trim()
            ?.takeIf {
                it.isNotEmpty()
            }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(shape)
            .alpha(opacity)
            .let { base ->
                if (
                    element.borderWidth != null &&
                    element.borderWidth > 0.0
                ) {
                    base.border(
                        width =
                            borderWidthDp,
                        color =
                            borderColor,
                        shape =
                            shape,
                    )
                } else {
                    base
                }
            },
        contentAlignment =
            Alignment.Center,
    ) {
        if (src == null) {
            SlideImagePlaceholder(
                isError = false,
            )

            return@Box
        }

        SubcomposeAsyncImage(
            model = src,
            contentDescription =
                element.alt,
            modifier =
                Modifier.fillMaxSize(),
            contentScale =
                contentScale,
            loading = {
                SlideImagePlaceholder(
                    isError = false,
                )
            },
            error = {
                SlideImagePlaceholder(
                    isError = true,
                )
            },
            success = {
                SubcomposeAsyncImageContent()
            },
        )
    }
}

@Composable
private fun SlideImagePlaceholder(
    isError: Boolean,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme
                    .surfaceContainerHighest
            ),
        contentAlignment =
            Alignment.Center,
    ) {
        Icon(
            imageVector =
                if (isError) {
                    Icons.Outlined.BrokenImage
                } else {
                    Icons.Outlined.Image
                },
            contentDescription =
                null,
            tint =
                MaterialTheme.colorScheme
                    .onSurfaceVariant,
        )
    }
}

private fun resolveImageContentScale(
    objectFit: String?,
): ContentScale {
    return when (
        objectFit
            ?.trim()
            ?.lowercase()
    ) {
        "contain",
        "scale-down" ->
            ContentScale.Fit

        "fill" ->
            ContentScale.FillBounds

        "none" ->
            ContentScale.Fit

        else ->
            ContentScale.Crop
    }
}