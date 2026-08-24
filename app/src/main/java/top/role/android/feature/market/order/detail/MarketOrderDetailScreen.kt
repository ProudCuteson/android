package top.role.android.feature.market.order.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar

import androidx.compose.runtime.Composable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

import coil3.compose.AsyncImage

import top.role.android.core.model.market.MobileMarketOrderDetail
import top.role.android.core.model.market.MobileMarketOrderFulfillment
import top.role.android.core.model.market.MobileMarketOrderShippingInfo

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun MarketOrderDetailScreen(
    state: MarketOrderDetailUiState,

    onBack: () -> Unit,
    onProductClick: (String) -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {

                    Column {

                        Text(
                            text =
                                "订单详情",

                            style =
                                MaterialTheme
                                    .typography
                                    .titleMedium,
                        )


                        state.order
                            ?.statusLabel
                            ?.takeIf {
                                it.isNotBlank()
                            }
                            ?.let { status ->

                                Text(
                                    text =
                                        status,

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

        when {

            state.isLoading &&
                    state.order == null -> {

                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(
                                innerPadding
                            ),

                    horizontalAlignment =
                        Alignment.CenterHorizontally,

                    verticalArrangement =
                        Arrangement.Center,
                ) {

                    CircularProgressIndicator()
                }
            }


            state.order != null -> {

                MarketOrderDetailContent(
                    order =
                        state.order,

                    modifier =
                        Modifier.padding(
                            innerPadding
                        ),

                    onProductClick =
                        onProductClick,
                )
            }


            else -> {

                MarketOrderDetailError(
                    message =
                        state.errorMessage
                            ?: "订单详情加载失败",

                    modifier =
                        Modifier.padding(
                            innerPadding
                        ),

                    onRetry =
                        onRetry,
                )
            }
        }
    }
}


@Composable
private fun MarketOrderDetailContent(
    order: MobileMarketOrderDetail,
    modifier: Modifier = Modifier,
    onProductClick: (String) -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(
                    rememberScrollState()
                )
                .navigationBarsPadding()
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp,
                ),

        verticalArrangement =
            Arrangement.spacedBy(
                14.dp
            ),
    ) {

        OrderProductCard(
            order =
                order,

            onClick = {
                onProductClick(
                    order.listingId
                )
            },
        )


        order.fulfillment
            ?.takeIf {
                it.hasDisplayContent()
            }
            ?.let { fulfillment ->

                OrderFulfillmentCard(
                    statusLabel =
                        order.statusLabel,

                    fulfillment =
                        fulfillment,
                )
            }


        order.shippingInfo
            ?.let { shippingInfo ->

                OrderShippingCard(
                    shippingInfo =
                        shippingInfo,
                )
            }


        OrderInfoCard(
            order =
                order,
        )


        order.buyerNote
            ?.takeIf {
                it.isNotBlank()
            }
            ?.let { buyerNote ->

                OrderSectionCard(
                    title =
                        "买家备注",
                ) {

                    Text(
                        text =
                            buyerNote,

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
            }


        order.sellerSpace
            ?.let { sellerSpace ->

                OrderSectionCard(
                    title =
                        "卖家信息",
                ) {

                    Text(
                        text =
                            sellerSpace.name,

                        style =
                            MaterialTheme
                                .typography
                                .titleSmall,

                        fontWeight =
                            FontWeight.Medium,
                    )


                    sellerSpace.number
                        ?.let { number ->

                            Text(
                                text =
                                    "专属号码 ${formatSpaceNumber(number)}",

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
                }
            }


        Text(
            text =
                "更多订单管理与售后功能，请前往 TopRole 网页版处理。",

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 8.dp,
                        vertical = 12.dp,
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


        Spacer(
            modifier =
                Modifier.height(
                    32.dp
                )
        )
    }
}


@Composable
private fun OrderProductCard(
    order: MobileMarketOrderDetail,
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

            if (
                !order.cover
                    .isNullOrBlank()
            ) {
                AsyncImage(
                    model =
                        order.cover,

                    contentDescription =
                        order.title,

                    modifier =
                        Modifier.size(
                            80.dp
                        ),

                    contentScale =
                        ContentScale.Crop,
                )
            } else {
                Surface(
                    modifier =
                        Modifier.size(
                            80.dp
                        ),

                    shape =
                        RoundedCornerShape(
                            10.dp
                        ),

                    color =
                        MaterialTheme
                            .colorScheme
                            .surfaceContainerHighest,
                ) {}
            }

            Column(
                modifier =
                    Modifier.weight(
                        1f
                    ),

                verticalArrangement =
                    Arrangement.spacedBy(
                        6.dp
                    ),
            ) {

                Text(
                    text =
                        order.title,

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


                Text(
                    text =
                        "数量 ${formatQuantity(order.quantity)}",

                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,
                )


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
                            order.totalPrice,

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


                    Text(
                        text =
                            "查看商品 ›",

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
        }
    }
}

private fun MobileMarketOrderFulfillment.hasDisplayContent(): Boolean {
    return !sellerMessage.isNullOrBlank() ||
            !deliveryTrackingCompany.isNullOrBlank() ||
            !deliveryTrackingNo.isNullOrBlank() ||
            !deliveryContent.isNullOrBlank()
}

@Composable
private fun OrderFulfillmentCard(
    statusLabel: String,
    fulfillment: MobileMarketOrderFulfillment,
) {
    OrderSectionCard(
        title =
            "履约信息",

        trailing = {

            if (
                statusLabel.isNotBlank()
            ) {

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
                            statusLabel,

                        modifier =
                            Modifier.padding(
                                horizontal = 9.dp,
                                vertical = 4.dp,
                            ),

                        style =
                            MaterialTheme
                                .typography
                                .labelSmall,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onPrimaryContainer,
                    )
                }
            }
        },
    ) {

        fulfillment.sellerMessage
            ?.takeIf {
                it.isNotBlank()
            }
            ?.let { message ->

                OrderDetailBlock(
                    label =
                        "卖家留言",

                    value =
                        message,
                )
            }


        fulfillment.deliveryTrackingCompany
            ?.takeIf {
                it.isNotBlank()
            }
            ?.let { company ->

                OrderDetailRow(
                    label =
                        "物流公司",

                    value =
                        company,
                )
            }


        fulfillment.deliveryTrackingNo
            ?.takeIf {
                it.isNotBlank()
            }
            ?.let { trackingNo ->

                OrderDetailRow(
                    label =
                        "物流单号",

                    value =
                        trackingNo,
                )
            }


        fulfillment.deliveryContent
            ?.takeIf {
                it.isNotBlank()
            }
            ?.let { content ->

                OrderDetailBlock(
                    label =
                        "交付内容",

                    value =
                        content,
                )
            }
    }
}


@Composable
private fun OrderShippingCard(
    shippingInfo: MobileMarketOrderShippingInfo,
) {
    OrderSectionCard(
        title =
            "收货信息",
    ) {

        Row(
            horizontalArrangement =
                Arrangement.spacedBy(
                    12.dp
                ),
        ) {

            Text(
                text =
                    shippingInfo.receiverName,

                style =
                    MaterialTheme
                        .typography
                        .titleSmall,

                fontWeight =
                    FontWeight.Medium,
            )


            Text(
                text =
                    shippingInfo.receiverPhone,

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


        Text(
            text =
                buildString {

                    append(
                        shippingInfo.regionText
                    )

                    if (
                        shippingInfo.regionText.isNotBlank() &&
                        shippingInfo.addressLine1.isNotBlank()
                    ) {
                        append(" ")
                    }

                    append(
                        shippingInfo.addressLine1
                    )
                },

            style =
                MaterialTheme
                    .typography
                    .bodyMedium,
        )


        shippingInfo.postalCode
            ?.takeIf {
                it.isNotBlank()
            }
            ?.let { postalCode ->

                Text(
                    text =
                        "邮编 $postalCode",

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
    }
}


@Composable
private fun OrderInfoCard(
    order: MobileMarketOrderDetail,
) {
    OrderSectionCard(
        title =
            "订单信息",
    ) {

        order.outTradeNo
            ?.takeIf {
                it.isNotBlank()
            }
            ?.let { outTradeNo ->

                OrderDetailRow(
                    label =
                        "订单号",

                    value =
                        outTradeNo,
                )
            }


        OrderDetailRow(
            label =
                "下单时间",

            value =
                formatOrderDateTime(
                    order.createdAt
                ),
        )


        order.paidAt
            ?.takeIf {
                it > 0.0
            }
            ?.let { paidAt ->

                OrderDetailRow(
                    label =
                        "支付时间",

                    value =
                        formatOrderDateTime(
                            paidAt
                        ),
                )
            }


        OrderDetailRow(
            label =
                "实付金额",

            value =
                order.totalPrice,

            valueEmphasis =
                true,
        )
    }
}


@Composable
private fun OrderSectionCard(
    title: String,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Card(
        modifier =
            Modifier.fillMaxWidth(),

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

        Column(
            modifier =
                Modifier.padding(
                    16.dp
                ),

            verticalArrangement =
                Arrangement.spacedBy(
                    12.dp
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
                        title,

                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,

                    fontWeight =
                        FontWeight.SemiBold,
                )


                trailing
                    ?.invoke()
            }


            content()
        }
    }
}

@Composable
private fun OrderDetailRow(
    label: String,
    value: String,
    valueEmphasis: Boolean = false,
) {
    Row(
        modifier =
            Modifier.fillMaxWidth(),

        verticalAlignment =
            Alignment.Top,
    ) {

        Text(
            text =
                label,

            modifier =
                Modifier.weight(
                    0.32f
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


        Text(
            text =
                value,

            modifier =
                Modifier
                    .weight(
                        0.68f
                    )
                    .padding(
                        start = 16.dp
                    ),

            style =
                MaterialTheme
                    .typography
                    .bodyMedium,

            fontWeight =
                if (
                    valueEmphasis
                ) {
                    FontWeight.SemiBold
                } else {
                    FontWeight.Normal
                },

            color =
                if (
                    valueEmphasis
                ) {
                    MaterialTheme
                        .colorScheme
                        .primary
                } else {
                    MaterialTheme
                        .colorScheme
                        .onSurface
                },
        )
    }
}

@Composable
private fun OrderDetailBlock(
    label: String,
    value: String,
) {
    Column(
        verticalArrangement =
            Arrangement.spacedBy(
                5.dp
            ),
    ) {

        Text(
            text =
                label,

            style =
                MaterialTheme
                    .typography
                    .labelMedium,

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant,
        )


        Text(
            text =
                value,

            style =
                MaterialTheme
                    .typography
                    .bodyMedium,
        )
    }
}


@Composable
private fun MarketOrderDetailError(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit,
) {
    Column(
        modifier =
            modifier
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


        TextButton(
            modifier =
                Modifier.padding(
                    top = 8.dp
                ),

            onClick =
                onRetry,
        ) {

            Text(
                text =
                    "重新加载",
            )
        }
    }
}


private fun formatQuantity(
    value: Double,
): String {

    return if (
        value % 1.0 ==
        0.0
    ) {
        value
            .toInt()
            .toString()
    } else {
        value.toString()
    }
}


private fun formatOrderDateTime(
    value: Double,
): String {

    if (
        value <=
        0.0
    ) {
        return "-"
    }

    return SimpleDateFormat(
        "yyyy-MM-dd HH:mm",
        Locale.getDefault(),
    ).format(
        Date(
            value.toLong()
        )
    )
}


private fun formatSpaceNumber(
    value: Double,
): String {

    return if (
        value % 1.0 ==
        0.0
    ) {
        value
            .toLong()
            .toString()
    } else {
        value.toString()
    }
}