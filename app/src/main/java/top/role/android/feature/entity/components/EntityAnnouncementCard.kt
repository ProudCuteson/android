package top.role.android.feature.entity.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.role.android.core.model.entity.EntityAnnouncement
import androidx.compose.ui.text.font.FontWeight

@Composable
fun EntityAnnouncementCard(
    announcement: EntityAnnouncement,
    modifier: Modifier = Modifier,
) {
    val hasBlocks =
        announcement.blocks.isNotEmpty()

    val hasMarkdown =
        announcement.md.isNotBlank()

    if (!hasBlocks && !hasMarkdown) {
        return
    }

    val backgroundColor =
        when (announcement.style) {
            "info" ->
                MaterialTheme.colorScheme.primaryContainer
                    .copy(alpha = 0.35f)

            "success" ->
                MaterialTheme.colorScheme.tertiaryContainer
                    .copy(alpha = 0.35f)

            "warning" ->
                MaterialTheme.colorScheme.secondaryContainer
                    .copy(alpha = 0.45f)

            "note" ->
                MaterialTheme.colorScheme.surfaceVariant

            else ->
                MaterialTheme.colorScheme.surfaceContainerLow
        }

    val shape =
        RoundedCornerShape(18.dp)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color =
                    MaterialTheme.colorScheme.outlineVariant
                        .copy(alpha = 0.4f),
                shape = shape,
            ),
        shape = shape,
        color = backgroundColor,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement =
                Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "公告",
                style =
                    MaterialTheme.typography.titleMedium,
            )

            if (hasBlocks) {
                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(10.dp),
                ) {
                    announcement.blocks
                        .forEach { block ->

                            Column(
                                verticalArrangement =
                                    Arrangement.spacedBy(4.dp),
                            ) {
                                block.title
                                    ?.takeIf {
                                        it.isNotBlank()
                                    }
                                    ?.let { title ->
                                        Text(
                                            text = title,
                                            style =
                                                MaterialTheme
                                                    .typography
                                                    .bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }

                                block.subtitle
                                    ?.takeIf {
                                        it.isNotBlank()
                                    }
                                    ?.let { subtitle ->
                                        Text(
                                            text = subtitle,
                                            style =
                                                MaterialTheme
                                                    .typography
                                                    .labelMedium,
                                            color =
                                                MaterialTheme
                                                    .colorScheme
                                                    .onSurfaceVariant,
                                        )
                                    }

                                block.text
                                    ?.takeIf {
                                        it.isNotBlank()
                                    }
                                    ?.let { text ->
                                        Text(
                                            text = text,
                                            style =
                                                MaterialTheme
                                                    .typography
                                                    .bodyMedium,
                                        )
                                    }
                            }
                        }
                }
            } else if (hasMarkdown) {
                Text(
                    text = announcement.md,
                    style =
                        MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}