package top.role.android.feature.reading.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.role.android.core.model.doc.MobileChartBlock

@Composable
fun MobileChartBlockView(
    block: MobileChartBlock,
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
                    RoundedCornerShape(12.dp),
            )
            .padding(16.dp),
    ) {
        Text(
            text =
                block.title
                    ?.takeIf { it.isNotBlank() }
                    ?: "图表",
            style =
                MaterialTheme.typography.titleMedium,
        )

        block.caption
            ?.takeIf { it.isNotBlank() }
            ?.let { caption ->
                Text(
                    text = caption,
                    modifier =
                        Modifier.padding(
                            top = 6.dp,
                        ),
                    style =
                        MaterialTheme.typography.bodySmall,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant,
                )
            }

        Text(
            text = "Chart · ${block.chartId}",
            modifier =
                Modifier.padding(
                    top = 12.dp,
                ),
            style =
                MaterialTheme.typography.labelSmall,
            color =
                MaterialTheme.colorScheme
                    .onSurfaceVariant,
        )
    }
}