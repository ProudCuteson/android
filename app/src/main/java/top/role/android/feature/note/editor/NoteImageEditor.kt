package top.role.android.feature.note.editor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import top.role.android.core.database.entity.LocalNoteImageEntity
import top.role.android.core.ui.imageviewer.PreviewableImage
import java.io.File

@Composable
fun NoteImageEditor(
    images: List<LocalNoteImageEntity>,
    onAddImage: (Uri) -> Unit,
    onRetryUpload: (String) -> Unit,
    onRemoveImage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val imagePicker =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts
                    .PickVisualMedia(),
        ) { uri ->
            if (
                uri != null
            ) {
                onAddImage(
                    uri,
                )
            }
        }

    Column(
        modifier =
            modifier,

        verticalArrangement =
            Arrangement.spacedBy(
                8.dp,
            ),
    ) {
        if (
            images.isNotEmpty()
        ) {
            LazyRow(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(
                        10.dp,
                    ),
            ) {
                items(
                    items =
                        images,

                    key = {
                        it.localImageKey
                    },
                ) { image ->
                    NoteImageEditorItem(
                        image =
                            image,

                        onRetryUpload = {
                            onRetryUpload(
                                image.localImageKey,
                            )
                        },

                        onRemoveImage = {
                            onRemoveImage(
                                image.localImageKey,
                            )
                        },
                    )
                }
            }
        }

        Row(
            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.Start,

            verticalAlignment =
                Alignment.CenterVertically,
        ) {
            FilledTonalIconButton(
                onClick = {
                    imagePicker.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts
                                .PickVisualMedia
                                .ImageOnly,
                        )
                    )
                },
            ) {
                Icon(
                    imageVector =
                        Icons.Default
                            .AddPhotoAlternate,

                    contentDescription =
                        "添加图片",
                )
            }
        }
    }
}

@Composable
private fun NoteImageEditorItem(
    image: LocalNoteImageEntity,
    onRetryUpload: () -> Unit,
    onRemoveImage: () -> Unit,
) {
    val localUrl =
        remember(
            image.localPath,
        ) {
            File(
                image.localPath,
            )
                .toUri()
                .toString()
        }

    Column(
        verticalArrangement =
            Arrangement.spacedBy(
                4.dp,
            ),
    ) {
        Box(
            modifier =
                Modifier.size(
                    104.dp,
                ),
        ) {
            PreviewableImage(
                imageUrl =
                    localUrl,

                contentDescription =
                    "记录图片",

                modifier =
                    Modifier
                        .fillMaxSize()
                        .clip(
                            MaterialTheme
                                .shapes
                                .medium,
                        ),

                contentScale =
                    ContentScale.Crop,
            )

            if (
                image.uploadState ==
                "uploading"
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(
                                Color.Black.copy(
                                    alpha =
                                        0.28f,
                                )
                            ),

                    contentAlignment =
                        Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier =
                            Modifier.size(
                                28.dp,
                            ),
                    )
                }
            }

            IconButton(
                onClick =
                    onRemoveImage,

                modifier =
                    Modifier
                        .align(
                            Alignment.TopEnd,
                        )
                        .size(
                            36.dp,
                        ),
            ) {
                Icon(
                    imageVector =
                        Icons.Default.Delete,

                    contentDescription =
                        "删除图片",

                    tint =
                        Color.White,
                )
            }
        }

        when (
            image.uploadState
        ) {
            "local" -> {
                Text(
                    text =
                        "等待上传",

                    style =
                        MaterialTheme
                            .typography
                            .labelSmall,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,
                )
            }

            "uploading" -> {
                Text(
                    text =
                        "上传中",

                    style =
                        MaterialTheme
                            .typography
                            .labelSmall,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,
                )
            }

            "failed" -> {
                TextButton(
                    onClick =
                        onRetryUpload,
                ) {
                    Icon(
                        imageVector =
                            Icons.Default.Refresh,

                        contentDescription =
                            null,
                    )

                    Text(
                        text =
                            "重试",
                    )
                }
            }

            "uploaded" -> {
                Text(
                    text =
                        "已上传",

                    style =
                        MaterialTheme
                            .typography
                            .labelSmall,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,
                )
            }
        }
    }
}