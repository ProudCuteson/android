package top.role.android.feature.slide.render

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import top.role.android.core.model.slide.MobileSlideElement
import top.role.android.ui.chart.MobileChart
import top.role.android.ui.chart.MobileChartDisplay
import top.role.android.ui.chart.UnsupportedChartContent
import top.role.android.core.model.chart.toMobileChartRuntime

@Composable
fun SlideChartElement(
    element: MobileSlideElement,
    scale: Double,
    modifier: Modifier = Modifier,
) {
    val chart =
        element.chart

    if (chart == null) {
        UnsupportedChartContent(
            title =
                "图表不可用",
            modifier =
                modifier,
        )

        return
    }

    MobileChart(
        chart =
            chart.toMobileChartRuntime(),

        modifier =
            modifier,

        display =
            MobileChartDisplay(
                showTitle =
                    element.showTitle != false,

                showDescription =
                    true,

                showLegend =
                    true,

                compact =
                    true,

                scale =
                    scale,

                backgroundMode =
                    element.backgroundMode,
            ),
    )
}