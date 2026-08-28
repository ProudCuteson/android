package top.role.android.feature.chat.conversation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

@Composable
internal fun ChatImagePreview(
    imageId: String,
    kind: String,
    repository: ChatImagePreviewRepository,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
) {
    var url by
    remember(
        imageId,
        kind,
    ) {
        mutableStateOf<String?>(
            null
        )
    }

    var loading by
    remember(
        imageId,
        kind,
    ) {
        mutableStateOf(
            true
        )
    }

    var failed by
    remember(
        imageId,
        kind,
    ) {
        mutableStateOf(
            false
        )
    }

    LaunchedEffect(
        imageId,
        kind,
    ) {
        loading = true
        failed = false
        url = null

        runCatching {
            repository.getPreviewUrl(
                imageId = imageId,
                kind = kind,
            )
        }
            .onSuccess { result ->
                url =
                    result.url
                        ?.takeIf {
                            it.isNotBlank()
                        }

                failed =
                    url == null

                loading = false
            }
            .onFailure {
                url = null
                failed = true
                loading = false
            }
    }

    Box(
        modifier =
            modifier,
        contentAlignment =
            Alignment.Center,
    ) {
        when {
            loading -> {
                CircularProgressIndicator()
            }

            url != null -> {
                AsyncImage(
                    model =
                        url,

                    contentDescription =
                        contentDescription,

                    contentScale =
                        contentScale,

                    modifier =
                        Modifier.fillMaxSize(),
                )
            }

            failed -> {
                Text(
                    text =
                        "图片加载失败",

                    style =
                        MaterialTheme
                            .typography
                            .labelSmall,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,
                )
            }
        }
    }
}