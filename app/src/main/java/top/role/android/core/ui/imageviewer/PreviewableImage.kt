package top.role.android.core.ui.imageviewer

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

@Composable
fun PreviewableImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale =
        ContentScale.Crop,
) {
    val url =
        imageUrl
            ?.takeIf {
                it.isNotBlank()
            }
            ?: return

    var previewing by
    remember(url) {
        mutableStateOf(false)
    }

    AsyncImage(
        model = url,
        contentDescription =
            contentDescription,
        modifier = modifier
            .clickable {
                previewing = true
            },
        contentScale =
            contentScale,
    )

    if (previewing) {
        ImageViewerDialog(
            imageUrl = url,
            onDismiss = {
                previewing = false
            },
        )
    }
}