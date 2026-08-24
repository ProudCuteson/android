package top.role.android.feature.market.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height

import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import coil3.compose.AsyncImage

import top.role.android.core.model.region.ChinaRegionNode
import top.role.android.core.model.region.MarketRegionSelection
import top.role.android.core.region.ChinaRegionRepository
import top.role.android.feature.market.detail.components.MarketPurchaseSection
import top.role.android.feature.market.detail.components.MarketRegionPicker


@Composable
fun MarketDetailScreen(
    state:
    MarketDetailUiState,

    onRetry:
        () -> Unit,

    onDecreaseQuantity:
        () -> Unit,

    onIncreaseQuantity:
        () -> Unit,

    onReceiverNameChange:
        (String) -> Unit,

    onReceiverPhoneChange:
        (String) -> Unit,

    onAddressLine1Change:
        (String) -> Unit,

    onPostalCodeChange:
        (String) -> Unit,

    onBuyerNoteChange:
        (String) -> Unit,

    onRegionSelected:
        (MarketRegionSelection) -> Unit,

    onBuy:
        () -> Unit,
) {

    /*
     * ------------------------------------------------
     * Region picker
     * ------------------------------------------------
     *
     * Picker 是否打开属于纯 UI state，
     * 不进入 ViewModel。
     */

    var showRegionPicker by
    rememberSaveable {
        mutableStateOf(
            false
        )
    }


    /*
     * pca.json 是纯静态 Android asset。
     *
     * 这里通过 Repository 读取即可，
     * 不需要为了地区数据继续扩大
     * MarketDetailViewModel / AppContainer 边界。
     */

    val context =
        LocalContext.current

    val regionRepository =
        remember(
            context
        ) {
            ChinaRegionRepository(
                context =
                    context,
            )
        }


    val regions by
    produceState<
            List<ChinaRegionNode>
            >(
        initialValue =
            emptyList(),

        key1 =
            regionRepository,
    ) {

        value =
            runCatching {
                regionRepository
                    .getRegions()
            }
                .getOrElse {
                    emptyList()
                }
    }


    /*
     * ------------------------------------------------
     * Page
     * ------------------------------------------------
     */

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
                state =
                    state,

                onDecreaseQuantity =
                    onDecreaseQuantity,

                onIncreaseQuantity =
                    onIncreaseQuantity,

                onRegionClick = {

                    /*
                     * 正常情况下 pca.json 很快已经读取完。
                     *
                     * 如果数据尚未完成读取，
                     * 暂时不打开空 Picker。
                     */
                    if (
                        regions.isNotEmpty()
                    ) {
                        showRegionPicker =
                            true
                    }
                },

                onReceiverNameChange =
                    onReceiverNameChange,

                onReceiverPhoneChange =
                    onReceiverPhoneChange,

                onAddressLine1Change =
                    onAddressLine1Change,

                onPostalCodeChange =
                    onPostalCodeChange,

                onBuyerNoteChange =
                    onBuyerNoteChange,

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


    /*
     * ------------------------------------------------
     * Region Picker
     * ------------------------------------------------
     */

    if (
        showRegionPicker &&
        regions.isNotEmpty()
    ) {

        MarketRegionPicker(
            regions =
                regions,

            onDismiss = {
                showRegionPicker =
                    false
            },

            onSelected = {
                    region ->

                /*
                 * 最终选择结果交给 ViewModel。
                 */

                onRegionSelected(
                    region
                )

                showRegionPicker =
                    false
            },
        )
    }
}


