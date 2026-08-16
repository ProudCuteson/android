package top.role.android.feature.slide.render

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.role.android.core.model.slide.MobileSlideTableCell
import top.role.android.core.model.slide.MobileSlideTableHeader
import top.role.android.core.model.slide.MobileSlideTextContent
import top.role.android.core.model.slide.MobileSlideTextLine
import top.role.android.core.model.slide.MobileSlideTextRun
import kotlin.math.max

@Composable
internal fun SlideTableCell(
    cell: MobileSlideTableCell,
    header: MobileSlideTableHeader?,
    scale: Double,
    modifier: Modifier = Modifier,
) {
    val anchorRow =
        cell.anchorRow
            .toInt()

    val anchorCol =
        cell.anchorCol
            .toInt()

    val isHeaderCell =
        (
                header?.top == true &&
                        anchorRow == 0
                ) ||
                (
                        header?.left == true &&
                                anchorCol == 0
                        )

    val alignment =
        resolveCellAlignment(
            cell.verticalAlign
        )

    val density =
        LocalDensity.current

    val horizontalPadding =
        with(density) {
            max(
                3f,
                (8.0 * scale)
                    .toFloat(),
            ).toDp()
        }

    val verticalPadding =
        with(density) {
            max(
                2f,
                (5.0 * scale)
                    .toFloat(),
            ).toDp()
        }

    val borderWidth =
        with(density) {
            max(
                0.5f,
                scale.toFloat(),
            ).toDp()
        }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(
                    if (isHeaderCell) {
                        MaterialTheme.colorScheme
                            .surfaceContainerHighest
                    } else {
                        MaterialTheme.colorScheme
                            .background
                            .copy(
                                alpha = 0.98f
                            )
                    }
                )
                .border(
                    width =
                        borderWidth,
                    color =
                        MaterialTheme.colorScheme
                            .outlineVariant
                            .copy(
                                alpha = 0.55f
                            ),
                )
                .clipToBounds(),
        contentAlignment =
            alignment,
    ) {
        val content =
            cell.content
                ?.content

        if (content != null) {
            SlideTableCellText(
                content =
                    content,
                fallbackAlign =
                    cell.align,
                scale =
                    scale,
                modifier =
                    Modifier.padding(
                        horizontal =
                            horizontalPadding,
                        vertical =
                            verticalPadding,
                    ),
            )
        }
    }
}

private fun resolveCellAlignment(
    verticalAlign: String?,
): Alignment {
    return when (
        verticalAlign
            ?.trim()
            ?.lowercase()
    ) {
        "middle" ->
            Alignment.CenterStart

        "bottom" ->
            Alignment.BottomStart

        else ->
            Alignment.TopStart
    }
}

@Composable
private fun SlideTableCellText(
    content: MobileSlideTextContent,
    fallbackAlign: String?,
    scale: Double,
    modifier: Modifier = Modifier,
) {
    val density =
        LocalDensity.current

    /*
     * Slide 中的 fontSize 本质是设计坐标。
     *
     * 目标：
     * designFontSize * scale = 最终 px
     *
     * Compose 的 sp 还会再次经过：
     * density * fontScale
     *
     * 所以这里需要抵消 density 和系统 fontScale。
     */
    val textScale =
        scale /
                density.density.toDouble() /
                density.fontScale.toDouble()

    Column(
        modifier =
            modifier.fillMaxWidth(),
    ) {
        content.lines.forEach { line ->
            SlideTableTextLine(
                line =
                    line,
                fallbackAlign =
                    fallbackAlign,
                textScale =
                    textScale,
            )
        }
    }
}

