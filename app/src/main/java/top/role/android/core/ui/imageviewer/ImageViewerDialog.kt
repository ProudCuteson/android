package top.role.android.core.ui.imageviewer

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.safeDrawing

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.layout.onSizeChanged

@Composable
fun ImageViewerDialog(
    imageUrl: String?,
    onDismiss: () -> Unit,
) {
    val url =
        imageUrl
            ?.takeIf {
                it.isNotBlank()
            }
            ?: return

    var scale by
    remember(url) {
        mutableFloatStateOf(
            1f
        )
    }

    var offset by
    remember(url) {
        mutableStateOf(
            Offset.Zero
        )
    }

    var containerSize by
    remember {
        mutableStateOf(
            IntSize.Zero
        )
    }

    fun applyResistance(
        value: Float,
        min: Float,
        max: Float,
    ): Float {
        return when {
            value < min -> {
                val overflow =
                    value - min

                min +
                        overflow *
                        0.35f
            }

            value > max -> {
                val overflow =
                    value - max

                max +
                        overflow *
                        0.35f
            }

            else ->
                value
        }
    }

    fun clampOffset(
        targetOffset: Offset,
        targetScale: Float,
    ): Offset {
        if (
            targetScale <= 1f ||
            containerSize.width <= 0 ||
            containerSize.height <= 0
        ) {
            return Offset.Zero
        }

        val maxX =
            containerSize.width *
                    (targetScale - 1f) /
                    2f

        val maxY =
            containerSize.height *
                    (targetScale - 1f) /
                    2f

        return Offset(
            x =
                applyResistance(
                    value =
                        targetOffset.x,

                    min =
                        -maxX,

                    max =
                        maxX,
                ),

            y =
                applyResistance(
                    value =
                        targetOffset.y,

                    min =
                        -maxY,

                    max =
                        maxY,
                ),
        )
    }


    val transformableState =
        rememberTransformableState {
                zoomChange,
                panChange,
                _ ->

            val oldScale =
                scale

            val newScale =
                (
                        oldScale *
                                zoomChange
                        ).coerceIn(
                        1f,
                        5f,
                    )

            if (
                newScale <= 1f
            ) {
                scale =
                    1f

                offset =
                    Offset.Zero

                return@rememberTransformableState
            }

            scale =
                newScale

            offset =
                clampOffset(
                    targetOffset =
                        offset +
                                panChange,

                    targetScale =
                        newScale,
                )
        }

    Dialog(
        onDismissRequest = onDismiss,
        properties =
            DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
            ),
    ) {
        BackHandler {
            onDismiss()
        }

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black
                    )
                    .onSizeChanged {
                        containerSize =
                            it
                    }
        ) {
            AsyncImage(
                model = url,
                contentDescription = null,

                modifier =
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX =
                                scale

                            scaleY =
                                scale

                            translationX =
                                offset.x

                            translationY =
                                offset.y
                        }
                        .transformable(
                            state =
                                transformableState,
                        )
                        .pointerInput(
                            url,
                            containerSize,
                        ) {
                            detectTapGestures(

                                onDoubleTap = {
                                        tapOffset ->

                                    if (
                                        scale > 1f
                                    ) {
                                        scale =
                                            1f

                                        offset =
                                            Offset.Zero
                                    } else {
                                        val targetScale =
                                            2.5f

                                        val center =
                                            Offset(
                                                x =
                                                    containerSize.width /
                                                            2f,

                                                y =
                                                    containerSize.height /
                                                            2f,
                                            )

                                        scale =
                                            targetScale

                                        offset =
                                            clampOffset(
                                                targetOffset =
                                                    (
                                                            center -
                                                                    tapOffset
                                                            ) *
                                                            (
                                                                    targetScale -
                                                                            1f
                                                                    ),

                                                targetScale =
                                                    targetScale,
                                            )
                                    }
                                },
                            )
                        },

                contentScale =
                    ContentScale.Fit,
            )


            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing,
                    )
                    .padding(
                        top = 8.dp,
                        end = 8.dp,
                    ),
            ) {
                Icon(
                    imageVector =
                        Icons.Default.Close,
                    contentDescription =
                        "关闭图片",
                    tint =
                        Color.White,
                )
            }
        }
    }
}