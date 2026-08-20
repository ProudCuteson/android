package top.role.android.feature.survey

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

import top.role.android.core.model.engage.MobileSurveyCard
import top.role.android.core.model.survey.MobileSurveyListMode


@Composable
fun SurveyListScreen(
    state:
    SurveyListUiState,

    onModeChange:
        (MobileSurveyListMode) -> Unit,

    onSurveyClick:
        (String) -> Unit,

    onLoadMore:
        () -> Unit,

    onRetry:
        () -> Unit,
) {
    when {
        state.signedOut -> {
            SurveySignedOutContent()
        }

        state.loading &&
                state.items.isEmpty() -> {
            SurveyLoadingContent()
        }

        state.errorMessage != null &&
                state.items.isEmpty() -> {
            SurveyErrorContent(
                message =
                    state.errorMessage,

                onRetry =
                    onRetry,
            )
        }

        else -> {
            SurveyListContent(
                state =
                    state,

                onModeChange =
                    onModeChange,

                onSurveyClick =
                    onSurveyClick,

                onLoadMore =
                    onLoadMore,

                onRetry =
                    onRetry,
            )
        }
    }
}


@Composable
private fun SurveyListContent(
    state:
    SurveyListUiState,

    onModeChange:
        (MobileSurveyListMode) -> Unit,

    onSurveyClick:
        (String) -> Unit,

    onLoadMore:
        () -> Unit,

    onRetry:
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
                16.dp
            ),
    ) {

        item {
            SurveyModeTabs(
                selectedMode =
                    state.mode,

                onModeChange =
                    onModeChange,
            )
        }

        if (
            state.items.isEmpty()
        ) {
            item {
                Text(
                    text =
                        when (
                            state.mode
                        ) {
                            MobileSurveyListMode.Featured ->
                                "暂无推荐调查"

                            MobileSurveyListMode.Latest ->
                                "暂无最新调查"
                        },

                    modifier =
                        Modifier.padding(
                            vertical = 24.dp
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
        } else {
            items(
                items =
                    state.items,

                key = {
                    it.id
                },
            ) { item ->

                SurveyListCard(
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

        if (
            !state.errorMessage
                .isNullOrBlank() &&
            state.items.isNotEmpty()
        ) {
            item {
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
                            state.errorMessage,

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

                        color =
                            MaterialTheme
                                .colorScheme
                                .primary,
                    )
                }
            }
        }

        if (
            state.loadingMore
        ) {
            item {
                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        } else if (
            !state.isDone &&
            state.continueCursor
                .isNotBlank()
        ) {
            item {
                Button(
                    modifier =
                        Modifier.fillMaxWidth(),

                    onClick =
                        onLoadMore,
                ) {
                    Text(
                        text =
                            "加载更多"
                    )
                }
            }
        }
    }
}


@Composable
private fun SurveyModeTabs(
    selectedMode:
    MobileSurveyListMode,

    onModeChange:
        (MobileSurveyListMode) -> Unit,
) {
    Row(
        horizontalArrangement =
            Arrangement.spacedBy(
                20.dp
            ),
    ) {

        SurveyModeTab(
            text =
                "推荐",

            selected =
                selectedMode ==
                        MobileSurveyListMode.Featured,

            onClick = {
                onModeChange(
                    MobileSurveyListMode.Featured
                )
            },
        )

        SurveyModeTab(
            text =
                "最新",

            selected =
                selectedMode ==
                        MobileSurveyListMode.Latest,

            onClick = {
                onModeChange(
                    MobileSurveyListMode.Latest
                )
            },
        )
    }
}


@Composable
private fun SurveyModeTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Text(
        text =
            text,

        modifier =
            Modifier
                .clickable {
                    onClick()
                }
                .padding(
                    vertical = 8.dp
                ),

        style =
            MaterialTheme
                .typography
                .titleSmall,

        fontWeight =
            if (selected) {
                FontWeight.SemiBold
            } else {
                FontWeight.Normal
            },

        color =
            if (selected) {
                MaterialTheme
                    .colorScheme
                    .primary
            } else {
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
            },
    )
}


@Composable
private fun SurveyListCard(
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
                    vertical = 8.dp
                ),

        verticalArrangement =
            Arrangement.spacedBy(
                6.dp
            ),
    ) {
        Row(
            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.SpaceBetween,
        ) {
            Text(
                text =
                    item.title,

                modifier =
                    Modifier.weight(1f),

                style =
                    MaterialTheme
                        .typography
                        .titleMedium,

                fontWeight =
                    FontWeight.Medium,

                maxLines =
                    2,

                overflow =
                    TextOverflow.Ellipsis,
            )

            if (
                item.featured
            ) {
                Text(
                    text =
                        "推荐",

                    modifier =
                        Modifier.padding(
                            start = 12.dp
                        ),

                    style =
                        MaterialTheme
                            .typography
                            .labelMedium,

                    color =
                        MaterialTheme
                            .colorScheme
                            .primary,
                )
            }
        }

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
                    3,

                overflow =
                    TextOverflow.Ellipsis,
            )
        }

        Row(
            horizontalArrangement =
                Arrangement.spacedBy(
                    16.dp
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

            if (
                item.views > 0
            ) {
                Text(
                    text =
                        "${item.views.toInt()} 次浏览",

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
private fun SurveyLoadingContent() {
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
private fun SurveySignedOutContent() {
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
                "登录后查看调查内容"
        )
    }
}


@Composable
private fun SurveyErrorContent(
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
                message
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