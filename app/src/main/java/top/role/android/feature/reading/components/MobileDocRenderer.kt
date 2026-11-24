package top.role.android.feature.reading.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

import top.role.android.core.model.chart.MobileChartRuntime

import top.role.android.core.model.content.MobileRenderDoc
import top.role.android.core.model.content.MobileRenderBlock
import top.role.android.core.model.content.MobileTextBlock
import top.role.android.core.model.content.MobileQuoteBlock
import top.role.android.core.model.content.MobileListBlock
import top.role.android.core.model.content.MobileImageBlock
import top.role.android.core.model.content.MobileImageFigureBlock
import top.role.android.core.model.content.MobileCodeBlock
import top.role.android.core.model.content.MobileTableBlock
import top.role.android.core.model.content.MobileChartBlock
import top.role.android.core.model.content.MobileDisclosureBlock
import top.role.android.core.model.content.MobileDividerBlock
import top.role.android.core.model.content.MobileSoftBreakBlock

/**
 * 完整 Doc 正文渲染入口。
 */
@Composable
fun MobileDocRenderer(
    renderDoc: MobileRenderDoc,
    modifier: Modifier = Modifier,
    charts: Map<String, MobileChartRuntime> = emptyMap(),
) {
    Column(
        modifier = modifier.fillMaxWidth(),

        verticalArrangement =
            Arrangement.spacedBy(
                ReadingDimens.BlockSpacing
            ),
    ) {

        renderDoc.blocks.forEach { block ->

            key(block.id) {

                MobileRenderBlockView(
                    block = block,
                    charts = charts,
                )
            }
        }
    }
}

/**
 * 单个 Block 的统一渲染入口。
 *
 * 同时供 Doc 正文及 Disclosure 等嵌套内容使用。
 */
@Composable
fun MobileRenderBlockView(
    block: MobileRenderBlock,
    modifier: Modifier = Modifier,
    charts: Map<String, MobileChartRuntime> = emptyMap(),
) {
    Box(
        modifier = modifier.fillMaxWidth(),
    ) {

        when (block) {

            is MobileTextBlock -> {
                MobileTextBlockView(
                    block = block,
                )
            }

            is MobileQuoteBlock -> {
                MobileQuoteBlockView(
                    block = block,
                )
            }

            is MobileListBlock -> {
                MobileListBlockView(
                    block = block,
                )
            }

            is MobileImageBlock -> {
                MobileImageBlockView(
                    block = block,
                )
            }

            is MobileImageFigureBlock -> {
                MobileImageFigureBlockView(
                    block = block,
                )
            }

            is MobileCodeBlock -> {
                MobileCodeBlockView(
                    block = block,
                )
            }

            is MobileTableBlock -> {
                MobileTableBlockView(
                    block = block,
                )
            }

            is MobileChartBlock -> {
                MobileChartBlockView(
                    block = block,
                    runtime = charts[block.chartId],
                )
            }

            is MobileDisclosureBlock -> {
                MobileDisclosureBlockView(
                    block = block,
                    charts = charts,
                )
            }

            is MobileDividerBlock -> {
                MobileDividerBlockView(
                    block = block,
                )
            }

            is MobileSoftBreakBlock -> {
                MobileSoftBreakBlockView(
                    block = block,
                )
            }
        }
    }
}

/**
 * Paragraph、Heading、Caption。
 */
@Composable
private fun MobileTextBlockView(
    block: MobileTextBlock,
) {
    val style =
        resolveTextBlockStyle(block)

    val topPadding =
        if (block.role == "heading") {

            when (block.level?.toInt() ?: 1) {

                1 ->
                    ReadingDimens.Heading1TopSpacing

                2 ->
                    ReadingDimens.Heading2TopSpacing

                3 ->
                    ReadingDimens.Heading3TopSpacing

                else ->
                    ReadingDimens.HeadingSmallTopSpacing
            }

        } else {
            ReadingDimens.ParagraphTopSpacing
        }

    val modifier =
        Modifier
            .fillMaxWidth()
            .padding(top = topPadding)

    val textAlign =
        resolveMobileTextAlign(
            block.align
        )

    /*
     * 保留作者主动创建的空白段落。
     */
    if (
        block.empty == true ||
        block.inlines.isEmpty() ||
        block.inlines.all {
            it.text.isEmpty()
        }
    ) {

        Text(
            text = " ",
            modifier = modifier,
            style = style,
            textAlign = textAlign,
        )

        return
    }

    MobileInlineText(
        inlines = block.inlines,
        modifier = modifier,
        baseStyle = style,
        textAlign = textAlign,
    )
}

