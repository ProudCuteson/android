package top.role.android.feature.market.order

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import top.role.android.core.model.market.MobileMarketOrderCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketOrderListScreen(
    state: MarketOrderListUiState,
    onBack: () -> Unit,
    onOrderClick: (MobileMarketOrderCard) -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "我的购买",
                            style =
                                MaterialTheme
                                    .typography
                                    .titleMedium,
                        )

                        Text(
                            text =
                                "查看订单与履约状态",
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
                },

                navigationIcon = {
                    IconButton(
                        onClick =
                            onBack,
                    ) {
                        Icon(
                            imageVector =
                                Icons
                                    .AutoMirrored
                                    .Filled
                                    .ArrowBack,

                            contentDescription =
                                "返回",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    innerPadding
                ),
        ) {
            when {
                state.isLoading &&
                        state.items.isEmpty() -> {

                    CircularProgressIndicator(
                        modifier =
                            Modifier.align(
                                Alignment.Center
                            ),
                    )
                }

                state.errorMessage != null &&
                        state.items.isEmpty() -> {

                    Column(
                        modifier =
                            Modifier
                                .align(
                                    Alignment.Center
                                )
                                .padding(24.dp),

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

                        Text(
                            text =
                                "点击重试",

                            modifier =
                                Modifier
                                    .padding(
                                        top = 12.dp,
                                    )
                                    .clickable(
                                        onClick =
                                            onRetry,
                                    ),

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .primary,
                        )
                    }
                }

                state.items.isEmpty() -> {

                    Text(
                        text =
                            "暂无购买记录",

                        modifier =
                            Modifier.align(
                                Alignment.Center
                            ),

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant,
                    )
                }

                else -> {

                    LazyColumn(
                        modifier =
                            Modifier.fillMaxSize(),

                        contentPadding =
                            androidx.compose.foundation.layout.PaddingValues(
                                horizontal = 16.dp,
                                vertical = 12.dp,
                            ),

                        verticalArrangement =
                            Arrangement.spacedBy(
                                10.dp
                            ),
                    ) {
                        items(
                            items =
                                state.items,

                            key = {
                                it.id
                            },
                        ) { item ->

                            MarketOrderCard(
                                item =
                                    item,

                                onClick = {
                                    onOrderClick(
                                        item
                                    )
                                },
                            )
                        }

                        if (
                            state.isLoadingMore
                        ) {
                            item(
                                key =
                                    "market-orders-loading"
                            ) {
                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                20.dp
                                            ),

                                    contentAlignment =
                                        Alignment.Center,
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }

                        if (
                            !state.isLoadingMore
                        ) {
                            item(
                                key =
                                    "market-orders-more"
                            ) {
                                Text(
                                    text =
                                        "加载更多",

                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .clickable(
                                                onClick =
                                                    onLoadMore,
                                            )
                                            .padding(
                                                16.dp
                                            ),

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
    }
}

@Composable
private fun MarketOrderCard(
    item: MobileMarketOrderCard,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick,
            ),

        shape =
            RoundedCornerShape(
                16.dp
            ),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme
                        .colorScheme
                        .surfaceContainerLow,
            ),
    ) {
        Row(
            modifier =
                Modifier.padding(
                    14.dp
                ),

            horizontalArrangement =
                Arrangement.spacedBy(
                    12.dp
                ),
        ) {
            AsyncImage(
                model =
                    item.cover,

                contentDescription =
                    item.title,

                modifier =
                    Modifier.size(
                        76.dp
                    ),
            )

            Column(
                modifier =
                    Modifier.weight(
                        1f
                    ),
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
                            resolveOrderStatusLabel(
                                item.status
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

                    Text(
                        text =
                            formatOrderTime(
                                item.createdAt
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

                Text(
                    text =
                        item.title,

                    modifier =
                        Modifier.padding(
                            top = 5.dp,
                        ),

                    style =
                        MaterialTheme
                            .typography
                            .titleSmall,

                    fontWeight =
                        FontWeight.SemiBold,

                    maxLines =
                        2,

                    overflow =
                        TextOverflow.Ellipsis,
                )

                Text(
                    text =
                        "数量 ${formatQuantity(item.quantity)}",

                    modifier =
                        Modifier.padding(
                            top = 5.dp,
                        ),

                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,
                )

                Text(
                    text =
                        formatPrice(
                            cents =
                                item.totalPriceCents,

                            currency =
                                item.currency,
                        ),

                    modifier =
                        Modifier.padding(
                            top = 8.dp,
                        ),

                    style =
                        MaterialTheme
                            .typography
                            .titleSmall,

                    fontWeight =
                        FontWeight.SemiBold,
                )
            }
        }
    }
}

private fun resolveOrderStatusLabel(
    status: String,
): String {
    return when (
        status
    ) {
        "pending" ->
            "待支付"

        "paid",
        "settled" ->
            "已支付"

        "canceled",
        "cancelled" ->
            "已取消"

        "failed" ->
            "支付失败"

        else ->
            status.ifBlank {
                "处理中"
            }
    }
}

private fun formatQuantity(
    value: Double,
): String {
    return if (
        value % 1.0 == 0.0
    ) {
        value
            .toInt()
            .toString()
    } else {
        value.toString()
    }
}

private fun formatPrice(
    cents: Double,
    currency: String,
): String {
    val amount =
        cents / 100.0

    return when (
        currency.uppercase()
    ) {
        "CNY" ->
            "¥%.2f".format(
                amount
            )

        "USD" ->
            "$%.2f".format(
                amount
            )

        else ->
            "%.2f %s".format(
                amount,
                currency,
            )
    }
}

private fun formatOrderTime(
    value: Double,
): String {
    if (
        value <= 0
    ) {
        return ""
    }

    return SimpleDateFormat(
        "MM-dd HH:mm",
        Locale.getDefault(),
    ).format(
        Date(
            value.toLong()
        )
    )
}