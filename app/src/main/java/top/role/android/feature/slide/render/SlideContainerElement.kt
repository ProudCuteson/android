package top.role.android.feature.slide.render

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import top.role.android.core.model.slide.MobileSlideElement
import top.role.android.core.model.slide.MobileSlideElementKind

@Composable
fun SlideContainerElement(
    element: MobileSlideElement,
    scale: Double,
    modifier: Modifier = Modifier,
) {
    val children =
        element.children
            .orEmpty()
            .sortedWith(
                compareBy<MobileSlideElement> {
                    it.zIndex
                }.thenBy {
                    it.id
                }
            )

    Box(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds(),
    ) {
        children.forEach { child ->
            SlidePositionedElement(
                element = child,
                scale = scale,
                allowVerticalOverflow =
                    child.type ==
                            MobileSlideElementKind.Text,
            ) {
                SlideElementRenderer(
                    element = child,
                    scale = scale,
                )
            }
        }
    }
}