@Composable
private fun resolveTextBlockStyle(
    block: MobileTextBlock,
): TextStyle {

    return when (block.role) {

        "heading" ->
            readingHeadingStyle(
                level = block.level?.toInt() ?: 1,
            )

        "caption" ->
            readingCaptionStyle()

        else ->
            readingParagraphStyle()
    }
}

/**
 * Quote。
 *
 * 保留原有引用背景、侧边色条及字号。
 */
@Composable
private fun MobileQuoteBlockView(
    block: MobileQuoteBlock,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(
                color =
                    MaterialTheme.colorScheme
                        .surfaceContainerLow,

                shape = RoundedCornerShape(12.dp),
            )
            .padding(
                horizontal =
                    ReadingDimens
                        .QuotePaddingHorizontal,

                vertical =
                    ReadingDimens
                        .QuotePaddingVertical,
            ),

        verticalAlignment = Alignment.Top,
    ) {

        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(
                    color =
                        MaterialTheme.colorScheme
                            .primary
                            .copy(alpha = 0.45f),

                    shape = RoundedCornerShape(999.dp),
                ),
        )

        Spacer(
            modifier = Modifier.width(12.dp),
        )

        MobileInlineText(
            inlines = block.inlines,

            modifier = Modifier.weight(1f),

            baseStyle = readingQuoteStyle(),

            textAlign =
                resolveMobileTextAlign(
                    block.align
                ),
        )
    }
}

/**
 * List。
 *
 * 支持 bulleted、numbered、todo 及缩进。
 */
@Composable
private fun MobileListBlockView(
    block: MobileListBlock,
) {
    val style = readingListStyle()

    val counters = mutableMapOf<Int, Int>()

    Column(
        modifier = Modifier.fillMaxWidth(),

        verticalArrangement =
            Arrangement.spacedBy(
                ReadingDimens.ListItemSpacing
            ),
    ) {

        block.items.forEach { item ->

            val indent = (item.indent ?: 0).toInt().coerceAtLeast(0)

            val marker = when (block.listKind) {

                "todo" -> {
                    if (item.checked == true) {
                        "☑"
                    } else {
                        "☐"
                    }
                }

                "numbered" -> {

                    counters.keys
                        .filter { it > indent }
                        .forEach {
                            counters.remove(it)
                        }

                    val number = (counters[indent] ?: 0) + 1

                    counters[indent] = number

                    "$number."
                }

                else -> "•"
            }

            /*
             * 限制实际视觉缩进，
             * 避免手机上深层列表没有正文空间。
             */
            val visualIndent =
                indent.coerceAtMost(4)

            key(item.id) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = (visualIndent * 16).dp
                        ),

                    verticalAlignment = Alignment.Top,
                ) {

                    Text(
                        text = marker,

                        modifier = Modifier
                            .widthIn(min = 30.dp)
                            .padding(end = 4.dp),

                        style = style,

                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant,
                    )

                    if (
                        item.inlines.isEmpty() ||
                        item.inlines.all {
                            it.text.isEmpty()
                        }
                    ) {

                        Text(
                            text = " ",
                            modifier = Modifier.weight(1f),
                            style = style,
                        )

                    } else {

                        MobileInlineText(
                            inlines = item.inlines,

                            modifier = Modifier.weight(1f),

                            baseStyle = style,

                            textAlign =
                                resolveMobileTextAlign(
                                    block.align
                                ),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Divider。
 */
@Composable
private fun MobileDividerBlockView(
    block: MobileDividerBlock,
) {
    val spacingBefore =
        block.spacingBefore
            ?.takeIf {
                it.isFinite() && it >= 0
            }
            ?.coerceAtMost(48.0)
            ?.dp
            ?: 0.dp

    val spacingAfter =
        block.spacingAfter
            ?.takeIf {
                it.isFinite() && it >= 0
            }
            ?.coerceAtMost(48.0)
            ?.dp
            ?: 0.dp

    HorizontalDivider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = spacingBefore,
                bottom = spacingAfter,
            ),

        thickness = 1.dp,

        color =
            MaterialTheme.colorScheme
                .outlineVariant,
    )
}

/**
 * SoftBreak。
 *
 * 当前协议以独立 Block 表达，
 * 因此在阅读端表现为垂直间距。
 */
@Composable
private fun MobileSoftBreakBlockView(
    block: MobileSoftBreakBlock,
) {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(
                if (block.isInline == true) {
                    4.dp
                } else {
                    12.dp
                }
            ),
    )
}