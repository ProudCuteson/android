package top.role.android.feature.discover.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import top.role.android.core.model.content.MobileContentCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PublishedCard(
    item: MobileContentCard,
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
                verticalAlignment =
                    Alignment.CenterVertically,
            ) {
                Text(
                    text =
                        when (item.type) {
                            "doc" -> "文档"
                            "slide" -> "演示"
                            else -> "内容"
                        },
                    style =
                        MaterialTheme.typography.labelMedium,
                    color =
                        MaterialTheme.colorScheme.primary,
                )

                if (!item.canOpen) {
                    Spacer(
                        modifier =
                            Modifier.width(8.dp),
                    )

                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint =
                            MaterialTheme.colorScheme
                                .onSurfaceVariant,
                    )
                }
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
                    maxLines = 3,
                    overflow =
                        TextOverflow.Ellipsis,
                )
            }

            Row(
                modifier =
                    Modifier.padding(
                        top = 12.dp,
                    ),
                verticalAlignment =
                    Alignment.CenterVertically,
            ) {
                item.space.name
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?.let { spaceName ->
                        Text(
                            text = spaceName,
                            style =
                                MaterialTheme.typography.labelMedium,
                            color =
                                MaterialTheme.colorScheme
                                    .onSurfaceVariant,
                        )

                        Spacer(
                            modifier =
                                Modifier.width(8.dp),
                        )
                    }

                Text(
                    text =
                        formatContentTime(
                            item.updatedAt
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

private fun formatContentTime(
    timestamp: Double,
): String {
    if (timestamp <= 0) {
        return ""
    }

    return SimpleDateFormat(
        "yyyy-MM-dd",
        Locale.getDefault(),
    ).format(
        Date(timestamp.toLong())
    )
}