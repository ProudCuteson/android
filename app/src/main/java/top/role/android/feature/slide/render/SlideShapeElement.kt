package top.role.android.feature.slide.render

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import top.role.android.core.model.slide.MobileSlideElement
import kotlin.math.max

@Composable
fun SlideShapeElement(
    element: MobileSlideElement,
    scale: Double,
    modifier: Modifier = Modifier,
) {
    val density =
        LocalDensity.current

    val fillColor =
        parseSlideColor(
            element.fillColor
        )
            ?: Color.Transparent

    val strokeColor =
        parseSlideColor(
            element.strokeColor
        )
            ?: Color.Transparent

    val strokeWidthPx =
        (
                (element.strokeWidth ?: 0.0) *
                        scale
                )
            .toFloat()
            .coerceAtLeast(0f)

    val cornerRadiusPx =
        (
                (element.cornerRadius ?: 18.0) *
                        scale
                )
            .toFloat()
            .coerceAtLeast(0f)

    val opacity =
        (element.opacity ?: 1.0)
            .coerceIn(
                0.0,
                1.0,
            )
            .toFloat()

    val label =
        resolveShapeLabel(
            element
        )

    val labelPadding =
        with(density) {
            (4.0 * scale)
                .toFloat()
                .toDp()
        }

    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(opacity),
        contentAlignment =
            Alignment.Center,
    ) {
        Canvas(
            modifier =
                Modifier.fillMaxSize(),
        ) {
            when (
                element.shapeType
                    ?.trim()
                    ?.lowercase()
            ) {
                "circle",
                "ellipse" -> {
                    drawOval(
                        color =
                            fillColor,
                        topLeft =
                            Offset.Zero,
                        size =
                            size,
                        style =
                            Fill,
                    )

                    if (
                        strokeWidthPx > 0f &&
                        strokeColor !=
                        Color.Transparent
                    ) {
                        drawOval(
                            color =
                                strokeColor,
                            topLeft =
                                Offset.Zero,
                            size =
                                size,
                            style =
                                Stroke(
                                    width =
                                        strokeWidthPx,
                                ),
                        )
                    }
                }

                "pill" -> {
                    val radius =
                        size.height / 2f

                    drawRoundRect(
                        color =
                            fillColor,
                        topLeft =
                            Offset.Zero,
                        size =
                            size,
                        cornerRadius =
                            CornerRadius(
                                radius,
                                radius,
                            ),
                        style =
                            Fill,
                    )

                    if (
                        strokeWidthPx > 0f &&
                        strokeColor !=
                        Color.Transparent
                    ) {
                        drawRoundRect(
                            color =
                                strokeColor,
                            topLeft =
                                Offset.Zero,
                            size =
                                size,
                            cornerRadius =
                                CornerRadius(
                                    radius,
                                    radius,
                                ),
                            style =
                                Stroke(
                                    width =
                                        strokeWidthPx,
                                ),
                        )
                    }
                }

                "rounded-rect" -> {
                    drawRoundRect(
                        color =
                            fillColor,
                        topLeft =
                            Offset.Zero,
                        size =
                            size,
                        cornerRadius =
                            CornerRadius(
                                cornerRadiusPx,
                                cornerRadiusPx,
                            ),
                        style =
                            Fill,
                    )

                    if (
                        strokeWidthPx > 0f &&
                        strokeColor !=
                        Color.Transparent
                    ) {
                        drawRoundRect(
                            color =
                                strokeColor,
                            topLeft =
                                Offset.Zero,
                            size =
                                size,
                            cornerRadius =
                                CornerRadius(
                                    cornerRadiusPx,
                                    cornerRadiusPx,
                                ),
                            style =
                                Stroke(
                                    width =
                                        strokeWidthPx,
                                ),
                        )
                    }
                }

                "line",
                "arrow" -> {
                    drawLineShape(
                        color =
                            strokeColor,

                        strokeWidth =
                            max(
                                1f,
                                strokeWidthPx,
                            ),

                        arrowHeadEnd =
                            element
                                .arrowHeadEnd ==
                                    true,
                    )
                }

                "triangle" -> {
                    val path =
                        Path().apply {
                            moveTo(
                                size.width / 2f,
                                0f,
                            )

                            lineTo(
                                size.width,
                                size.height,
                            )

                            lineTo(
                                0f,
                                size.height,
                            )

                            close()
                        }

                    drawPath(
                        path =
                            path,
                        color =
                            fillColor,
                        style =
                            Fill,
                    )

                    if (
                        strokeWidthPx > 0f &&
                        strokeColor !=
                        Color.Transparent
                    ) {
                        drawPath(
                            path =
                                path,
                            color =
                                strokeColor,
                            style =
                                Stroke(
                                    width =
                                        strokeWidthPx,
                                    join =
                                        StrokeJoin.Round,
                                ),
                        )
                    }
                }

                "diamond" -> {
                    val path =
                        Path().apply {
                            moveTo(
                                size.width / 2f,
                                0f,
                            )

                            lineTo(
                                size.width,
                                size.height / 2f,
                            )

                            lineTo(
                                size.width / 2f,
                                size.height,
                            )

                            lineTo(
                                0f,
                                size.height / 2f,
                            )

                            close()
                        }

                    drawPath(
                        path =
                            path,
                        color =
                            fillColor,
                        style =
                            Fill,
                    )

                    if (
                        strokeWidthPx > 0f &&
                        strokeColor !=
                        Color.Transparent
                    ) {
                        drawPath(
                            path =
                                path,
                            color =
                                strokeColor,
                            style =
                                Stroke(
                                    width =
                                        strokeWidthPx,
                                    join =
                                        StrokeJoin.Round,
                                ),
                        )
                    }
                }

                else -> {
                    drawRect(
                        color =
                            fillColor,
                        topLeft =
                            Offset.Zero,
                        size =
                            size,
                        style =
                            Fill,
                    )

                    if (
                        strokeWidthPx > 0f &&
                        strokeColor !=
                        Color.Transparent
                    ) {
                        drawRect(
                            color =
                                strokeColor,
                            topLeft =
                                Offset.Zero,
                            size =
                                size,
                            style =
                                Stroke(
                                    width =
                                        strokeWidthPx,
                                ),
                        )
                    }
                }
            }
        }

        if (!label.isNullOrBlank()) {
            Text(
                text = label,
                modifier =
                    Modifier.padding(
                        labelPadding
                    ),
                style =
                    MaterialTheme
                        .typography
                        .labelSmall,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurface,
                textAlign =
                    TextAlign.Center,
                maxLines = 3,
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope
        .drawLineShape(
    color: Color,
    strokeWidth: Float,
    arrowHeadEnd: Boolean,
) {
    val midY =
        size.height / 2f

    val start =
        Offset(
            x = 0f,
            y = midY,
        )

    val end =
        Offset(
            x = size.width,
            y = midY,
        )

    drawLine(
        color =
            color,
        start =
            start,
        end =
            end,
        strokeWidth =
            strokeWidth,
        cap =
            StrokeCap.Round,
    )

    if (!arrowHeadEnd) {
        return
    }

    val arrowSize =
        max(
            6f,
            strokeWidth * 3f,
        )

    drawLine(
        color =
            color,
        start =
            end,
        end =
            Offset(
                x =
                    size.width -
                            arrowSize,
                y =
                    midY -
                            arrowSize / 2f,
            ),
        strokeWidth =
            strokeWidth,
        cap =
            StrokeCap.Round,
    )

    drawLine(
        color =
            color,
        start =
            end,
        end =
            Offset(
                x =
                    size.width -
                            arrowSize,
                y =
                    midY +
                            arrowSize / 2f,
            ),
        strokeWidth =
            strokeWidth,
        cap =
            StrokeCap.Round,
    )
}

private fun resolveShapeLabel(
    element: MobileSlideElement,
): String? {
    val label =
        element.label
            ?: return null

    return runCatching {
        label
            .jsonObject["text"]
            ?.jsonPrimitive
            ?.content
            ?.trim()
            ?.takeIf {
                it.isNotEmpty()
            }
    }.getOrNull()
}