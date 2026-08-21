package top.role.android.feature.entity.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
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
import top.role.android.core.model.entity.EntityDetail
import top.role.android.core.ui.imageviewer.PreviewableImage

@Composable
fun EntityHeader(
    entity: EntityDetail,
    onMembershipClick: () -> Unit = {},
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
        EntityIdentityRow(
            entity = entity,
        )

        if (entity.subtitle.isNotBlank()) {
            Text(
                text = entity.subtitle,
                modifier = Modifier.padding(
                    top = 12.dp,
                ),
                style =
                    MaterialTheme.typography.bodyMedium,
                color =
                    MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (entity.tags.isNotEmpty()) {
            EntityTags(
                tags = entity.tags,
                modifier = Modifier.padding(
                    top = 14.dp,
                ),
            )
        }

        entity.statsLabel
            ?.takeIf {
                it.isNotBlank()
            }
            ?.let { stats ->
                Text(
                    text = stats,
                    modifier = Modifier.padding(
                        top = 14.dp,
                    ),
                    style =
                        MaterialTheme.typography.labelMedium,
                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

        val isManager =
            entity.role == "owner" ||
                    entity.role == "admin"

        if (entity.isMember == true) {
            Text(
                text =
                    resolveMemberRoleLabel(
                        entity.role,
                    ),
                modifier =
                    Modifier.padding(
                        top = 6.dp,
                    ),
                style =
                    MaterialTheme.typography.labelMedium,
                color =
                    MaterialTheme.colorScheme.primary,
            )
        }

        if (
            entity.membershipEnabled && !isManager
        ) {
            OutlinedButton(
                onClick = onMembershipClick,
                modifier = Modifier.padding(
                    top = 12.dp,
                ),
            ) {
                Text(
                    text =
                        if (entity.isMember == true) {
                            "会员中心"
                        } else {
                            "开通会员"
                        },
                )
            }
        }
    }
}

@Composable
private fun EntityIdentityRow(
    entity: EntityDetail,
) {
    Row(
        modifier =
            Modifier.fillMaxWidth(),
        verticalAlignment =
            Alignment.CenterVertically,
    ) {
        EntityAvatar(
            entity = entity,
        )

        Spacer(
            modifier =
                Modifier.width(14.dp),
        )

        Column(
            modifier =
                Modifier.weight(1f),
        ) {
            Row(
                verticalAlignment =
                    Alignment.CenterVertically,
            ) {
                Text(
                    text =
                        entity.entityLabel,
                    style =
                        MaterialTheme.typography.labelMedium,
                    color =
                        MaterialTheme.colorScheme.primary,
                )

                entity.number
                    ?.let { number ->
                        Text(
                            text =
                                " · No.${number.toInt()}",
                            style =
                                MaterialTheme.typography.labelMedium,
                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
            }

            Text(
                text = entity.title,
                modifier =
                    Modifier.padding(
                        top = 4.dp,
                    ),
                style =
                    MaterialTheme.typography.headlineSmall,
                fontWeight =
                    FontWeight.SemiBold,
                color =
                    MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun EntityAvatar(
    entity: EntityDetail,
) {
    val imageUrl =
        entity.cover
            ?.takeIf {
                it.isNotBlank()
            }

    if (imageUrl != null) {
        PreviewableImage(
            imageUrl = imageUrl,
            contentDescription =
                entity.title,
            modifier = Modifier
                .size(64.dp)
                .clip(
                    CircleShape,
                ),
            contentScale =
                ContentScale.Crop,
        )
    } else {
        Surface(
            modifier =
                Modifier.size(64.dp),
            shape =
                CircleShape,
            color =
                MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Box(
                contentAlignment =
                    Alignment.Center,
            ) {
                Text(
                    text =
                        entity.title
                            .firstOrNull()
                            ?.uppercase()
                            ?: "S",
                    style =
                        MaterialTheme.typography.titleLarge,
                    fontWeight =
                        FontWeight.SemiBold,
                    color =
                        MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun EntityTags(
    tags: List<String>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement =
            Arrangement.spacedBy(6.dp),
    ) {
        tags
            .take(4)
            .forEach { tag ->
                Surface(
                    shape =
                        RoundedCornerShape(
                            999.dp
                        ),
                    color =
                        MaterialTheme.colorScheme
                            .surfaceContainerHighest,
                ) {
                    Text(
                        text = tag,
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 5.dp,
                        ),
                        style =
                            MaterialTheme.typography.labelSmall,
                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
    }
}

private fun resolveMemberRoleLabel(
    role: String?,
): String =
    when (role) {
        "owner" ->
            "空间所有者"

        "admin" ->
            "空间管理员"

        "member" ->
            "空间成员"

        else ->
            "空间成员"
    }