package top.role.android.feature.browse.pdf

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfReaderRouteScreen(
    title: String,
    url: String,
    assetId: String,
    loader: PdfDocumentLoader,
    onBack: () -> Unit,
) {
    var file by remember(url, assetId) {
        mutableStateOf<File?>(null)
    }

    var loading by remember(url, assetId) {
        mutableStateOf(true)
    }

    var error by remember(url, assetId) {
        mutableStateOf<String?>(null)
    }

    var retryToken by remember(url, assetId) {
        mutableStateOf(0)
    }

    BackHandler {
        onBack()
    }

    LaunchedEffect(url, assetId, retryToken) {
        file = null
        error = null
        loading = true

        try {
            require(url.isNotBlank()) {
                "PDF 下载地址为空"
            }

            require(assetId.isNotBlank()) {
                "PDF 资源 ID 为空"
            }

            file = loader.download(
                url = url,
                cacheKey = assetId,
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            error = e.message ?: "PDF 下载失败"
        } finally {
            loading = false
        }
    }

    val downloadedFile = file

    if (downloadedFile != null) {
        PdfReaderScreen(
            title = title,
            file = downloadedFile,
            onBack = onBack,
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        maxLines = 1,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector =
                                Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                        )
                    }
                },
            )
        },
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            when {
                loading -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CircularProgressIndicator()

                        Text(
                            text = "正在加载 PDF…",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                error != null -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(
                            text = error.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                        )

                        Button(
                            onClick = {
                                retryToken++
                            },
                        ) {
                            Text("重试")
                        }
                    }
                }
            }
        }
    }
}
