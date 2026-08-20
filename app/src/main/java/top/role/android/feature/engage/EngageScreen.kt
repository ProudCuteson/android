package top.role.android.feature.engage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

import top.role.android.core.model.engage.MobileBountyCard
import top.role.android.core.model.engage.MobileMarketCard
import top.role.android.core.model.engage.MobileSurveyCard


@Composable
fun EngageScreen(
    state: EngageUiState,

    onRetry: () -> Unit,

    onSurveyClick: (String) -> Unit,
    onSurveyMore: () -> Unit,

    onBountyClick: (String) -> Unit,
    onBountyMore: () -> Unit,

    onMarketClick: (String) -> Unit,
    onMarketMore: () -> Unit,
) {
    when {
        state.signedOut -> {
            EngageSignedOutContent()
        }

        state.loading &&
                state.home == null -> {
            EngageLoadingContent()
        }

        state.home != null -> {
            val home =
                state.home

            EngageHomeContent(
                surveyItems =
                    home.surveyItems,

                bountyItems =
                    home.bountyItems,

                marketItems =
                    home.marketItems,

                errorMessage =
                    state.errorMessage,

                onRetry =
                    onRetry,

                onSurveyClick =
                    onSurveyClick,

                onSurveyMore =
                    onSurveyMore,

                onBountyClick =
                    onBountyClick,

                onBountyMore =
                    onBountyMore,

                onMarketClick =
                    onMarketClick,

                onMarketMore =
                    onMarketMore,
            )
        }

        state.errorMessage != null -> {
            EngageErrorContent(
                message =
                    state.errorMessage,

                onRetry =
                    onRetry,
            )
        }

        else -> {
            EngageEmptyContent()
        }
    }
}


@Composable
private fun EngageHomeContent(
    surveyItems:
    List<MobileSurveyCard>,

    bountyItems:
    List<MobileBountyCard>,

    marketItems:
    List<MobileMarketCard>,

    errorMessage:
    String?,

    onRetry: () -> Unit,

    onSurveyClick:
        (String) -> Unit,

    onSurveyMore:
        () -> Unit,

    onBountyClick:
        (String) -> Unit,

    onBountyMore:
        () -> Unit,

    onMarketClick:
        (String) -> Unit,

    onMarketMore:
        () -> Unit,
) {
    LazyColumn(
        modifier =
            Modifier.fillMaxSize(),

        contentPadding =
            PaddingValues(
                horizontal = 16.dp,
                vertical = 12.dp,
            ),

        verticalArrangement =
            Arrangement.spacedBy(
                24.dp
            ),
    ) {

        if (
            !errorMessage.isNullOrBlank()
        ) {
            item {
                EngageInlineError(
                    message =
                        errorMessage,

                    onRetry =
                        onRetry,
                )
            }
        }

        item {
            EngageSectionHeader(
                title =
                    "调查与测评",

                onMore =
                    onSurveyMore,
            )
        }

        if (
            surveyItems.isEmpty()
        ) {
            item {
                EngageSectionEmpty(
                    text =
                        "暂无推荐调查"
                )
            }
        } else {
            items(
                items =
                    surveyItems,

                key = {
                    "survey:${it.id}"
                },
            ) { item ->

                SurveyEngageCard(
                    item =
                        item,

                    onClick = {
                        onSurveyClick(
                            item.id
                        )
                    },
                )
            }
        }

        item {
            HorizontalDivider()
        }

        item {
            EngageSectionHeader(
                title =
                    "赏金任务",

                onMore =
                    onBountyMore,
            )
        }

        if (
            bountyItems.isEmpty()
        ) {
            item {
                EngageSectionEmpty(
                    text =
                        "暂无推荐任务"
                )
            }
        } else {
            items(
                items =
                    bountyItems,

                key = {
                    "bounty:${it.id}"
                },
            ) { item ->

                BountyEngageCard(
                    item =
                        item,

                    onClick = {
                        onBountyClick(
                            item.id
                        )
                    },
                )
            }
        }

        item {
            HorizontalDivider()
        }

        item {
            EngageSectionHeader(
                title =
                    "精选单品",

                onMore =
                    onMarketMore,
            )
        }

        if (
            marketItems.isEmpty()
        ) {
            item {
                EngageSectionEmpty(
                    text =
                        "暂无推荐单品"
                )
            }
        } else {
            items(
                items =
                    marketItems,

                key = {
                    "market:${it.id}"
                },
            ) { item ->

                MarketEngageCard(
                    item =
                        item,

                    onClick = {
                        onMarketClick(
                            item.id
                        )
                    },
                )
            }
        }
    }
}


@Composable
private fun EngageSectionHeader(
    title: String,
    onMore: () -> Unit,
) {
    Row(
        modifier =
            Modifier.fillMaxWidth(),

        horizontalArrangement =
            Arrangement.SpaceBetween,

        verticalAlignment =
            Alignment.CenterVertically,
    ) {
        Text(
            text =
                title,

            style =
                MaterialTheme
                    .typography
                    .titleMedium,

            fontWeight =
                FontWeight.SemiBold,
        )

        Text(
            text =
                "查看全部",

            modifier =
                Modifier.clickable {
                    onMore()
                },

            style =
                MaterialTheme
                    .typography
                    .labelLarge,

            color =
                MaterialTheme
                    .colorScheme
                    .primary,
        )
    }
}


