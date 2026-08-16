package top.role.android.feature.slide.render

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.material3.MaterialTheme
import top.role.android.core.model.slide.MobileSlideElement
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

private enum class SlideConnectorDirection {
    LeftToRight,
    RightToLeft,
    TopToBottom,
    BottomToTop,
}

@Composable
internal fun SlideConnectorElement(
    element: MobileSlideElement,
    scale: Double,
    modifier: Modifier = Modifier,
) {
    val fallbackColor =
        MaterialTheme.colorScheme
            .onSurface
            .copy(
                alpha = 0.75f
            )

    val strokeColor =
        parseSlideColor(
            element.strokeColor
        )
            ?: fallbackColor

    val strokeWidth =
        max(
            1f,
            (
                    (element.strokeWidth ?: 2.0) *
                            scale
                    )
                .toFloat(),
        )

    val direction =
        resolveConnectorDirection(
            width =
                element.rect.w,

            height =
                element.rect.h,
        )

    val pathEffect =
        resolveConnectorDashEffect(
            dashStyle =
                element.dashStyle,

            scale =
                scale,
        )

    Canvas(
        modifier =
            modifier.fillMaxSize(),
    ) {
        if (
            size.width <= 0f ||
            size.height <= 0f
        ) {
            return@Canvas
        }

        drawSlideConnector(
            direction =
                direction,

            arrowHeadStart =
                element.arrowHeadStart == true,

            arrowHeadEnd =
                element.arrowHeadEnd == true,

            color =
                strokeColor,

            strokeWidth =
                strokeWidth,

            pathEffect =
                pathEffect,
        )
    }
}

private fun resolveConnectorDirection(
    width: Double,
    height: Double,
): SlideConnectorDirection {
    return if (
        abs(width) >=
        abs(height)
    ) {
        if (width >= 0.0) {
            SlideConnectorDirection.LeftToRight
        } else {
            SlideConnectorDirection.RightToLeft
        }
    } else {
        if (height >= 0.0) {
            SlideConnectorDirection.TopToBottom
        } else {
            SlideConnectorDirection.BottomToTop
        }
    }
}

private fun resolveConnectorDashEffect(
    dashStyle: String?,
    scale: Double,
): PathEffect? {
    return when (
        dashStyle
            ?.trim()
            ?.lowercase()
    ) {
        "dashed" ->
            PathEffect.dashPathEffect(
                intervals =
                    floatArrayOf(
                        (6.0 * scale)
                            .toFloat(),
                        (4.0 * scale)
                            .toFloat(),
                    )
            )

        "dotted" ->
            PathEffect.dashPathEffect(
                intervals =
                    floatArrayOf(
                        max(
                            1f,
                            (1.0 * scale)
                                .toFloat(),
                        ),
                        (4.0 * scale)
                            .toFloat(),
                    )
            )

        else ->
            null
    }
}

private fun DrawScope.drawSlideConnector(
    direction: SlideConnectorDirection,
    arrowHeadStart: Boolean,
    arrowHeadEnd: Boolean,
    color: Color,
    strokeWidth: Float,
    pathEffect: PathEffect?,
) {
    val start: Offset
    val end: Offset

    when (direction) {
        SlideConnectorDirection.LeftToRight -> {
            start =
                Offset(
                    x = 0f,
                    y = size.height / 2f,
                )

            end =
                Offset(
                    x = size.width,
                    y = size.height / 2f,
                )
        }

        SlideConnectorDirection.RightToLeft -> {
            start =
                Offset(
                    x = size.width,
                    y = size.height / 2f,
                )

            end =
                Offset(
                    x = 0f,
                    y = size.height / 2f,
                )
        }

        SlideConnectorDirection.TopToBottom -> {
            start =
                Offset(
                    x = size.width / 2f,
                    y = 0f,
                )

            end =
                Offset(
                    x = size.width / 2f,
                    y = size.height,
                )
        }

        SlideConnectorDirection.BottomToTop -> {
            start =
                Offset(
                    x = size.width / 2f,
                    y = size.height,
                )

            end =
                Offset(
                    x = size.width / 2f,
                    y = 0f,
                )
        }
    }

    /*
     * iOS 是同一个 Shape Path：
     *
     * main line
     * + start arrow head
     * + end arrow head
     *
     * Android 这里也放进同一个 Path，
     * 再统一 stroke，一致性最好。
     */
    val path =
        Path().apply {
            moveTo(
                start.x,
                start.y,
            )

            lineTo(
                end.x,
                end.y,
            )

            if (arrowHeadStart) {
                appendConnectorArrowHead(
                    path =
                        this,

                    tip =
                        start,

                    from =
                        end,

                    strokeWidth =
                        strokeWidth,
                )
            }

            if (arrowHeadEnd) {
                appendConnectorArrowHead(
                    path =
                        this,

                    tip =
                        end,

                    from =
                        start,

                    strokeWidth =
                        strokeWidth,
                )
            }
        }

    drawPath(
        path =
            path,

        color =
            color,

        style =
            Stroke(
                width =
                    strokeWidth,

                cap =
                    StrokeCap.Round,

                join =
                    StrokeJoin.Round,

                pathEffect =
                    pathEffect,
            ),
    )
}

private fun appendConnectorArrowHead(
    path: Path,
    tip: Offset,
    from: Offset,
    strokeWidth: Float,
) {
    val dx =
        tip.x -
                from.x

    val dy =
        tip.y -
                from.y

    val angle =
        atan2(
            dy,
            dx,
        )

    /*
     * 与 iOS：
     *
     * max(7, strokeWidth * 4)
     */
    val length =
        max(
            7f,
            strokeWidth *
                    4f,
        )

    /*
     * Swift:
     * CGFloat.pi / 7
     */
    val spread =
        PI /
                7.0

    val p1 =
        Offset(
            x =
                tip.x -
                        length *
                        cos(
                            angle -
                                    spread
                        )
                            .toFloat(),

            y =
                tip.y -
                        length *
                        sin(
                            angle -
                                    spread
                        )
                            .toFloat(),
        )

    val p2 =
        Offset(
            x =
                tip.x -
                        length *
                        cos(
                            angle +
                                    spread
                        )
                            .toFloat(),

            y =
                tip.y -
                        length *
                        sin(
                            angle +
                                    spread
                        )
                            .toFloat(),
        )

    path.moveTo(
        tip.x,
        tip.y,
    )

    path.lineTo(
        p1.x,
        p1.y,
    )

    path.moveTo(
        tip.x,
        tip.y,
    )

    path.lineTo(
        p2.x,
        p2.y,
    )
}