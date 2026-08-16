package top.role.android.feature.reading.components

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import top.role.android.core.model.content.MobileInline
import androidx.compose.ui.graphics.luminance
import androidx.core.graphics.toColorInt
import androidx.compose.ui.text.style.BaselineShift

@Composable
fun MobileInlineText(
    inlines: List<MobileInline>,
    modifier: Modifier = Modifier,
    baseStyle: TextStyle =
        MaterialTheme.typography.bodyLarge,
    textAlign: TextAlign =
        TextAlign.Left,
) {
    val contentColor =
        if (
            baseStyle.color !=
            Color.Unspecified
        ) {
            baseStyle.color
        } else {
            LocalContentColor.current
        }

    val isDark =
        MaterialTheme.colorScheme
            .background
            .luminance() < 0.5f

    val annotatedText =
        buildMobileAnnotatedString(
            inlines = inlines,
            defaultColor = contentColor,
            isDark = isDark,
        )

    Text(
        text = annotatedText,
        modifier = modifier,
        style =
            baseStyle.copy(
                textAlign = textAlign,
            ),
    )
}

private fun buildMobileAnnotatedString(
    inlines: List<MobileInline>,
    defaultColor: Color,
    isDark: Boolean,
): AnnotatedString {
    return buildAnnotatedString {
        normalizeLeadingPunctuation(
            inlines
        ).forEach { inline ->

            if (inline.text.isEmpty()) {
                return@forEach
            }

            val start = length

            append(inline.text)

            val end = length

            if (end <= start) {
                return@forEach
            }

            addStyle(
                style =
                    buildInlineSpanStyle(
                        inline = inline,
                        defaultColor =
                            defaultColor,
                        isDark = isDark,
                    ),
                start = start,
                end = end,
            )

            inline.link
                ?.href
                ?.takeIf { it.isNotBlank() }
                ?.let { href ->
                    addStringAnnotation(
                        tag = "URL",
                        annotation = href,
                        start = start,
                        end = end,
                    )
                }

            inline.mention
                ?.let { mention ->
                    addStringAnnotation(
                        tag = "MENTION",
                        annotation =
                            "${mention.targetType}:${mention.targetId}",
                        start = start,
                        end = end,
                    )
                }
        }
    }
}

private fun buildInlineSpanStyle(
    inline: MobileInline,
    defaultColor: Color,
    isDark: Boolean,
): SpanStyle {
    val isCode =
        inline.code == true

    return SpanStyle(
        fontWeight =
            if (inline.bold == true) {
                FontWeight.Bold
            } else {
                FontWeight.Normal
            },

        fontStyle =
            if (inline.italic == true) {
                FontStyle.Italic
            } else {
                FontStyle.Normal
            },

        fontFamily =
            if (isCode) {
                FontFamily.Monospace
            } else {
                null
            },

        textDecoration =
            resolveTextDecoration(
                underline =
                    inline.underline == true,
                strikethrough =
                    inline.strikethrough == true,
            ),

        color =
            resolveInlineColor(
                raw = inline.color,
                isDark = isDark,
            ) ?: defaultColor,

        background =
            resolveInlineBackground(
                inline = inline,
                isDark = isDark,
            ),

        fontSize =
            resolveInlineFontSize(
                size = inline.fontSize,
                subscript =
                    inline.subscript == true,
                superscript =
                    inline.superscript == true,
            ),

        baselineShift =
            resolveBaselineShift(
                subscript =
                    inline.subscript == true,
                superscript =
                    inline.superscript == true,
            ),
    )
}

private fun resolveInlineBackground(
    inline: MobileInline,
    isDark: Boolean,
): Color {
    /*
     * 作者显式设置的 bgColor 优先。
     */
    resolveInlineColor(
        raw = inline.bgColor,
        isDark = isDark,
    )?.let {
        return it
    }

    /*
     * inline code 没有显式背景时，
     * 给一个随主题变化的弱背景。
     *
     * 对应小程序 .rich-inline.is-code
     * 的弱背景效果。
     */
    if (inline.code == true) {
        return if (isDark) {
            Color.White.copy(
                alpha = 0.08f
            )
        } else {
            Color.Black.copy(
                alpha = 0.06f
            )
        }
    }

    return Color.Unspecified
}

