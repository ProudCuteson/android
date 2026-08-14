package top.role.android.feature.topic.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import top.role.android.core.model.topic.TopicSpaceRef
import top.role.android.core.model.topic.TopicSummary

@Composable
fun TopicHeader(
    topic: TopicSummary,
    space: TopicSpaceRef?,
    onSpaceClick: (() -> Unit)? = null,
    onMembershipClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                top = 20.dp,
                bottom = 8.dp,
            ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "话题",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            if (topic.locked) {
                Spacer(
                    modifier = Modifier.width(8.dp),
                )

                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Text(
            text = topic.title,
            modifier = Modifier.padding(
                top = 10.dp,
            ),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )

        if (topic.summary.isNotBlank()) {
            Text(
                text = topic.summary,
                modifier = Modifier.padding(
                    top = 8.dp,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Row(
            modifier = Modifier.padding(
                top = 14.dp,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            topic.itemCount?.let { count ->
                Text(
                    text = "${count.toInt()} 篇内容",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (
                topic.itemCount != null &&
                !topic.timeLabel.isNullOrBlank()
            ) {
                Text(
                    text = " · ",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            topic.timeLabel
                ?.takeIf {
                    it.isNotBlank()
                }
                ?.let { time ->
                    Text(
                        text = time,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
        }

        if (space != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 16.dp,
                    )
                    .then(
                        if (onSpaceClick != null) {
                            Modifier.clickable(
                                onClick = onSpaceClick,
                            )
                        } else {
                            Modifier
                        }
                    ),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 14.dp,
                            vertical = 12.dp,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = space.entityLabel ?: "空间",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        Text(
                            text = space.name,
                            modifier = Modifier.padding(
                                top = 2.dp,
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                        )
                    }

                    if (onSpaceClick != null) {
                        Icon(
                            imageVector =
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint =
                                MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        if (
            topic.lockReason == "member_required" &&
            space?.isMember != true
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 14.dp,
                    )
                    .then(
                        if (onMembershipClick != null) {
                            Modifier.clickable(
                                onClick = onMembershipClick,
                            )
                        } else {
                            Modifier
                        }
                    ),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "仅空间会员可查看",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Text(
                        text = "加入空间会员后可查看完整话题内容",
                        style = MaterialTheme.typography.bodySmall,
                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}