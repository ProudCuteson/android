package top.role.android.feature.reading.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import top.role.android.core.model.content.MobileImageBlock

@Composable
fun MobileImageBlockView(
    block: MobileImageBlock,
    modifier: Modifier = Modifier,
) {
    val ratio =
        block.ratio
            ?.takeIf { it > 0 }
            ?.toFloat()

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        if (ratio != null) {
            SubcomposeAsyncImage(
                model = block.src,
                contentDescription =
                    block.alt
                        ?.takeIf { it.isNotBlank() },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(ratio)
                    .clip(
                        RoundedCornerShape(12.dp)
                    ),
                contentScale =
                    ContentScale.Crop,
                loading = {
                    ImageLoadingPlaceholder()
                },
                error = {
                    ImageErrorPlaceholder()
                },
                success = {
                    SubcomposeAsyncImageContent()
                },
            )
        } else {
            AsyncImage(
                model = block.src,
                contentDescription =
                    block.alt
                        ?.takeIf { it.isNotBlank() },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(12.dp)
                    ),
                contentScale =
                    ContentScale.FillWidth,
            )
        }

        block.caption
            ?.takeIf { it.isNotEmpty() }
            ?.let { caption ->
                MobileInlineText(
                    inlines = caption,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 8.dp,
                            start = 4.dp,
                            end = 4.dp,
                        ),
                    baseStyle =
                        MaterialTheme.typography
                            .bodySmall.copy(
                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .onSurfaceVariant,
                            ),
                    textAlign =
                        resolveMobileTextAlign(
                            block.align
                        ),
                )
            }
    }
}

@Composable
private fun ImageLoadingPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme
                    .surfaceContainerLow
            ),
        contentAlignment =
            Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ImageErrorPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme
                    .surfaceContainerLow
            ),
    )
}