@Composable
private fun MarketDetailContent(
    state:
    MarketDetailUiState,

    onDecreaseQuantity:
        () -> Unit,

    onIncreaseQuantity:
        () -> Unit,

    onRegionClick:
        () -> Unit,

    onReceiverNameChange:
        (String) -> Unit,

    onReceiverPhoneChange:
        (String) -> Unit,

    onAddressLine1Change:
        (String) -> Unit,

    onPostalCodeChange:
        (String) -> Unit,

    onBuyerNoteChange:
        (String) -> Unit,

    onBuy:
        () -> Unit,
) {

    val listing =
        state.listing
            ?: return


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

        /*
         * ------------------------------------------------
         * Cover
         * ------------------------------------------------
         */

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
                            16f / 10f
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

            /*
             * ------------------------------------------------
             * Product
             * ------------------------------------------------
             */

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


            /*
             * ------------------------------------------------
             * Description
             * ------------------------------------------------
             */

            if (
                listing.description
                    .isNotBlank()
            ) {

                HorizontalDivider()


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


            /*
             * ------------------------------------------------
             * Purchase
             * ------------------------------------------------
             */

            HorizontalDivider()


            MarketPurchaseSection(
                listing =
                    listing,

                quantity =
                    state.quantity,

                selectedRegion =
                    state.selectedRegion,

                receiverName =
                    state.receiverName,

                receiverPhone =
                    state.receiverPhone,

                addressLine1 =
                    state.addressLine1,

                postalCode =
                    state.postalCode,

                buyerNote =
                    state.buyerNote,

                enabled =
                    !state.paying &&
                            !state.confirming,

                onDecreaseQuantity =
                    onDecreaseQuantity,

                onIncreaseQuantity =
                    onIncreaseQuantity,

                onRegionClick =
                    onRegionClick,

                onReceiverNameChange =
                    onReceiverNameChange,

                onReceiverPhoneChange =
                    onReceiverPhoneChange,

                onAddressLine1Change =
                    onAddressLine1Change,

                onPostalCodeChange =
                    onPostalCodeChange,

                onBuyerNoteChange =
                    onBuyerNoteChange,
            )


            /*
             * ------------------------------------------------
             * Seller
             * ------------------------------------------------
             */

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
                ?.let { description ->

                    Text(
                        text =
                            description,

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


            /*
             * ------------------------------------------------
             * Payment message
             * ------------------------------------------------
             */

            state.paymentError
                ?.let { error ->

                    Text(
                        text =
                            resolveMarketPaymentError(
                                error
                            ),

                        style =
                            MaterialTheme
                                .typography
                                .bodySmall,

                        color =
                            if (
                                error ==
                                "MARKET_PAYMENT_CONFIRMING"
                            ) {
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                            } else {
                                MaterialTheme
                                    .colorScheme
                                    .error
                            },
                    )
                }


            /*
             * ------------------------------------------------
             * Buy
             * ------------------------------------------------
             */

            Button(
                modifier =
                    Modifier.fillMaxWidth(),

                enabled =
                    !state.paying &&
                            !state.confirming &&
                            marketCanPurchase(
                                state =
                                    state,
                            ),

                onClick =
                    onBuy,
            ) {

                if (
                    state.paying ||
                    state.confirming
                ) {

                    CircularProgressIndicator(
                        modifier =
                            Modifier
                                .padding(
                                    end =
                                        8.dp,
                                )
                                .size(
                                    18.dp
                                ),

                        strokeWidth =
                            2.dp,
                    )
                }


                Text(
                    text =
                        when {

                            state.paying ->
                                "正在发起支付"


                            state.confirming ->
                                "正在确认支付"


                            else ->
                                "购买"
                        }
                )
            }

            Spacer(
                modifier =
                    Modifier.height(
                        48.dp
                    )
            )

        }
    }
}


/*
 * ------------------------------------------------
 * Buy validation
 * ------------------------------------------------
 */

private fun marketCanPurchase(
    state:
    MarketDetailUiState,
): Boolean {

    val listing =
        state.listing
            ?: return false


    /*
     * Sold out
     */

    if (
        listing.stockMode !=
        "unlimited"
    ) {

        val stockCount =
            listing.stockCount
                ?: 0.0

        if (
            stockCount <=
            0.0
        ) {
            return false
        }
    }


    /*
     * Quantity
     */

    if (
        state.quantity <
        1.0
    ) {
        return false
    }


    if (
        listing.stockMode !=
        "unlimited"
    ) {

        val stockCount =
            listing.stockCount

        if (
            stockCount !=
            null &&
            state.quantity >
            stockCount
        ) {
            return false
        }
    }


    /*
     * Shipping
     */

    if (
        listing.needsShippingAddress
    ) {

        if (
            state.receiverName
                .isBlank()
        ) {
            return false
        }


        if (
            state.receiverPhone
                .isBlank()
        ) {
            return false
        }


        if (
            state.selectedRegion ==
            null
        ) {
            return false
        }


        if (
            state.addressLine1
                .isBlank()
        ) {
            return false
        }
    }


    return true
}


/*
 * ------------------------------------------------
 * Stock
 * ------------------------------------------------
 */

private fun marketStockLabel(
    listing:
    top.role.android.core.model.market.MobileMarketDetail,
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
        count <=
        0.0
    ) {
        "已售罄"
    } else {
        "库存 ${count.toInt()}"
    }
}


/*
 * ------------------------------------------------
 * Loading
 * ------------------------------------------------
 */

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


/*
 * ------------------------------------------------
 * Center Message
 * ------------------------------------------------
 */

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
            text =
                text,
        )
    }
}


/*
 * ------------------------------------------------
 * Error
 * ------------------------------------------------
 */

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
                text =
                    "重试",
            )
        }
    }
}


/*
 * ------------------------------------------------
 * Payment error
 * ------------------------------------------------
 */

private fun resolveMarketPaymentError(
    code:
    String,
): String {

    return when (
        code
    ) {

        "SHIPPING_ADDRESS_REQUIRED" ->
            "请完整填写收货信息"


        "INVALID_MARKET_QUANTITY" ->
            "购买数量不正确"


        "MARKET_STOCK_NOT_ENOUGH" ->
            "商品库存不足"


        "MARKET_PAYMENT_CONFIRMING" ->
            "支付结果正在确认，可稍后在“我的购买”中查看订单状态"


        "WECHAT_PAY_LAUNCH_FAILED" ->
            "无法拉起微信支付"


        "WECHAT_PAY_FAILED" ->
            "微信支付未完成"


        "CREATE_MARKET_PURCHASE_FAILED" ->
            "创建订单失败"


        "MARKET_ORDER_CANCELLED" ->
            "订单已取消"


        "MARKET_ORDER_REFUNDED" ->
            "订单已退款"


        else ->
            code
    }
}