@Composable
private fun SurveyEngageCard(
    item:
    MobileSurveyCard,

    onClick:
        () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable {
                    onClick()
                }
                .padding(
                    vertical = 6.dp
                ),

        verticalArrangement =
            Arrangement.spacedBy(
                6.dp
            ),
    ) {
        Text(
            text =
                item.title,

            style =
                MaterialTheme
                    .typography
                    .titleSmall,

            fontWeight =
                FontWeight.Medium,

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

        Row(
            horizontalArrangement =
                Arrangement.spacedBy(
                    12.dp
                ),
        ) {
            Text(
                text =
                    if (
                        item.responseKind ==
                        "report"
                    ) {
                        "测评"
                    } else {
                        "调查"
                    },

                style =
                    MaterialTheme
                        .typography
                        .labelMedium,

                color =
                    MaterialTheme
                        .colorScheme
                        .primary,
            )

            Text(
                text =
                    "${item.responses.toInt()} 次参与",

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
    }
}


@Composable
private fun BountyEngageCard(
    item:
    MobileBountyCard,

    onClick:
        () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable {
                    onClick()
                }
                .padding(
                    vertical = 6.dp
                ),

        verticalArrangement =
            Arrangement.spacedBy(
                6.dp
            ),
    ) {
        Text(
            text =
                item.title,

            style =
                MaterialTheme
                    .typography
                    .titleSmall,

            fontWeight =
                FontWeight.Medium,

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

        Row(
            horizontalArrangement =
                Arrangement.spacedBy(
                    12.dp
                ),
        ) {
            Text(
                text =
                    item.reward,

                style =
                    MaterialTheme
                        .typography
                        .labelLarge,

                color =
                    MaterialTheme
                        .colorScheme
                        .primary,
            )

            if (
                item.deadlineLabel
                    .isNotBlank()
            ) {
                Text(
                    text =
                        item.deadlineLabel,

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
        }
    }
}


@Composable
private fun MarketEngageCard(
    item:
    MobileMarketCard,

    onClick:
        () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable {
                    onClick()
                }
                .padding(
                    vertical = 6.dp
                ),

        verticalArrangement =
            Arrangement.spacedBy(
                6.dp
            ),
    ) {
        Text(
            text =
                item.title,

            style =
                MaterialTheme
                    .typography
                    .titleSmall,

            fontWeight =
                FontWeight.Medium,

            maxLines =
                2,

            overflow =
                TextOverflow.Ellipsis,
        )

        if (
            item.subtitle
                .isNotBlank()
        ) {
            Text(
                text =
                    item.subtitle,

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

        Row(
            horizontalArrangement =
                Arrangement.spacedBy(
                    12.dp
                ),
        ) {
            Text(
                text =
                    item.price,

                style =
                    MaterialTheme
                        .typography
                        .labelLarge,

                color =
                    MaterialTheme
                        .colorScheme
                        .primary,
            )

            Text(
                text =
                    item.kindLabel,

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
    }
}


@Composable
private fun EngageLoadingContent() {
    Column(
        modifier =
            Modifier.fillMaxSize(),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center,
    ) {
        CircularProgressIndicator()
    }
}


@Composable
private fun EngageSignedOutContent() {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center,
    ) {
        Text(
            text =
                "登录后查看参与内容",

            style =
                MaterialTheme
                    .typography
                    .bodyLarge,
        )
    }
}


@Composable
private fun EngageErrorContent(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center,
    ) {
        Text(
            text =
                message,

            style =
                MaterialTheme
                    .typography
                    .bodyMedium,
        )

        Button(
            modifier =
                Modifier.padding(
                    top = 16.dp
                ),

            onClick =
                onRetry,
        ) {
            Text(
                text =
                    "重试"
            )
        }
    }
}


@Composable
private fun EngageInlineError(
    message: String,
    onRetry: () -> Unit,
) {
    Row(
        modifier =
            Modifier.fillMaxWidth(),

        horizontalArrangement =
            Arrangement.SpaceBetween,

        verticalAlignment =
            Alignment.CenterVertically,
    ) {
        Text(
            text =
                message,

            modifier =
                Modifier.weight(1f),

            style =
                MaterialTheme
                    .typography
                    .bodySmall,

            color =
                MaterialTheme
                    .colorScheme
                    .error,
        )

        Text(
            text =
                "重试",

            modifier =
                Modifier
                    .padding(
                        start = 12.dp
                    )
                    .clickable {
                        onRetry()
                    },

            style =
                MaterialTheme
                    .typography
                    .labelLarge,

            color =
                MaterialTheme
                    .colorScheme
                    .primary,
        )
    }
}


@Composable
private fun EngageSectionEmpty(
    text: String,
) {
    Text(
        text =
            text,

        modifier =
            Modifier.padding(
                vertical = 8.dp
            ),

        style =
            MaterialTheme
                .typography
                .bodyMedium,

        color =
            MaterialTheme
                .colorScheme
                .onSurfaceVariant,
    )
}


@Composable
private fun EngageEmptyContent() {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center,
    ) {
        Text(
            text =
                "暂无参与内容",

            style =
                MaterialTheme
                    .typography
                    .bodyLarge,

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant,
        )
    }
}