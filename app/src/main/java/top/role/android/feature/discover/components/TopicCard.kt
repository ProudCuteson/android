package top.role.android.feature.discover.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Surface
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
    val memberRequired =
        !item.canOpen &&
                item.lockReason ==
                "member_required"

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(
                    onClick =
                        onClick,
                ),

        shape =
            RoundedCornerShape(
                18.dp
            ),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme
                        .colorScheme
                        .surfaceContainerLow,
            ),
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        16.dp
                    ),
        ) {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically,
            ) {

                Text(
                    text =
                        "话题",

                    style =
                        MaterialTheme
                            .typography
                            .labelMedium,

                    color =
                        MaterialTheme
                            .colorScheme
                            .primary,
                )


                if (
                    !item.canOpen
                ) {

                    Spacer(
                        modifier =
                            Modifier.padding(
                                horizontal = 4.dp,
                            ),
                    )


                    Icon(
                        imageVector =
                            Icons.Default.Lock,

                        contentDescription =
                            "内容受限",

                        tint =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant,
                    )
                }


                if (
                    memberRequired
                ) {

                    Spacer(
                        modifier =
                            Modifier.padding(
                                horizontal = 3.dp,
                            ),
                    )


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
                                    horizontal = 8.dp,
                                    vertical = 3.dp,
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


                Spacer(
                    modifier =
                        Modifier.weight(
                            1f
                        ),
                )


                Text(
                    text =
                        "${item.itemCount.toInt()} 篇",

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


            Text(
                text =
                    item.title,

                modifier =
                    Modifier.padding(
                        top = 8.dp,
                    ),

                style =
                    MaterialTheme
                        .typography
                        .titleMedium,

                fontWeight =
                    FontWeight.SemiBold,

                maxLines =
                    2,

                overflow =
                    TextOverflow.Ellipsis,
            )


            if (
                item.description
                    .isNotBlank()
            ) {

                Text(
                    text =
                        item.description,

                    modifier =
                        Modifier.padding(
                            top = 6.dp,
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


            item.space.name
                ?.takeIf {
                    it.isNotBlank()
                }
                ?.let { spaceName ->

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

                        Text(
                            text =
                                spaceName,

                            style =
                                MaterialTheme
                                    .typography
                                    .labelMedium,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant,
                        )


                        if (
                            memberRequired
                        ) {

                            Text(
                                text =
                                    "加入会员后可查看",

                                style =
                                    MaterialTheme
                                        .typography
                                        .labelSmall,

                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .primary,
                            )
                        }
                    }
                }
        }
    }
}