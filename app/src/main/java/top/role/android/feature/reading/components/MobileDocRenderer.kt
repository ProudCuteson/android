package top.role.android.feature.reading.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import top.role.android.core.model.content.MobileListBlock
import top.role.android.core.model.content.MobileQuoteBlock
import top.role.android.core.model.content.MobileRenderBlock
import top.role.android.core.model.content.MobileTextBlock
import androidx.compose.foundation.layout.height
import top.role.android.core.model.content.MobileImageBlock
import top.role.android.core.model.content.MobileCodeBlock
import top.role.android.core.model.content.MobileTableBlock
import top.role.android.core.model.content.MobileChartBlock
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight

@Composable
fun MobileRenderBlockView(
    block: MobileRenderBlock,
    modifier: Modifier = Modifier,
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
                )
            }

            else -> {
                // image / code / table / chart 后面补
            }
        }
    }
}

@Composable
private fun MobileTextBlockView(
    block: MobileTextBlock,
) {
    val style =
        resolveTextBlockStyle(
            block = block,
        )

    val topPadding =
        if (block.role == "heading") {
            when (
                block.level
                    ?.toInt()
                    ?: 1
            ) {
                1 ->
                    ReadingDimens
                        .Heading1TopSpacing

                2 ->
                    ReadingDimens
                        .Heading2TopSpacing

                3 ->
                    ReadingDimens
                        .Heading3TopSpacing

                else ->
                    ReadingDimens
                        .HeadingSmallTopSpacing
            }
        } else {
            0.dp
        }

    MobileInlineText(
        inlines = block.inlines,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = topPadding,
            ),
        baseStyle = style,
        textAlign =
            resolveMobileTextAlign(
                block.align,
            ),
    )
}

@Composable
private fun resolveTextBlockStyle(
    block: MobileTextBlock,
): TextStyle {
    return when (block.role) {
        "heading" -> {
            readingHeadingStyle(
                level =
                    block.level
                        ?.toInt()
                        ?: 1,
            )
        }

        "caption",
        "date-time",
            -> {
            readingCaptionStyle()
        }

        else -> {
            readingParagraphStyle()
        }
    }
}

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
                shape =
                    RoundedCornerShape(12.dp),
            )
            .padding(
                horizontal =
                    ReadingDimens
                        .QuotePaddingHorizontal,
                vertical =
                    ReadingDimens
                        .QuotePaddingVertical,
            ),
        verticalAlignment =
            Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(
                    color =
                        MaterialTheme.colorScheme
                            .primary
                            .copy(
                                alpha = 0.45f
                            ),
                    shape =
                        RoundedCornerShape(
                            999.dp
                        ),
                ),
        )

        Spacer(
            modifier =
                Modifier.width(12.dp),
        )

        MobileInlineText(
            inlines = block.inlines,
            modifier =
                Modifier.weight(1f),
            baseStyle =
                readingQuoteStyle(),
            textAlign =
                resolveMobileTextAlign(
                    block.align,
                ),
        )
    }
}

@Composable
private fun MobileListBlockView(
    block: MobileListBlock,
) {
    Column(
        modifier =
            Modifier.fillMaxWidth(),
        verticalArrangement =
            Arrangement.spacedBy(
                ReadingDimens
                    .ListItemSpacing
            ),
    ) {
        block.items
            .forEachIndexed {
                    index,
                    item,
                ->

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    verticalAlignment =
                        Alignment.Top,
                ) {
                    Text(
                        text =
                            when {
                                item.checked == true ->
                                    "☑"

                                item.checked == false ->
                                    "☐"

                                block.ordered == true ->
                                    "${index + 1}."

                                else ->
                                    "•"
                            },
                        modifier =
                            Modifier.width(
                                30.dp
                            ),
                        style =
                            readingListStyle(),
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant,
                    )

                    MobileInlineText(
                        inlines =
                            item.inlines,
                        modifier =
                            Modifier.weight(
                                1f
                            ),
                        baseStyle =
                            readingListStyle(),
                        textAlign =
                            resolveMobileTextAlign(
                                block.align,
                            ),
                    )
                }
            }
    }
}