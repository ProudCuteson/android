package top.role.android.feature.reading.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import top.role.android.core.model.content.MobileCodeBlock

@Composable
fun MobileCodeBlockView(
    block: MobileCodeBlock,
    modifier: Modifier = Modifier,
) {
    val wrap = block.wrap == true

    val showLineNumbers =
        block.showLineNumbers != false

    val startLine =
        block.startLine?.toInt() ?: 1

    val scrollState =
        rememberScrollState()

    val codeStyle =
        MaterialTheme.typography.bodyMedium.copy(
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            lineHeight = 22.sp,
            color =
                MaterialTheme.colorScheme.onSurface,
        )

    val lineNumberStyle =
        codeStyle.copy(
            color =
                MaterialTheme.colorScheme
                    .onSurfaceVariant
                    .copy(alpha = 0.5f),
        )

    val lastLine =
        startLine + (block.lines.size - 1)
            .coerceAtLeast(0)

    val numberDigits =
        maxOf(
            startLine.toString().length,
            lastLine.toString().length,
        )

    val lineNumberWidth =
        (numberDigits * 9 + 12)
            .coerceAtLeast(34)
            .dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color =
                    MaterialTheme.colorScheme
                        .surfaceContainerLow,
                shape =
                    RoundedCornerShape(12.dp),
            )
            .padding(vertical = 12.dp),
    ) {

        /*
         * 代码语言。
         */
        block.language
            ?.takeIf { it.isNotBlank() }
            ?.let { language ->

                Text(
                    text = language,
                    modifier = Modifier.padding(
                        start = 14.dp,
                        end = 14.dp,
                        bottom = 8.dp,
                    ),
                    style =
                        MaterialTheme.typography.labelSmall,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant,
                )
            }

        /*
         * 代码正文。
         *
         * wrap = true 时适应阅读宽度。
         * wrap = false 时允许水平滚动。
         */
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (wrap) {
                        Modifier
                    } else {
                        Modifier.horizontalScroll(
                            scrollState
                        )
                    }
                )
                .padding(
                    start = 10.dp,
                    end = 14.dp,
                ),
        ) {

            block.lines.forEachIndexed { index, line ->

                key(line.id) {

                    Row(
                        modifier =
                            if (wrap) {
                                Modifier.fillMaxWidth()
                            } else {
                                Modifier
                            },
                    ) {

                        if (showLineNumbers) {
                            Text(
                                text =
                                    (startLine + index)
                                        .toString(),

                                modifier =
                                    Modifier.width(
                                        lineNumberWidth
                                    ),

                                style = lineNumberStyle,

                                textAlign = TextAlign.End,

                                softWrap = false,
                            )
                        }

                        val lineModifier =
                            Modifier
                                .then(
                                    if (wrap) {
                                        Modifier.weight(1f)
                                    } else {
                                        Modifier
                                    }
                                )
                                .padding(
                                    start =
                                        if (showLineNumbers) {
                                            12.dp
                                        } else {
                                            4.dp
                                        },
                                )

                        /*
                         * 保留空白代码行。
                         */
                        if (
                            line.inlines.isEmpty() ||
                            line.inlines.all {
                                it.text.isEmpty()
                            }
                        ) {
                            Text(
                                text = " ",
                                modifier = lineModifier,
                                style = codeStyle,
                                softWrap = wrap,
                            )
                        } else {
                            MobileInlineText(
                                inlines = line.inlines,
                                modifier = lineModifier,
                                baseStyle = codeStyle,
                                textAlign = TextAlign.Left,
                                softWrap = wrap,
                            )
                        }
                    }
                }
            }
        }
    }
}