package top.role.android.feature.market.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import coil3.compose.AsyncImage

import top.role.android.core.model.market.MobileMarketDetail


@Composable
fun MarketDetailScreen(
    state:
    MarketDetailUiState,

    onRetry:
        () -> Unit,

    onBuy:
        () -> Unit,
) {
    when {
        state.signedOut -> {
            MarketDetailCenterMessage(
                text =
                    "登录后查看单品",
            )
        }

        state.loading &&
                state.listing == null -> {
            MarketDetailLoading()
        }

        state.listing != null -> {
            MarketDetailContent(
                listing =
                    state.listing,

                onBuy =
                    onBuy,
            )
        }

        else -> {
            MarketDetailError(
                message =
                    state.errorMessage
                        ?: "单品不可用",

                onRetry =
                    onRetry,
            )
        }
    }
}


@Composable
private fun MarketDetailContent(
    listing:
    MobileMarketDetail,

    onBuy:
        () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(
                    rememberScrollState()
                ),

        verticalArrangement =
            Arrangement.spacedBy(
                16.dp
            ),
    ) {

        if (
            listing.cover
                .isNotBlank()
        ) {
            AsyncImage(
                model =
                    listing.cover,

                contentDescription =
                    listing.title,

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(
                            1f
                        ),

                contentScale =
                    ContentScale.Crop,
            )
        }

        Column(
            modifier =
                Modifier.padding(
                    horizontal =
                        16.dp,
                ),

            verticalArrangement =
                Arrangement.spacedBy(
                    12.dp
                ),
        ) {

            Text(
                text =
                    listing.title,

                style =
                    MaterialTheme
                        .typography
                        .headlineSmall,

                fontWeight =
                    FontWeight.SemiBold,
            )

            if (
                listing.subtitle
                    .isNotBlank()
            ) {
                Text(
                    text =
                        listing.subtitle,

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

            Text(
                text =
                    listing.price,

                style =
                    MaterialTheme
                        .typography
                        .headlineSmall,

                fontWeight =
                    FontWeight.Bold,

                color =
                    MaterialTheme
                        .colorScheme
                        .primary,
            )

            if (
                listing.feeEnabled &&
                listing.feeText
                    .isNotBlank()
            ) {
                Text(
                    text =
                        listing.feeText,

                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,
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
                        listing.kindLabel,

                    style =
                        MaterialTheme
                            .typography
                            .labelMedium,
                )

                Text(
                    text =
                        marketStockLabel(
                            listing =
                                listing,
                        ),

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

            HorizontalDivider()

            if (
                listing.description
                    .isNotBlank()
            ) {
                Text(
                    text =
                        "商品说明",

                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,

                    fontWeight =
                        FontWeight.Medium,
                )

                Text(
                    text =
                        listing.description,

                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,
                )
            }

            if (
                listing.needsShippingAddress ||
                listing.allowMultiQuantity
            ) {
                HorizontalDivider()

                Text(
                    text =
                        "购买信息",

                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,

                    fontWeight =
                        FontWeight.Medium,
                )

                if (
                    listing.allowMultiQuantity
                ) {
                    Text(
                        text =
                            "支持购买多个数量",

                        style =
                            MaterialTheme
                                .typography
                                .bodyMedium,
                    )
                }

                if (
                    listing.needsShippingAddress
                ) {
                    Text(
                        text =
                            "购买时需要填写收货地址",

                        style =
                            MaterialTheme
                                .typography
                                .bodyMedium,
                    )
                }
            }

            HorizontalDivider()

            Text(
                text =
                    "卖家空间",

                style =
                    MaterialTheme
                        .typography
                        .titleMedium,

                fontWeight =
                    FontWeight.Medium,
            )

            Text(
                text =
                    listing
                        .sellerSpace
                        .name,

                style =
                    MaterialTheme
                        .typography
                        .titleSmall,
            )

            listing
                .sellerSpace
                .description
                ?.takeIf {
                    it.isNotBlank()
                }
                ?.let {
                    Text(
                        text =
                            it,

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

            Button(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            bottom =
                                24.dp,
                        ),

                onClick =
                    onBuy,
            ) {
                Text(
                    text =
                        "购买"
                )
            }
        }
    }
}


private fun marketStockLabel(
    listing:
    MobileMarketDetail,
): String {

    if (
        listing.stockMode ==
        "unlimited"
    ) {
        return "库存充足"
    }

    val count =
        listing.stockCount
            ?: 0.0

    return if (
        count <= 0.0
    ) {
        "已售罄"
    } else {
        "库存 ${count.toInt()}"
    }
}


@Composable
private fun MarketDetailLoading() {
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
private fun MarketDetailCenterMessage(
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
private fun MarketDetailError(
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