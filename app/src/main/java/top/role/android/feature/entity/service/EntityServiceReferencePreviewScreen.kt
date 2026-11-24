package top.role.android.feature.entity.service

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull

import top.role.android.feature.slide.render.SlidePageCanvas

import top.role.android.core.model.chart.MobileChartRuntime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.serialization.builtins.ListSerializer

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
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
import top.role.android.feature.slide.render.SlidePageCanvas
import top.role.android.core.model.content.MobileRenderDoc
import top.role.android.feature.reading.components.MobileDocRenderer

import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding

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
    onRetryPdfDelivery: (String) -> Unit,
    onOpenPdf: (
        title: String,
        url: String,
        assetId: String,
    ) -> Unit,
    onBack: () -> Unit,
) {
    val payload = state.content

    val title = payload?.stringValue("title")
        ?: "关联内容"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
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
                        isPdfLoading = state.isPdfLoading,
                        pdfDelivery = state.pdfDelivery,
                        pdfErrorMessage = state.pdfErrorMessage,
                        onRetryPdfDelivery = onRetryPdfDelivery,
                        onOpenPdf = onOpenPdf,
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
    isPdfLoading: Boolean,
    pdfDelivery: AssetDeliveryResult?,
    pdfErrorMessage: String?,
    onRetryPdfDelivery: (String) -> Unit,
    onOpenPdf: (String, String, String) -> Unit,
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
            )

            EntityServiceReferenceImages(
                media = media,
                onRetry = onRetryMediaPreview,
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
                isLoading = isPdfLoading,
                delivery = pdfDelivery,
                errorMessage = pdfErrorMessage,
                onRetry = onRetryPdfDelivery,
                onOpenPdf = onOpenPdf,
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
 *
 * - 正文：BlockContent → RichTextReadingView
 * - 图片：media → 后续接入 NoteImageReading
 */

@Composable
private fun NoteReferenceContent(
    payload: JsonObject,
) {
    // 直接取得 BlockContent
    val body = payload["content"] as? JsonObject

    val blockContent = body?.let { element ->
        runCatching {
            val obj = element as? JsonObject
                ?: error("BlockContent is not an object")

            val normalized = JsonObject(
                obj + (
                        "v" to JsonPrimitive(1)
                        )
            )

            referencePreviewJson.decodeFromJsonElement(
                BlockContent.serializer(),
                normalized,
            )
        }.onFailure { error ->

        }.getOrNull()
    }

    if (blockContent != null) {
        RichTextReadingView(
            content = blockContent,
            modifier = Modifier.fillMaxWidth(),
        )
    } else {
        Text("暂无可展示内容")
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
 *
 * 后端返回 MobileRenderDoc V2 结构：
 * contents
 *   ├── version
 *   ├── blocks
 *   ├── repairedBlockIds
 *   └── repairedLeafIds
 *
 * 直接复用正常阅读模块的 MobileDocRenderer。
 */
@Composable
private fun DocReferenceContent(
    payload: JsonObject,
) {
    val renderDoc = remember(payload) {
        runCatching {
            val element = payload["renderDoc"]
                ?: error("Doc renderDoc 不存在")

            referencePreviewJson.decodeFromJsonElement(
                MobileRenderDoc.serializer(),
                element,
            )
        }.onFailure { error ->

        }.getOrNull()
    }

    val charts = remember(payload) {
        runCatching {
            val element = payload["charts"]
                ?: return@runCatching emptyMap()

            referencePreviewJson.decodeFromJsonElement(
                MapSerializer(
                    String.serializer(),
                    MobileChartRuntime.serializer(),
                ),
                element,
            )
        }.onFailure { error ->

        }.getOrDefault(emptyMap())
    }

    if (renderDoc == null) {
        Text("文档内容解析失败")
        return
    }

    if (renderDoc.blocks.isEmpty()) {
        Text("暂无可展示内容")
        return
    }

    MobileDocRenderer(
        renderDoc = renderDoc,
        modifier = Modifier.fillMaxWidth(),
        charts = charts,
    )
}

/**
 * Slide
 */

@Composable
private fun SlideReferenceContent(
    payload: JsonObject,
) {
    val slideData = remember(payload) {
        runCatching {
            val slide = MobileSlideSummary(
                slideId = payload["id"]!!
                    .jsonPrimitive.content,
                title = payload["title"]!!
                    .jsonPrimitive.content,
                description = payload["description"]
                    ?.jsonPrimitive
                    ?.contentOrNull,
                canvasW = payload["canvasW"]!!
                    .jsonPrimitive.double,
                canvasH = payload["canvasH"]!!
                    .jsonPrimitive.double,
            )

            val pages = referencePreviewJson
                .decodeFromJsonElement(
                    ListSerializer(
                        MobileSlidePage.serializer()
                    ),
                    payload["cards"]
                        ?: error("Slide cards 不存在"),
                )

            slide to pages
        }.onFailure { error ->

        }.getOrNull()
    }

    if (slideData == null) {
        Text("演示文稿解析失败")
        return
    }

    val (slide, pages) = slideData

    if (pages.isEmpty()) {
        Text("暂无可展示页面")
        return
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
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
 * 通过后端 Action 获取临时授权 URL，
 * 使用原始 assetId 进入原生 PDF 阅读器。
 */
@Composable
private fun PdfReferenceContent(
    payload: JsonObject,
    isLoading: Boolean,
    delivery: AssetDeliveryResult?,
    errorMessage: String?,
    onRetry: (String) -> Unit,
    onOpenPdf: (String, String, String) -> Unit,
) {
    val assetId = payload.stringValue("id")
        .orEmpty()

    val title = payload.stringValue("title")
        ?: "PDF 文件"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
        )

        Text(
            text = payload.stringValue("mime")
                ?: "PDF 文档",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                )

                Text(
                    text = "正在获取 PDF 访问权限",
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            errorMessage != null -> {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )

                OutlinedButton(
                    enabled = assetId.isNotBlank(),
                    onClick = {
                        onRetry(assetId)
                    },
                ) {
                    Text("重新获取")
                }
            }

            delivery != null -> {
                Button(
                    enabled =
                        delivery.assetId == assetId &&
                                delivery.url.isNotBlank(),
                    onClick = {
                        onOpenPdf(
                            title,
                            delivery.url,
                            delivery.assetId,
                        )
                    },
                ) {
                    Text("打开 PDF")
                }
            }

            else -> {
                Text(
                    text = "PDF 访问地址暂不可用",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                OutlinedButton(
                    enabled = assetId.isNotBlank(),
                    onClick = {
                        onRetry(assetId)
                    },
                ) {
                    Text("重试")
                }
            }
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
