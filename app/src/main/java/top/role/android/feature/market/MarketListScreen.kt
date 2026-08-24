package top.role.android.feature.market

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

import coil3.compose.AsyncImage

import top.role.android.core.model.engage.MobileMarketCard
import top.role.android.core.model.market.MobileMarketListMode


@Composable
fun MarketListScreen(
    state:
    MarketListUiState,

    onModeChange:
        (MobileMarketListMode) -> Unit,

    onMarketClick:
        (String) -> Unit,

    onLoadMore:
        () -> Unit,

    onRetry:
        () -> Unit,
) {
    when {
        state.signedOut -> {
            MarketCenterMessage(
                text =
                    "登录后查看单品",
            )
        }

        state.loading &&
                state.items.isEmpty() -> {
            MarketLoadingContent()
        }

        state.errorMessage != null &&
                state.items.isEmpty() -> {
            MarketErrorContent(
                message =
                    state.errorMessage,

                onRetry =
                    onRetry,
            )
        }

        else -> {
            MarketListContent(
                state =
                    state,

                onModeChange =
                    onModeChange,

                onMarketClick =
                    onMarketClick,

                onLoadMore =
                    onLoadMore,

                onRetry =
                    onRetry,
            )
        }
    }
}

@Composable
private fun MarketListContent(
    state: MarketListUiState,
    onModeChange: (MobileMarketListMode) -> Unit,
    onMarketClick: (String) -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
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
                12.dp
            ),
    ) {

        item(
            key =
                "market-mode-tabs",
        ) {
            MarketModeTabs(
                selectedMode =
                    state.mode,

                onModeChange =
                    onModeChange,
            )
        }


        if (
            state.items.isEmpty()
        ) {

            item(
                key =
                    "market-empty",
            ) {
                Text(
                    text =
                        when (
                            state.mode
                        ) {

                            MobileMarketListMode.Featured ->
                                "暂无推荐单品"

                            MobileMarketListMode.Latest ->
                                "暂无最新单品"
                        },

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                vertical = 32.dp,
                            ),

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

                MarketListCard(
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


        if (
            !state.errorMessage
                .isNullOrBlank() &&
            state.items.isNotEmpty()
        ) {

            item(
                key =
                    "market-error-more",
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                vertical = 12.dp,
                            ),

                    horizontalAlignment =
                        Alignment.CenterHorizontally,
                ) {

                    Text(
                        text =
                            state.errorMessage,

                        color =
                            MaterialTheme
                                .colorScheme
                                .error,
                    )


                    Button(
                        modifier =
                            Modifier.padding(
                                top = 8.dp,
                            ),

                        onClick =
                            onRetry,
                    ) {

                        Text(
                            text =
                                "重试",
                        )
                    }
                }
            }
        }


        if (
            state.loadingMore
        ) {

            item(
                key =
                    "market-loading-more",
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                vertical = 20.dp,
                            ),

                    contentAlignment =
                        Alignment.Center,
                ) {

                    CircularProgressIndicator()
                }
            }

        } else if (
            !state.isDone &&
            state.continueCursor
                .isNotBlank()
        ) {

            item(
                key =
                    "market-load-more",
            ) {
                Button(
                    modifier =
                        Modifier.fillMaxWidth(),

                    onClick =
                        onLoadMore,
                ) {

                    Text(
                        text =
                            "加载更多",
                    )
                }
            }
        }
    }
}

@Composable
private fun MarketModeTabs(
    selectedMode:
    MobileMarketListMode,

    onModeChange:
        (MobileMarketListMode) -> Unit,
) {
    Row(
        horizontalArrangement =
            Arrangement.spacedBy(
                20.dp
            ),
    ) {
        MarketModeTab(
            text =
                "推荐",

            selected =
                selectedMode ==
                        MobileMarketListMode.Featured,

            onClick = {
                onModeChange(
                    MobileMarketListMode.Featured
                )
            },
        )

        MarketModeTab(
            text =
                "最新",

            selected =
                selectedMode ==
                        MobileMarketListMode.Latest,

            onClick = {
                onModeChange(
                    MobileMarketListMode.Latest
                )
            },
        )
    }
}


@Composable
private fun MarketModeTab(
    text:
    String,

    selected:
    Boolean,

    onClick:
        () -> Unit,
) {
    Text(
        text =
            text,

        modifier =
            Modifier
                .clickable(
                    onClick =
                        onClick,
                )
                .padding(
                    vertical =
                        8.dp,
                ),

        style =
            MaterialTheme
                .typography
                .titleSmall,

        fontWeight =
            if (
                selected
            ) {
                FontWeight.SemiBold
            } else {
                FontWeight.Normal
            },

        color =
            if (
                selected
            ) {
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
private fun MarketListCard(
    item: MobileMarketCard,
    onClick: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    onClick =
                        onClick,
                ),
    ) {

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        12.dp
                    ),

            horizontalArrangement =
                Arrangement.spacedBy(
                    14.dp
                ),
        ) {

            if (
                item.cover.isNotBlank()
            ) {
                AsyncImage(
                    model =
                        item.cover,

                    contentDescription =
                        item.title,

                    modifier =
                        Modifier
                            .size(
                                112.dp
                            ),

                    contentScale =
                        ContentScale.Crop,
                )
            }


            Column(
                modifier =
                    Modifier
                        .weight(
                            1f
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
                            .titleMedium,

                    fontWeight =
                        FontWeight.SemiBold,

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
                                .bodySmall,

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


                Text(
                    text =
                        item.price,

                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,

                    fontWeight =
                        FontWeight.SemiBold,

                    color =
                        MaterialTheme
                            .colorScheme
                            .primary,
                )


                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.SpaceBetween,
                ) {

                    Text(
                        text =
                            item.kindLabel,

                        style =
                            MaterialTheme
                                .typography
                                .labelSmall,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant,
                    )


                    if (
                        item.featured
                    ) {

                        Text(
                            text =
                                "推荐",

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

@Composable
private fun MarketLoadingContent() {
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
private fun MarketCenterMessage(
    text:
    String,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(
                    24.dp
                ),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center,
    ) {
        Text(
            text
        )
    }
}


@Composable
private fun MarketErrorContent(
    message:
    String,

    onRetry:
        () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(
                    24.dp
                ),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center,
    ) {
        Text(
            text =
                message,

            color =
                MaterialTheme
                    .colorScheme
                    .error,
        )

        Button(
            modifier =
                Modifier.padding(
                    top =
                        16.dp,
                ),

            onClick =
                onRetry,
        ) {
            Text(
                "重试"
            )
        }
    }
}