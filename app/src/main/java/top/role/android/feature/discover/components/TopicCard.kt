package top.role.android.feature.discover.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import top.role.android.core.model.topic.MobileTopicCard

@Composable
fun TopicCard(
    item: MobileTopicCard,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                enabled = item.canOpen,
                onClick = onClick,
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically,
            ) {
                Text(
                    text = "话题",
                    style =
                        MaterialTheme.typography.labelMedium,
                    color =
                        MaterialTheme.colorScheme.primary,
                )

                if (!item.canOpen) {
                    Spacer(
                        modifier =
                            Modifier.padding(
                                horizontal = 4.dp,
                            ),
                    )

                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant,
                    )
                }

                Spacer(
                    modifier =
                        Modifier.weight(1f),
                )

                Text(
                    text =
                        "${item.itemCount.toInt()} 篇",
                    style =
                        MaterialTheme.typography.labelMedium,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant,
                )
            }

            Text(
                text = item.title,
                modifier =
                    Modifier.padding(
                        top = 8.dp,
                    ),
                style =
                    MaterialTheme.typography.titleMedium,
                fontWeight =
                    FontWeight.SemiBold,
                maxLines = 2,
                overflow =
                    TextOverflow.Ellipsis,
            )

            if (item.description.isNotBlank()) {
                Text(
                    text = item.description,
                    modifier =
                        Modifier.padding(
                            top = 6.dp,
                        ),
                    style =
                        MaterialTheme.typography.bodyMedium,
                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow =
                        TextOverflow.Ellipsis,
                )
            }

            item.space.name
                ?.takeIf {
                    it.isNotBlank()
                }
                ?.let { spaceName ->
                    Text(
                        text = spaceName,
                        modifier =
                            Modifier.padding(
                                top = 12.dp,
                            ),
                        style =
                            MaterialTheme.typography.labelMedium,
                        color =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant,
                    )
                }
        }
    }
}