@Composable
private fun SlideTableTextLine(
    line: MobileSlideTextLine,
    fallbackAlign: String?,
    textScale: Double,
) {
    val effectiveAlign =
        line.align
            ?: fallbackAlign

    val textAlign =
        resolveTableTextAlign(
            effectiveAlign
        )

    val fallbackFontSize =
        resolveTableFallbackFontSize(
            line.variant
        )

    val text =
        buildTableLineText(
            line =
                line,
            fallbackFontSize =
                fallbackFontSize,
            textScale =
                textScale,
        )

    val lineHeight =
        resolveTableLineHeight(
            line =
                line,
            fallbackFontSize =
                fallbackFontSize,
            textScale =
                textScale,
        )

    Text(
        text =
            text,
        modifier =
            Modifier.fillMaxWidth(),
        style =
            TextStyle(
                textAlign =
                    textAlign,
                lineHeight =
                    lineHeight,
            ),
        maxLines = 4,
    )
}

private fun buildTableLineText(
    line: MobileSlideTextLine,
    fallbackFontSize: Double,
    textScale: Double,
): AnnotatedString {
    return buildAnnotatedString {

        line.runs.forEach { run ->
            val mark =
                run.mark

            val fontSize =
                (
                        mark?.fontSize
                            ?: fallbackFontSize
                        ) *
                        textScale

            val foreground =
                parseSlideColor(
                    mark?.color
                )
                    ?: Color.Unspecified

            val background =
                parseSlideColor(
                    mark?.bgColor
                )
                    ?: Color.Unspecified

            val decorations =
                buildTableTextDecoration(
                    underline =
                        mark?.underline == true,
                    strikethrough =
                        mark?.strikethrough == true,
                )

            val fontFamily =
                if (mark?.code == true) {
                    FontFamily.Monospace
                } else {
                    FontFamily.Default
                }

            withStyle(
                style =
                    SpanStyle(
                        fontSize =
                            fontSize.sp,

                        fontWeight =
                            if (mark?.bold == true) {
                                FontWeight.SemiBold
                            } else {
                                FontWeight.Normal
                            },

                        fontStyle =
                            if (mark?.italic == true) {
                                FontStyle.Italic
                            } else {
                                FontStyle.Normal
                            },

                        fontFamily =
                            fontFamily,

                        color =
                            foreground,

                        background =
                            background,

                        textDecoration =
                            decorations,
                    )
            ) {
                append(
                    run.text
                )
            }
        }
    }
}

private fun buildTableTextDecoration(
    underline: Boolean,
    strikethrough: Boolean,
): TextDecoration? {
    return when {
        underline &&
                strikethrough -> {
            TextDecoration.combine(
                listOf(
                    TextDecoration.Underline,
                    TextDecoration.LineThrough,
                )
            )
        }

        underline -> {
            TextDecoration.Underline
        }

        strikethrough -> {
            TextDecoration.LineThrough
        }

        else -> {
            null
        }
    }
}

private fun resolveTableTextAlign(
    align: String?,
): TextAlign {
    return when (
        align
            ?.trim()
            ?.lowercase()
    ) {
        "center" ->
            TextAlign.Center

        "right" ->
            TextAlign.End

        else ->
            TextAlign.Start
    }
}

private fun resolveTableFallbackFontSize(
    variant: String?,
): Double {
    return when (
        variant
            ?.trim()
            ?.lowercase()
    ) {
        "title" ->
            28.0

        "h1" ->
            24.0

        "h2" ->
            21.0

        "h3" ->
            18.0

        "small" ->
            12.0

        "caption" ->
            11.0

        else ->
            14.0
    }
}

private fun resolveTableLineHeight(
    line: MobileSlideTextLine,
    fallbackFontSize: Double,
    textScale: Double,
): TextUnit {
    /*
     * 如果后端明确给了 lineHeight，
     * 就优先按设计尺寸缩放。
     */
    val explicitLineHeight =
        line.lineHeight

    if (
        explicitLineHeight != null &&
        explicitLineHeight > 0.0
    ) {
        return (
                explicitLineHeight *
                        textScale
                ).sp
    }

    /*
     * 否则找这一行最大的字号，
     * 用 1.2 倍作为 Compose lineHeight。
     */
    val largestFontSize =
        line.runs
            .mapNotNull {
                it.mark?.fontSize
            }
            .maxOrNull()
            ?: fallbackFontSize

    return (
            largestFontSize *
                    textScale *
                    1.2
            ).sp
}