package top.role.android.feature.slide.render

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import top.role.android.core.model.slide.MobileSlideElement
import top.role.android.core.model.slide.MobileSlideElementKind

@Composable
fun SlideElementRenderer(
    element: MobileSlideElement,
    scale: Double,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                rotationZ =
                    element.rotation
                        .toFloat()
            },
    ) {
        when (element.type) {
            MobileSlideElementKind.Text -> {
                SlideTextElement(
                    element = element,
                    scale = scale,
                )
            }

            MobileSlideElementKind.Image -> {
                SlideImageElement(
                    element = element,
                    scale = scale,
                )
            }

            MobileSlideElementKind.Shape -> {
                SlideShapeElement(
                    element = element,
                    scale = scale,
                )
            }

            MobileSlideElementKind.Table -> {
                SlideTableElement(
                    element = element,
                    scale = scale,
                )
            }

            MobileSlideElementKind.Chart -> {
                SlideChartElement(
                    element = element,
                    scale = scale,
                )
            }

            MobileSlideElementKind.Group,
            MobileSlideElementKind.Box -> {
                SlideContainerElement(
                    element = element,
                    scale = scale,
                )
            }

            MobileSlideElementKind.Connector -> {
                SlideConnectorElement(
                    element =
                        element,

                    scale =
                        scale,

                    modifier =
                        Modifier.fillMaxSize(),
                )
            }

            MobileSlideElementKind.Unsupported -> {
                SlideElementPlaceholder(
                    label =
                        element.originalType
                            ?.let {
                                "UNSUPPORTED: $it"
                            }
                            ?: "UNSUPPORTED",
                )
            }
        }
    }
}

@Composable
private fun SlideElementPlaceholder(
    label: String,
    background: Color =
        MaterialTheme.colorScheme
            .surfaceVariant
            .copy(
                alpha = 0.35f,
            ),
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                background
            )
            .padding(2.dp),
        contentAlignment =
            Alignment.Center,
    ) {
        Text(
            text = label,
            style =
                MaterialTheme.typography
                    .labelSmall,
            color =
                MaterialTheme.colorScheme
                    .onSurfaceVariant,
            textAlign =
                TextAlign.Center,
            maxLines = 1,
        )
    }
}