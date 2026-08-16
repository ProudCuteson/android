package top.role.android.feature.slide.render

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.unit.sp
import top.role.android.core.model.slide.MobileSlideElement
import top.role.android.core.model.slide.MobileSlideTextLine
import top.role.android.core.model.slide.MobileSlideTextRun
import kotlin.math.max

@Composable
fun SlideTextElement(
    element: MobileSlideElement,
    scale: Double,
    modifier: Modifier = Modifier,
) {
    val content =
        element.content
            ?: return

    val density =
        LocalDensity.current

    /*
     * Slide 的 scale 是：
     *
     *   实际 Canvas 宽度 px / 原始 canvasW
     *
     * fontSize 来自 Slide 设计坐标。
     *
     * 我们希望：
     *
     *   fontSize × scale
     *
     * 最终直接对应屏幕像素大小。
     *
     * Compose 的 sp 还会再乘：
     *
     *   density × fontScale
     *
     * 所以需要提前除掉这两个系数。
     */
    val textScale =
        scale /
                density.density.toDouble() /
                density.fontScale.toDouble()

    Column(
        modifier =
            modifier.fillMaxWidth(),
    ) {
        content.lines.forEachIndexed { index, line ->
            SlideTextLine(
                line = line,
                lineIndex = index,
                textScale = textScale,
            )
        }
    }
}

@Composable
private fun SlideTextLine(
    line: MobileSlideTextLine,
    lineIndex: Int,
    textScale: Double,
) {
    val prefix =
        buildListPrefix(
            line = line,
            lineIndex = lineIndex,
        )

    val text =
        buildLineText(
            prefix = prefix,
            runs = line.runs,
            textScale = textScale,
        )

    val lineHeight =
        resolveLineHeight(
            line = line,
            textScale = textScale,
        )

    Text(
        text = text,

        modifier =
            Modifier.fillMaxWidth(),

        style =
            TextStyle(
                textAlign =
                    resolveTextAlign(
                        line.align
                    ),

                lineHeight =
                    lineHeight,
            ),
    )
}

private fun buildLineText(
    prefix: String?,
    runs: List<MobileSlideTextRun>,
    textScale: Double,
): AnnotatedString {

    return buildAnnotatedString {

        if (!prefix.isNullOrEmpty()) {
            append(prefix)
        }

        runs.forEach { run ->
            val mark =
                run.mark

            val decorations =
                buildList {
                    if (
                        mark?.underline == true
                    ) {
                        add(
                            TextDecoration.Underline
                        )
                    }

                    if (
                        mark?.strikethrough == true
                    ) {
                        add(
                            TextDecoration.LineThrough
                        )
                    }
                }

            val textDecoration =
                if (decorations.isEmpty()) {
                    null
                } else {
                    TextDecoration.combine(
                        decorations
                    )
                }

            val fontSize =
                resolveFontSize(
                    run = run,
                    textScale = textScale,
                )

            val foregroundColor =
                parseSlideColor(
                    mark?.color
                )
                    ?: Color.Unspecified

            val backgroundColor =
                parseSlideColor(
                    mark?.bgColor
                )
                    ?: Color.Unspecified

            withStyle(
                style =
                    SpanStyle(
                        color =
                            foregroundColor,

                        background =
                            backgroundColor,

                        fontSize =
                            fontSize,

                        fontWeight =
                            if (
                                mark?.bold == true
                            ) {
                                FontWeight.Bold
                            } else {
                                null
                            },

                        fontStyle =
                            if (
                                mark?.italic == true
                            ) {
                                FontStyle.Italic
                            } else {
                                null
                            },

                        fontFamily =
                            resolveFontFamily(
                                run = run,
                            ),

                        textDecoration =
                            textDecoration,
                    )
            ) {
                append(
                    run.text
                )
            }
        }
    }
}

private fun resolveFontSize(
    run: MobileSlideTextRun,
    textScale: Double,
): TextUnit {

    val fontSize =
        run.mark
            ?.fontSize
            ?.takeIf {
                it > 0
            }
            ?: return TextUnit.Unspecified

    return (
            fontSize *
                    textScale
            )
        .toFloat()
        .sp
}

private fun resolveFontFamily(
    run: MobileSlideTextRun,
): FontFamily? {

    val mark =
        run.mark
            ?: return null

    /*
     * code 优先使用等宽字体。
     *
     * fontFamily 字符串暂时不直接映射，
     * 因为 Web / iOS / Android 的字体名称
     * 并不天然一一对应。
     *
     * 后面可以单独建立 SlideFontResolver。
     */
    if (mark.code == true) {
        return FontFamily.Monospace
    }

    return null
}

private fun buildListPrefix(
    line: MobileSlideTextLine,
    lineIndex: Int,
): String? {

    val list =
        line.list
            ?: return null

    val indent =
        max(
            0,
            list.indent
                ?.toInt()
                ?: 0,
        )

    val indentation =
        "  ".repeat(
            indent
        )

    return when (
        list.type
    ) {
        "bullet" -> {
            "${indentation}• "
        }

        "numbered" -> {
            val start =
                list.start
                    ?.toInt()
                    ?: 1

            /*
             * 第一版先按当前 line index 计算。
             *
             * 后续如果需要严格支持多个
             * numbered list sequence，
             * 再做连续列表分组计算。
             */
            val number =
                start +
                        lineIndex

            "${indentation}${number}. "
        }

        "checkbox" -> {
            if (
                list.checked == true
            ) {
                "${indentation}☑ "
            } else {
                "${indentation}☐ "
            }
        }

        else -> {
            null
        }
    }
}

private fun resolveTextAlign(
    value: String?,
): TextAlign {

    return when (
        value
            ?.trim()
            ?.lowercase()
    ) {
        "center" ->
            TextAlign.Center

        "right" ->
            TextAlign.Right

        "justify" ->
            TextAlign.Justify

        else ->
            TextAlign.Left
    }
}

private fun resolveLineHeight(
    line: MobileSlideTextLine,
    textScale: Double,
): TextUnit {

    val explicit =
        line.lineHeight
            ?.takeIf {
                it > 0
            }

    if (explicit != null) {
        return (
                explicit *
                        textScale
                )
            .toFloat()
            .sp
    }

    /*
     * 后端没有显式 lineHeight 时，
     * 用当前行最大字体作为基础。
     *
     * 这里 1.2 只是合理 fallback，
     * 后面结合真实 Slide 再判断
     * 是否需要进一步贴近桌面/iOS。
     */
    val largestFont =
        line.runs
            .mapNotNull {
                it.mark
                    ?.fontSize
            }
            .filter {
                it > 0
            }
            .maxOrNull()

    if (largestFont != null) {
        return (
                largestFont *
                        textScale *
                        1.2
                )
            .toFloat()
            .sp
    }

    return TextUnit.Unspecified
}