package top.role.android.feature.slide.render

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import top.role.android.core.model.slide.MobileSlideElement
import top.role.android.core.model.slide.MobileSlidePage
import top.role.android.core.model.slide.MobileSlideRect
import top.role.android.core.model.slide.MobileSlideSummary
import top.role.android.core.model.slide.MobileSlideElementKind
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import top.role.android.ui.theme.LocalTopRoleDarkTheme
import kotlin.math.abs
import kotlin.math.roundToInt
import android.graphics.Color as AndroidColor

data class SlideResolvedRect(
    val left: Double,
    val top: Double,
    val width: Double,
    val height: Double,
)

fun MobileSlideRect.resolve(): SlideResolvedRect {
    val left =
        minOf(
            x,
            x + w,
        )

    val top =
        minOf(
            y,
            y + h,
        )

    return SlideResolvedRect(
        left = left,
        top = top,
        width = abs(w),
        height = abs(h),
    )
}

@Composable
fun SlidePageCanvas(
    slide: MobileSlideSummary,
    page: MobileSlidePage,
    modifier: Modifier = Modifier,
) {
    val canvasWidth =
        slide.canvasW
            .takeIf {
                it > 0.0
            }
            ?: 1920.0

    val canvasHeight =
        slide.canvasH
            .takeIf {
                it > 0.0
            }
            ?: 1080.0


    val isDark =
        LocalTopRoleDarkTheme.current


    val shape =
        RoundedCornerShape(
            12.dp
        )


    val borderColor =
        if (
            isDark
        ) {
            Color(
                0xFF454545
            )
        } else {
            MaterialTheme
                .colorScheme
                .outlineVariant
        }


    BoxWithConstraints(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(
                    shape
                )
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = shape,
                ),
    ) {
        val canvasWidthPx =
            constraints.maxWidth

        if (
            canvasWidthPx <= 0
        ) {
            return@BoxWithConstraints
        }


        val scale =
            canvasWidthPx.toDouble() /
                    canvasWidth


        val canvasHeightPx =
            (
                    canvasHeight *
                            scale
                    )
                .roundToInt()
                .coerceAtLeast(
                    1
                )


        SlideCanvasLayout(
            widthPx =
                canvasWidthPx,

            heightPx =
                canvasHeightPx,

            modifier =
                Modifier
                    .fillMaxWidth()
                    .requiredSize(
                        width =
                            maxWidth,

                        height =
                            with(
                                androidx.compose.ui.platform
                                    .LocalDensity
                                    .current
                            ) {
                                canvasHeightPx
                                    .toDp()
                            },
                    ),
        ) {

            SlidePageBackground(
                page =
                    page,

                modifier =
                    Modifier.fillMaxSize(),
            )


            page.elements
                .forEach { element ->

                    SlidePositionedElement(
                        element =
                            element,

                        scale =
                            scale,

                        allowVerticalOverflow =
                            element.type ==
                                    MobileSlideElementKind.Text,
                    ) {

                        SlideElementRenderer(
                            element =
                                element,

                            scale =
                                scale,
                        )
                    }
                }
        }
    }
}

@Composable
private fun SlideCanvasLayout(
    widthPx: Int,
    heightPx: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Layout(
        modifier = modifier,
        content = content,
    ) { measurables, _ ->

        val childConstraints =
            Constraints.fixed(
                width =
                    widthPx.coerceAtLeast(1),
                height =
                    heightPx.coerceAtLeast(1),
            )

        val placeables =
            measurables.map {
                it.measure(
                    childConstraints
                )
            }

        layout(
            width =
                widthPx.coerceAtLeast(1),
            height =
                heightPx.coerceAtLeast(1),
        ) {
            placeables.forEach {
                it.place(
                    x = 0,
                    y = 0,
                )
            }
        }
    }
}

@Composable
internal fun SlidePositionedElement(
    element: MobileSlideElement,
    scale: Double,
    allowVerticalOverflow: Boolean = false,
    content: @Composable () -> Unit,
) {
    val resolved =
        element.rect.resolve()

    val leftPx =
        (resolved.left * scale)
            .roundToInt()

    val topPx =
        (resolved.top * scale)
            .roundToInt()

    val widthPx =
        (resolved.width * scale)
            .roundToInt()
            .coerceAtLeast(1)

    val heightPx =
        (resolved.height * scale)
            .roundToInt()
            .coerceAtLeast(1)

    if (
        widthPx <= 1 ||
        heightPx <= 1
    ) {
        return
    }

    Layout(
        modifier =
            Modifier.zIndex(
                element.zIndex
                    .toFloat()
            ),
        content = content,
    ) { measurables, constraints ->

        val childConstraints =
            if (allowVerticalOverflow) {
                Constraints(
                    minWidth = widthPx,
                    maxWidth = widthPx,

                    minHeight = heightPx,

                    /*
                     * Text 允许突破原始 rect 高度。
                     *
                     * width 仍然严格保持，
                     * 所以不会改变文本换行宽度。
                     */
                    maxHeight =
                        Constraints.Infinity,
                )
            } else {
                Constraints.fixed(
                    width = widthPx,
                    height = heightPx,
                )
            }

        val child =
            measurables
                .firstOrNull()
                ?.measure(
                    childConstraints
                )

        layout(
            width =
                constraints.maxWidth,

            height =
                constraints.maxHeight,
        ) {
            child?.place(
                x = leftPx,
                y = topPx,
            )
        }
    }
}

@Composable
private fun SlidePageBackground(
    page: MobileSlidePage,
    modifier: Modifier = Modifier,
) {
    val background =
        page.background

    val isDark =
        LocalTopRoleDarkTheme.current


    val defaultColor =
        if (
            isDark
        ) {
            Color(
                0xFF121212
            )
        } else {
            Color.White
        }


    val color =
        when (
            background.kind
        ) {
            "color" -> {
                parseSlideColor(
                    background.color
                ) ?: defaultColor
            }

            else -> {
                defaultColor
            }
        }


    Box(
        modifier =
            modifier.background(
                color
            ),
    )
}

fun parseSlideColor(
    value: String?,
): Color? {
    val raw =
        value
            ?.trim()
            ?.takeIf {
                it.isNotEmpty()
            }
            ?: return null

    if (
        raw.equals(
            "transparent",
            ignoreCase = true,
        )
    ) {
        return Color.Transparent
    }

    return runCatching {
        Color(
            AndroidColor.parseColor(raw)
        )
    }.getOrNull()
}