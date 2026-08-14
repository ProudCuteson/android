package top.role.android.feature.discover.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import top.role.android.core.model.space.MobileSpaceCard

@Composable
fun EntityCard(
    item: MobileSpaceCard,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick,
            ),
        shape =
            RoundedCornerShape(18.dp),
        colors =
            CardDefaults.cardColors(
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
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text =
                        when (item.kind) {
                            "personal" -> "个人空间"
                            "group" -> "群组空间"
                            "org" -> "组织空间"
                            else -> "空间"
                        },
                    style =
                        MaterialTheme.typography.labelMedium,
                    color =
                        MaterialTheme.colorScheme.primary,
                )

                if (item.following) {
                    Spacer(
                        modifier =
                            Modifier.width(8.dp),
                    )

                    Text(
                        text = "已关注",
                        style =
                            MaterialTheme.typography.labelSmall,
                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Text(
                text = item.name,
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

            if (item.tags.isNotEmpty()) {
                Row(
                    modifier =
                        Modifier.padding(
                            top = 12.dp,
                        ),
                    horizontalArrangement =
                        Arrangement.spacedBy(6.dp),
                ) {
                    item.tags
                        .take(3)
                        .forEach { tag ->
                            Surface(
                                shape =
                                    RoundedCornerShape(
                                        999.dp
                                    ),
                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .surfaceContainerHighest,
                            ) {
                                Text(
                                    text = tag,
                                    modifier =
                                        Modifier.padding(
                                            horizontal = 9.dp,
                                            vertical = 4.dp,
                                        ),
                                    style =
                                        MaterialTheme
                                            .typography
                                            .labelSmall,
                                )
                            }
                        }
                }
            }
        }
    }
}