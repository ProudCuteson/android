package top.role.android.feature.browse.pdf

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
import kotlinx.coroutines.Dispatchers
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

    LaunchedEffect(file) {
        loading = true
        error = null

        try {
            session = withContext(Dispatchers.IO) {
                PdfDocumentSession.open(file)
            }
        } catch (e: Exception) {
            error = e.message ?: "PDF 打开失败"
        } finally {
            loading = false
        }
    }

    DisposableEffect(session) {
        onDispose {
            session?.close()
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

            when {
                loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(
                            Alignment.Center
                        )
                    )
                }

                error != null -> {
                    Text(
                        text = error.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(
                            Alignment.Center
                        ),
                    )
                }

                session != null -> {
                    val currentSession = session!!

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
        }

        var bitmap by remember(
            session,
            index,
            targetWidth,
        ) {
            mutableStateOf<android.graphics.Bitmap?>(null)
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
            try {
                bitmap = withContext(Dispatchers.IO) {
                    session.renderPage(
                        index = index,
                        targetWidth = targetWidth,
                    )
                }
            } catch (e: Exception) {
                error = true
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp,
        ) {
            when {
                bitmap != null -> {
                    Image(
                        bitmap = bitmap!!.asImageBitmap(),
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
