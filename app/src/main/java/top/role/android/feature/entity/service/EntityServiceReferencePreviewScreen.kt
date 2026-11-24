package top.role.android.feature.entity.service

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.serialization.builtins.ListSerializer

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull

import top.role.android.core.model.note.content.BlockContent
import top.role.android.core.model.content.MobileRenderBlock
import top.role.android.core.model.slide.MobileSlidePage
import top.role.android.core.model.slide.MobileSlideSummary
import top.role.android.core.ui.imageviewer.PreviewableImage

import top.role.android.feature.note.editor.richtext.RichTextReadingView
import top.role.android.feature.reading.components.MobileRenderBlockView
import top.role.android.feature.slide.render.SlidePageCanvas

private val referencePreviewJson = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
}

/**
 * Space Service 关联内容只读预览。
 */
@Composable
fun EntityServiceReferencePreviewScreen(
    state: EntityServiceReferencePreviewUiState,
    onRetry: () -> Unit,
    onRetryMediaPreview: (String) -> Unit,
    onBack: () -> Unit,
) {
    val payload = state.content

    val title = payload?.stringValue("title")
        ?: "关联内容"

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {

        // 顶部导航
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = onBack,
            ) {
                Text("返回")
            }

            Text(
                text = title,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
        }

        HorizontalDivider()

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            state.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = state.errorMessage,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(Modifier.height(16.dp))

                    Button(onClick = onRetry) {
                        Text("重试")
                    }
                }
            }

            payload != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                ) {
                    ReferenceContentBody(
                        payload = payload,
                        media = state.media,
                        onRetryMediaPreview = onRetryMediaPreview,
                    )
                }
            }

            else -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("暂无可展示内容")
                }
            }
        }
    }
}

/**
 * 按内容类型分发。
 */
@Composable
private fun ReferenceContentBody(
    payload: JsonObject,
    media: List<EntityServiceReferenceMediaUi>,
    onRetryMediaPreview: (String) -> Unit,
) {
    val description = payload.stringValue("description")

    if (!description.isNullOrBlank()) {
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp),
        )
    }

    when (payload.stringValue("type")) {

        "note" -> {
            NoteReferenceContent(
                payload = payload,
                media = media,
                onRetryMediaPreview = onRetryMediaPreview,
            )
        }

        "doc" -> {
            DocReferenceContent(
                payload = payload,
            )
        }

        "slide" -> {
            SlideReferenceContent(
                payload = payload,
            )
        }

        "fileAsset" -> {
            PdfReferenceContent(
                payload = payload,
            )
        }

        else -> {
            Text("暂不支持此内容类型")
        }
    }
}

/**
 * Note
 *
 * 正文和图片是两条独立的内容链路。
 */
@Composable
private fun NoteReferenceContent(
    payload: JsonObject,
    media: List<EntityServiceReferenceMediaUi>,
    onRetryMediaPreview: (String) -> Unit,
) {
    val content = remember(payload) {
        runCatching {
            val element = payload["content"]

            if (element == null || element == JsonNull) {
                null
            } else {
                referencePreviewJson.decodeFromJsonElement(
                    BlockContent.serializer(),
                    element,
                )
            }
        }.getOrNull()
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {

        if (content != null) {
            RichTextReadingView(
                content = content,
            )
        }

        if (media.isNotEmpty()) {
            EntityServiceReferenceImages(
                media = media,
                onRetry = onRetryMediaPreview,
            )
        }

        if (content == null && media.isEmpty()) {
            Text("暂无可展示内容")
        }
    }
}

/**
 * Note 图片阅读。
 *
 * 保留图片顺序、加载状态和失败重试。
 */
@Composable
private fun EntityServiceReferenceImages(
    media: List<EntityServiceReferenceMediaUi>,
    onRetry: (String) -> Unit,
) {
    val orderedMedia = media.sortedBy { it.order }

    if (orderedMedia.isEmpty()) return

    val first = orderedMedia.first()
    val remaining = orderedMedia.drop(1)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {

        ReferenceMediaTile(
            media = first,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            onRetry = onRetry,
        )

        if (remaining.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(
                    items = remaining,
                    key = { it.imageId },
                ) { image ->
                    ReferenceMediaTile(
                        media = image,
                        modifier = Modifier.size(104.dp),
                        onRetry = onRetry,
                    )
                }
            }
        }
    }
}

