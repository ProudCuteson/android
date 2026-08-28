package top.role.android.feature.chat.conversation

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import top.role.android.core.model.chat.MobileChatMessage

@Composable
internal fun ChatMessageActionDialog(
    message:
    MobileChatMessage,

    operating:
    Boolean,

    onDismiss:
        () -> Unit,

    onReply:
        () -> Unit,

    onRecall:
        () -> Unit,

    onDelete:
        () -> Unit,
) {
    AlertDialog(
        onDismissRequest =
            onDismiss,

        title = {
            Text(
                "消息操作"
            )
        },

        text = {
            Column {
                if (
                    message.deletedAt == null
                ) {
                    TextButton(
                        onClick =
                            onReply,

                        enabled =
                            !operating,
                    ) {
                        Text(
                            "回复"
                        )
                    }
                }

                if (
                    message.mine &&
                    message.deletedAt == null
                ) {
                    TextButton(
                        onClick =
                            onRecall,

                        enabled =
                            !operating,
                    ) {
                        Text(
                            "撤回"
                        )
                    }
                }

                TextButton(
                    onClick =
                        onDelete,

                    enabled =
                        !operating,
                ) {
                    Text(
                        "仅自己删除"
                    )
                }
            }
        },

        confirmButton = {},

        dismissButton = {
            TextButton(
                onClick =
                    onDismiss,
            ) {
                Text(
                    "取消"
                )
            }
        },
    )
}

@Composable
internal fun ChatFullScreenImageDialog(
    imageId:
    String,

    imagePreviewRepository:
    ChatImagePreviewRepository,

    onDismiss:
        () -> Unit,
) {
    var scale by
    remember(
        imageId
    ) {
        mutableFloatStateOf(
            1f
        )
    }

    var offset by
    remember(
        imageId
    ) {
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

            val scaleFactor =
                newScale /
                        oldScale

            scale =
                newScale

            offset =
                clampOffset(
                    targetOffset =
                        offset *
                                scaleFactor +
                                panChange,

                    targetScale =
                        newScale,
                )
        }

    Dialog(
        onDismissRequest =
            onDismiss,

        properties =
            DialogProperties(
                usePlatformDefaultWidth =
                    false,
            ),
    ) {
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
            ChatImagePreview(
                imageId =
                    imageId,

                kind =
                    "original",

                repository =
                    imagePreviewRepository,

                contentDescription =
                    "图片原图",

                contentScale =
                    ContentScale.Fit,

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
                            imageId
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
                                }
                            )
                        },
            )

            IconButton(
                onClick =
                    onDismiss,

                modifier =
                    Modifier
                        .align(
                            Alignment.TopEnd
                        )
                        .padding(
                            12.dp
                        ),
            ) {
                Icon(
                    imageVector =
                        Icons.Default.Close,

                    contentDescription =
                        "关闭",

                    tint =
                        Color.White,
                )
            }
        }
    }
}