private fun resolveTextDecoration(
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

private fun resolveBaselineShift(
    subscript: Boolean,
    superscript: Boolean,
): BaselineShift? {
    return when {
        superscript -> {
            BaselineShift.Superscript
        }

        subscript -> {
            BaselineShift.Subscript
        }

        else -> {
            null
        }
    }
}

private fun resolveInlineFontSize(
    size: Double?,
    subscript: Boolean,
    superscript: Boolean,
): TextUnit {
    val hasExplicitSize =
        size != null &&
                size > 0

    if (hasExplicitSize) {
        val baseSize =
            size!!
                .toFloat()

        val finalSize =
            if (
                subscript ||
                superscript
            ) {
                baseSize * 0.8f
            } else {
                baseSize
            }

        return finalSize.sp
    }

    /*
     * 没有自定义字号时，
     * 上下标使用一个相对较小的字号。
     *
     * 后面如果要完全基于父级 TextStyle
     * 做比例缩放，可以再升级 renderer。
     */
    if (
        subscript ||
        superscript
    ) {
        return 12.sp
    }

    return TextUnit.Unspecified
}

private val darkRichColorMap =
    mapOf(
        // text
        "--toolbar-text-color-1" to
                Color(0xFFF3F4F6),

        "--toolbar-text-color-2" to
                Color(0xFF9CA3AF),

        "--toolbar-text-color-3" to
                Color(0xFFF87171),

        "--toolbar-text-color-4" to
                Color(0xFFFB923C),

        "--toolbar-text-color-5" to
                Color(0xFFFACC15),

        "--toolbar-text-color-6" to
                Color(0xFF4ADE80),

        "--toolbar-text-color-7" to
                Color(0xFF60A5FA),

        "--toolbar-text-color-8" to
                Color(0xFFA78BFA),

        // background
        "--toolbar-bg-color-1" to
                Color(0xFF374151),

        "--toolbar-bg-color-2" to
                Color(0xFF7F1D1D),

        "--toolbar-bg-color-3" to
                Color(0xFF7C2D12),

        "--toolbar-bg-color-4" to
                Color(0xFF713F12),

        "--toolbar-bg-color-5" to
                Color(0xFF14532D),

        "--toolbar-bg-color-6" to
                Color(0xFF1E3A8A),

        "--toolbar-bg-color-7" to
                Color(0xFF4C1D95),

        "--toolbar-bg-color-8" to
                Color(0xFF831843),
    )

private val lightRichColorMap =
    mapOf(
        // text
        "--toolbar-text-color-1" to
                Color(0xFF111827),

        "--toolbar-text-color-2" to
                Color(0xFF6B7280),

        "--toolbar-text-color-3" to
                Color(0xFFDC2626),

        "--toolbar-text-color-4" to
                Color(0xFFEA580C),

        "--toolbar-text-color-5" to
                Color(0xFFCA8A04),

        "--toolbar-text-color-6" to
                Color(0xFF16A34A),

        "--toolbar-text-color-7" to
                Color(0xFF2563EB),

        "--toolbar-text-color-8" to
                Color(0xFF7C3AED),

        // background
        "--toolbar-bg-color-1" to
                Color(0xFFF3F4F6),

        "--toolbar-bg-color-2" to
                Color(0xFFFEE2E2),

        "--toolbar-bg-color-3" to
                Color(0xFFFFEDD5),

        "--toolbar-bg-color-4" to
                Color(0xFFFEF3C7),

        "--toolbar-bg-color-5" to
                Color(0xFFDCFCE7),

        "--toolbar-bg-color-6" to
                Color(0xFFDBEAFE),

        "--toolbar-bg-color-7" to
                Color(0xFFEDE9FE),

        "--toolbar-bg-color-8" to
                Color(0xFFFCE7F3),
    )

private val cssVariableRegex =
    Regex(
        """^var\(\s*(--[^,\s)]+)(?:\s*,\s*([^)]+))?\s*\)$"""
    )

private fun resolveInlineColor(
    raw: String?,
    isDark: Boolean,
): Color? {
    val value =
        raw
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: return null

    val variableMatch =
        cssVariableRegex.matchEntire(
            value
        )

    if (variableMatch != null) {
        val variableName =
            variableMatch
                .groupValues
                .getOrNull(1)
                .orEmpty()

        val fallback =
            variableMatch
                .groupValues
                .getOrNull(2)
                ?.trim()
                .orEmpty()

        val colorMap =
            if (isDark) {
                darkRichColorMap
            } else {
                lightRichColorMap
            }

        return colorMap[
            variableName
        ] ?: fallback
            .takeIf { it.isNotBlank() }
            ?.let {
                parseCssColor(it)
            }
    }

    return parseCssColor(
        value
    )
}

private fun parseCssColor(
    value: String,
): Color? {
    return try {
        Color(
            value.toColorInt()
        )
    } catch (_: IllegalArgumentException) {
        null
    }
}

private val leadingPunctuationRegex =
    Regex(
        "^[，。！？；：、,.!?;:]+"
    )

private fun normalizeLeadingPunctuation(
    source: List<MobileInline>,
): List<MobileInline> {
    if (source.isEmpty()) {
        return emptyList()
    }

    val result =
        mutableListOf<MobileInline>()

    source.forEach { inline ->
        val text =
            inline.text

        if (text.isEmpty()) {
            return@forEach
        }

        val previous =
            result.lastOrNull()

        val match =
            leadingPunctuationRegex
                .find(text)

        if (
            previous != null &&
            match != null
        ) {
            val punctuation =
                match.value

            val remaining =
                text.substring(
                    punctuation.length
                )

            result[
                result.lastIndex
            ] =
                previous.copy(
                    text =
                        previous.text +
                                punctuation,
                )

            if (
                remaining.isNotEmpty()
            ) {
                result.add(
                    inline.copy(
                        text =
                            remaining,
                    )
                )
            }
        } else {
            result.add(
                inline
            )
        }
    }

    return result
}