package top.role.android.core.model.chart

import top.role.android.core.model.slide.MobileSlideChartRuntimeConfig
import top.role.android.core.model.slide.MobileSlideChartType

fun MobileSlideChartRuntimeConfig.toMobileChartRuntime(): MobileChartRuntime {
    return MobileChartRuntime(
        chartId = chartId,

        chartType =
            when (chartType) {
                MobileSlideChartType.Area ->
                    MobileChartType.Area

                MobileSlideChartType.Bar ->
                    MobileChartType.Bar

                MobileSlideChartType.Line ->
                    MobileChartType.Line

                MobileSlideChartType.Pie ->
                    MobileChartType.Pie

                MobileSlideChartType.Radar ->
                    MobileChartType.Radar

                MobileSlideChartType.Radial ->
                    MobileChartType.Radial

                MobileSlideChartType.Scatter ->
                    MobileChartType.Scatter

                MobileSlideChartType.Composed ->
                    MobileChartType.Composed
            },

        title = title,
        description = description,
        data = data,
        xField = xField,
        xFieldLabel = xFieldLabel,

        yFields =
            yFields.map { field ->
                MobileChartYField(
                    key = field.key,
                    label = field.label,
                    color = field.color,
                    unit = field.unit,
                    axis = field.axis,
                    axisId = field.axisId,
                    seriesType = field.seriesType,
                    dot = field.dot,
                    strokeDasharray =
                        field.strokeDasharray,
                    opacity = field.opacity,
                    strokeWidth =
                        field.strokeWidth,
                )
            },

        options = options,
    )
}