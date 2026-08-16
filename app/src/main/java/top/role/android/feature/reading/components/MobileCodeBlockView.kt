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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import top.role.android.core.model.content.MobileCodeBlock

@Composable
fun MobileCodeBlockView(
    block: MobileCodeBlock,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color =
                    MaterialTheme.colorScheme
                        .surfaceContainerLow,
                shape =
                    RoundedCornerShape(
                        12.dp
                    ),
            )
            .padding(
                vertical = 12.dp,
            ),
    ) {
        block.language
            ?.takeIf { it.isNotBlank() }
            ?.let { language ->
                Text(
                    text = language,
                    modifier =
                        Modifier.padding(
                            start = 14.dp,
                            end = 14.dp,
                            bottom = 8.dp,
                        ),
                    style =
                        MaterialTheme.typography
                            .labelSmall,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant,
                )
            }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(
                    rememberScrollState()
                )
                .padding(
                    start = 10.dp,
                    end = 14.dp,
                ),
        ) {
            block.lines
                .forEachIndexed {
                        index,
                        line,
                    ->

                    Row(
                        modifier =
                            Modifier.padding(
                                vertical = 2.dp,
                            ),
                    ) {
                        Text(
                            text =
                                "${index + 1}",
                            modifier =
                                Modifier.width(
                                    34.dp
                                ),
                            style =
                                MaterialTheme
                                    .typography
                                    .bodySmall,
                            fontFamily =
                                FontFamily.Monospace,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                                    .copy(
                                        alpha = 0.5f
                                    ),
                            textAlign =
                                TextAlign.End,
                        )

                        Text(
                            text = line,
                            modifier =
                                Modifier.padding(
                                    start = 12.dp,
                                ),
                            style =
                                MaterialTheme
                                    .typography
                                    .bodyMedium,
                            fontFamily =
                                FontFamily.Monospace,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurface,
                            softWrap = false,
                        )
                    }
                }
        }
    }
}