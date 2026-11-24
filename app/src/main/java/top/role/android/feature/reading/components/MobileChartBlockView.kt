package top.role.android.feature.reading.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import top.role.android.core.model.chart.MobileChartRuntime
import top.role.android.core.model.content.MobileChartBlock

import top.role.android.ui.chart.MobileChart
import top.role.android.ui.chart.MobileChartDisplay
import top.role.android.ui.chart.UnsupportedChartContent

@Composable
fun MobileChartBlockView(
    block: MobileChartBlock,
    runtime: MobileChartRuntime?,
    modifier: Modifier = Modifier,
) {
    val chartHeight =
        block.height
            ?.takeIf {
                it.isFinite() && it > 0
            }
            ?.coerceIn(240.0, 480.0)
            ?.dp
            ?: 300.dp

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight),
        ) {

            if (runtime == null) {

                UnsupportedChartContent(
                    title = "图表不可用",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(chartHeight),
                )

            } else {

                val chart =
                    block.title
                        ?.takeIf { it.isNotBlank() }
                        ?.let { title ->
                            runtime.copy(
                                title = title,
                            )
                        }
                        ?: runtime

                MobileChart(
                    chart = chart,

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(chartHeight),

                    display = MobileChartDisplay(
                        showTitle = true,

                        showDescription = false,

                        showLegend = true,

                        compact = false,

                        scale = 1.0,

                        backgroundMode = "transparent",
                    ),
                )
            }
        }

        block.caption
            ?.takeIf { it.isNotBlank() }
            ?.let { caption ->

                Text(
                    text = caption,

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 8.dp,
                            start = 4.dp,
                            end = 4.dp,
                        ),

                    style = readingCaptionStyle(),

                    textAlign =
                        resolveMobileTextAlign(
                            block.align
                        ),

                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant,
                )
            }
    }
}