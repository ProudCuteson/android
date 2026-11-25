package top.role.android.feature.browse.pdf

import android.graphics.Bitmap
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfReaderScreen(
    title: String,
    file: File,
    onBack: () -> Unit,
) {
    var session by remember(file) {
        mutableStateOf<PdfDocumentSession?>(null)
    }

    var loading by remember(file) {
        mutableStateOf(true)
    }

    var error by remember(file) {
        mutableStateOf<String?>(null)
    }

    BackHandler(onBack = onBack)

    /**
     * Session 的完整生命周期由同一个 Effect 管理。
     *
     * 页面退出或文件变化时，
     * Effect 被取消，并负责释放自己的 Session。
     */
    LaunchedEffect(file) {

        var openedSession: PdfDocumentSession? = null

        session = null
        loading = true
        error = null

        try {
            withContext(Dispatchers.IO) {
                openedSession = PdfDocumentSession.open(file)
            }

            session = openedSession
            loading = false

            // 保持 Session 有效，直到页面退出或文件变化。
            awaitCancellation()

        } catch (e: CancellationException) {
            throw e

        } catch (e: Exception) {
            Log.e(
                "PdfReaderScreen",
                "PDF 打开失败",
                e,
            )

            error = "PDF 打开失败"
            loading = false

        } finally {
            val closingSession = openedSession

            if (closingSession != null) {
                withContext(
                    NonCancellable + Dispatchers.IO
                ) {
                    runCatching {
                        closingSession.close()
                    }.onFailure {
                        Log.e(
                            "PdfReaderScreen",
                            "PDF Session 关闭失败",
                            it,
                        )
                    }
                }
            }
        }
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
                .padding(padding)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant
                ),
        ) {
            val currentSession = session

            when {
                loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(
                            Alignment.Center
                        ),
                    )
                }

                error != null -> {
                    Text(
                        text = error.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                    )
                }

                currentSession != null -> {

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            vertical = 12.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(
                            12.dp,
                        ),
                    ) {
                        items(
                            count = currentSession.pageCount,
                            key = { index -> index },
                        ) { index ->

                            PdfPageItem(
                                session = currentSession,
                                index = index,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 单个 PDF 页面的渲染与展示。
 */
@Composable
private fun PdfPageItem(
    session: PdfDocumentSession,
    index: Int,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
    ) {
        val density = LocalDensity.current

        val targetWidth = with(density) {
            maxWidth.roundToPx()
        }.coerceAtLeast(1)

        var bitmap by remember(
            session,
            index,
            targetWidth,
        ) {
            mutableStateOf<Bitmap?>(null)
        }

        var error by remember(
            session,
            index,
            targetWidth,
        ) {
            mutableStateOf(false)
        }

        LaunchedEffect(
            session,
            index,
            targetWidth,
        ) {
            var renderedBitmap: Bitmap? = null
            var published = false

            try {
                withContext(Dispatchers.IO) {
                    renderedBitmap = session.renderPage(
                        index = index,
                        targetWidth = targetWidth,
                    )
                }

                bitmap = renderedBitmap
                published = true

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                Log.e(
                    "PdfPageItem",
                    "PDF 第 ${index + 1} 页渲染失败",
                    e,
                )

                error = true

            } finally {
                // 仅释放尚未交给 UI 的 Bitmap。
                if (!published) {
                    renderedBitmap?.recycle()
                }
            }
        }

        val currentBitmap = bitmap

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp,
        ) {
            when {
                currentBitmap != null -> {
                    Image(
                        bitmap = currentBitmap.asImageBitmap(),
                        contentDescription = "PDF 第 ${index + 1} 页",
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.FillWidth,
                    )
                }

                error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "第 ${index + 1} 页加载失败",
                            color = Color.DarkGray,
                        )
                    }
                }

                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}