@Composable
private fun ReferenceMediaTile(
    media: EntityServiceReferenceMediaUi,
    modifier: Modifier = Modifier,
    onRetry: (String) -> Unit,
) {
    val shape = MaterialTheme.shapes.medium

    when {
        !media.displayUrl.isNullOrBlank() -> {
            PreviewableImage(
                imageUrl = media.displayUrl,
                contentDescription = "记录图片",
                modifier = modifier.clip(shape),
                contentScale = ContentScale.Crop,
            )
        }

        media.isLoading -> {
            Box(
                modifier = modifier
                    .clip(shape)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                )
            }
        }

        media.errorMessage != null -> {
            Column(
                modifier = modifier
                    .clip(shape)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                    )
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = media.errorMessage,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                )

                TextButton(
                    onClick = {
                        onRetry(media.imageId)
                    },
                ) {
                    Text("重试")
                }
            }
        }

        else -> {
            Box(
                modifier = modifier.background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(8.dp),
                ),
                contentAlignment = Alignment.Center,
            ) {
                Text("图片暂不可用")
            }
        }
    }
}

/**
 * Doc
 */
@Composable
private fun DocReferenceContent(
    payload: JsonObject,
) {
    val blocks = remember(payload) {
        runCatching {
            val element = payload["contents"] as? JsonArray

            if (element == null) {
                emptyList()
            } else {
                referencePreviewJson.decodeFromJsonElement(
                    ListSerializer(MobileRenderBlock.serializer()),
                    element,
                )
            }
        }.getOrDefault(emptyList())
    }

    if (blocks.isEmpty()) {
        Text("暂无可展示内容")
        return
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        blocks.forEach { block ->
            MobileRenderBlockView(
                block = block,
            )
        }
    }
}

/**
 * Slide
 */
@Composable
private fun SlideReferenceContent(
    payload: JsonObject,
) {
    val slide = remember(payload) {
        MobileSlideSummary(
            slideId = payload.stringValue("id").orEmpty(),
            title = payload.stringValue("title").orEmpty(),
            description = payload.stringValue("description"),
            canvasW = payload.doubleValue("canvasW") ?: 1920.0,
            canvasH = payload.doubleValue("canvasH") ?: 1080.0,
        )
    }

    val pages = remember(payload) {
        runCatching {
            val element = payload["cards"]
                    as? JsonArray

            if (element == null) {
                emptyList()
            } else {
                referencePreviewJson.decodeFromJsonElement(
                    ListSerializer(MobileSlidePage.serializer()),
                    element,
                )
            }
        }.getOrDefault(emptyList())
    }

    if (pages.isEmpty()) {
        Text("暂无可展示页面")
        return
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        pages.forEach { page ->
            SlidePageCanvas(
                slide = slide,
                page = page,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/**
 * PDF
 *
 * 第一阶段使用后端签名 URL 打开。
 */
@Composable
private fun PdfReferenceContent(
    payload: JsonObject,
) {
    val uriHandler = LocalUriHandler.current

    val title = payload.stringValue("title")
        ?: "PDF 文件"

    val url = payload.stringValue("url")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
        )

        Spacer(Modifier.height(16.dp))

        Button(
            enabled = !url.isNullOrBlank(),
            onClick = {
                url?.let {
                    uriHandler.openUri(it)
                }
            },
        ) {
            Text("打开 PDF")
        }
    }
}

/**
 * JSON Helpers
 */
private fun JsonObject.stringValue(
    key: String,
): String? {
    return (this[key] as? JsonPrimitive)
        ?.contentOrNull
}

private fun JsonObject.doubleValue(
    key: String,
): Double? {
    return (this[key] as? JsonPrimitive)
        ?.doubleOrNull
}
