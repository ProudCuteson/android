package top.role.android.feature.discover.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
                .padding(
                    horizontal = 16.dp,
                    vertical = 15.dp,
                ),
        ) {

            /*
             * Type / Status
             */
            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.SpaceBetween,
            ) {
                Text(
                    text =
                        when (item.kind) {
                            "personal" ->
                                "个人空间"

                            "group" ->
                                "群组空间"

                            "org" ->
                                "组织空间"

                            else ->
                                "空间"
                        },
                    style =
                        MaterialTheme
                            .typography
                            .labelMedium,
                    fontWeight =
                        FontWeight.Medium,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,
                )

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically,
                    horizontalArrangement =
                        Arrangement.spacedBy(
                            6.dp
                        ),
                ) {

                    if (item.following) {
                        Text(
                            text =
                                "已关注",
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

                    if (item.membershipEnabled) {
                        Surface(
                            shape =
                                RoundedCornerShape(
                                    999.dp
                                ),
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .primaryContainer,
                        ) {
                            Text(
                                text =
                                    "会员",
                                modifier =
                                    Modifier.padding(
                                        horizontal =
                                            8.dp,
                                        vertical =
                                            3.dp,
                                    ),
                                style =
                                    MaterialTheme
                                        .typography
                                        .labelSmall,
                                fontWeight =
                                    FontWeight.Medium,
                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .onPrimaryContainer,
                            )
                        }
                    }
                }
            }

            /*
             * Name
             */
            Text(
                text =
                    item.name,
                modifier =
                    Modifier.padding(
                        top = 9.dp,
                    ),
                style =
                    MaterialTheme
                        .typography
                        .titleMedium,
                fontWeight =
                    FontWeight.SemiBold,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurface,
                maxLines =
                    2,
                overflow =
                    TextOverflow.Ellipsis,
            )

            /*
             * Description
             */
            if (
                item.description
                    .isNotBlank()
            ) {
                Text(
                    text =
                        item.description,
                    modifier =
                        Modifier.padding(
                            top = 5.dp,
                        ),
                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,
                    maxLines =
                        2,
                    overflow =
                        TextOverflow.Ellipsis,
                )
            }

            /*
             * Tags
             */
            if (
                item.tags
                    .isNotEmpty()
            ) {
                Row(
                    modifier =
                        Modifier.padding(
                            top = 12.dp,
                        ),
                    horizontalArrangement =
                        Arrangement.spacedBy(
                            6.dp
                        ),
                    verticalAlignment =
                        Alignment.CenterVertically,
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
                                    text =
                                        tag,
                                    modifier =
                                        Modifier.padding(
                                            horizontal =
                                                9.dp,
                                            vertical =
                                                4.dp,
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
                }
            }
        }
    }
}