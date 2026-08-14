package top.role.android.feature.reading.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import top.role.android.core.model.doc.MobileDocInfo
import top.role.android.core.model.doc.MobileDocSpaceRef
import top.role.android.core.model.doc.MobileProfileRef

@Composable
fun ReadingHeader(
    doc: MobileDocInfo,
    author: MobileProfileRef?,
    space: MobileDocSpaceRef?,
    modifier: Modifier = Modifier,
    onAuthorClick: (() -> Unit)? = null,
    onSpaceClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = doc.title,
            style =
                MaterialTheme.typography
                    .headlineMedium
                    .copy(
                        fontWeight =
                            FontWeight.Bold,
                    ),
            color =
                MaterialTheme.colorScheme
                    .onBackground,
        )

        doc.description
            ?.takeIf {
                it.isNotBlank()
            }
            ?.let { description ->
                Text(
                    text = description,
                    modifier =
                        Modifier.padding(
                            top = 12.dp,
                        ),
                    style =
                        MaterialTheme.typography
                            .bodyMedium,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant,
                )
            }

        if (
            author != null ||
            space != null
        ) {
            ReadingMetaRow(
                author = author,
                space = space,
                modifier =
                    Modifier.padding(
                        top = 20.dp,
                    ),
                onAuthorClick =
                    onAuthorClick,
                onSpaceClick =
                    onSpaceClick,
            )
        }
    }
}

@Composable
private fun ReadingMetaRow(
    author: MobileProfileRef?,
    space: MobileDocSpaceRef?,
    modifier: Modifier = Modifier,
    onAuthorClick: (() -> Unit)?,
    onSpaceClick: (() -> Unit)?,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        author?.let {
            ReadingAuthorRow(
                author = it,
                onClick =
                    onAuthorClick,
            )
        }

        space
            ?.name
            ?.takeIf {
                it.isNotBlank()
            }
            ?.let { spaceName ->
                Surface(
                    modifier =
                        Modifier.padding(
                            top =
                                if (author != null) {
                                    10.dp
                                } else {
                                    0.dp
                                },
                        ),
                    shape =
                        MaterialTheme.shapes.small,
                    color =
                        MaterialTheme.colorScheme
                            .surfaceContainerLow,
                    onClick = {
                        onSpaceClick
                            ?.invoke()
                    },
                    enabled =
                        onSpaceClick != null,
                ) {
                    Text(
                        text =
                            buildString {
                                append("来自 ")

                                append(
                                    spaceName
                                )

                                space.number
                                    ?.let { number ->
                                        append(
                                            "  #${number.toInt()}"
                                        )
                                    }
                            },
                        modifier =
                            Modifier.padding(
                                horizontal =
                                    10.dp,
                                vertical =
                                    6.dp,
                            ),
                        style =
                            MaterialTheme.typography
                                .labelMedium,
                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant,
                    )
                }
            }
    }
}

@Composable
private fun ReadingAuthorRow(
    author: MobileProfileRef,
    onClick: (() -> Unit)?,
) {
    Surface(
        color =
            MaterialTheme.colorScheme
                .surface.copy(
                    alpha = 0f
                ),
        onClick = {
            onClick?.invoke()
        },
        enabled =
            onClick != null,
    ) {
        Row(
            verticalAlignment =
                Alignment.CenterVertically,
        ) {
            ReadingAuthorAvatar(
                author = author,
            )

            Spacer(
                modifier =
                    Modifier.width(
                        10.dp
                    ),
            )

            Column(
                modifier =
                    Modifier.weight(
                        1f
                    ),
            ) {
                Row(
                    verticalAlignment =
                        Alignment.CenterVertically,
                ) {
                    Text(
                        text =
                            author.name,
                        style =
                            MaterialTheme
                                .typography
                                .labelLarge,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurface,
                    )

                    author.number
                        ?.let { number ->
                            Text(
                                text =
                                    "#${number.toInt()}",
                                modifier =
                                    Modifier.padding(
                                        start =
                                            8.dp,
                                    ),
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

                author.subtitle
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?.let { subtitle ->
                        Text(
                            text =
                                subtitle,
                            modifier =
                                Modifier.padding(
                                    top =
                                        2.dp,
                                ),
                            style =
                                MaterialTheme
                                    .typography
                                    .bodySmall,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant,
                            maxLines = 2,
                        )
                    }
            }
        }
    }
}

@Composable
private fun ReadingAuthorAvatar(
    author: MobileProfileRef,
) {
    val avatarUrl =
        author.avatarUrl
            ?.takeIf {
                it.isNotBlank()
            }

    if (avatarUrl != null) {
        AsyncImage(
            model = avatarUrl,
            contentDescription =
                author.name,
            modifier = Modifier
                .size(40.dp)
                .clip(
                    CircleShape
                ),
            contentScale =
                ContentScale.Crop,
        )
    } else {
        Surface(
            modifier =
                Modifier.size(
                    40.dp
                ),
            shape =
                CircleShape,
            color =
                MaterialTheme
                    .colorScheme
                    .surfaceContainerHigh,
        ) {
            androidx.compose.foundation.layout.Box(
                contentAlignment =
                    Alignment.Center,
            ) {
                Text(
                    text =
                        author.name
                            .firstOrNull()
                            ?.uppercase()
                            ?: "U",
                    style =
                        MaterialTheme
                            .typography
                            .labelLarge,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurface,
                )
            }
        }
    }
}