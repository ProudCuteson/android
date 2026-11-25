package top.role.android.feature.market.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import coil3.compose.AsyncImage

import top.role.android.core.auth.AuthState
import top.role.android.core.model.market.MobileMarketDetail
import top.role.android.core.model.region.ChinaRegionNode
import top.role.android.core.model.region.MarketRegionSelection
import top.role.android.core.payment.wechatPayDisplayMessage
import top.role.android.core.region.ChinaRegionRepository
import top.role.android.feature.market.detail.components.MarketPurchaseSection
import top.role.android.feature.market.detail.components.MarketRegionPicker
import top.role.android.ui.auth.AuthStatusContent
import top.role.android.ui.components.RemoteMessageState

@Composable
fun MarketDetailScreen(
    viewModel: MarketDetailViewModel,
    authState: AuthState,
    remoteOnline: Boolean,
    onLogin: () -> Unit,
    onRetryAuth: () -> Unit,
    onReSignIn: () -> Unit,
    onRetry: () -> Unit,
    onDecreaseQuantity: () -> Unit,
    onIncreaseQuantity: () -> Unit,
    onReceiverNameChange: (String) -> Unit,
    onReceiverPhoneChange: (String) -> Unit,
    onAddressLine1Change: (String) -> Unit,
    onPostalCodeChange: (String) -> Unit,
    onBuyerNoteChange: (String) -> Unit,
    onRegionSelected: (MarketRegionSelection) -> Unit,
    onBuy: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var showRegionPicker by rememberSaveable {
        mutableStateOf(false)
    }

    val context = LocalContext.current

    val regionRepository = remember(context) {
        ChinaRegionRepository(context)
    }

    val regions by produceState<List<ChinaRegionNode>>(
        initialValue = emptyList(),
        key1 = regionRepository,
    ) {
        value = runCatching {
            regionRepository.getRegions()
        }.getOrElse {
            emptyList()
        }
    }

    AuthStatusContent(
        authState = authState,
        onLogin = onLogin,
        onRetry = onRetryAuth,
        onReSignIn = onReSignIn,
    ) {
        when {
            !remoteOnline -> {
                RemoteMessageState(
                    title = "当前处于离线状态",
                    description = "连接网络后将自动恢复单品浏览。",
                )
            }

            state.loading && state.listing == null -> {
                MarketDetailLoading()
            }

            state.listing != null -> {
                MarketDetailContent(
                    state = state,
                    onDecreaseQuantity = onDecreaseQuantity,
                    onIncreaseQuantity = onIncreaseQuantity,
                    onRegionClick = {
                        if (regions.isNotEmpty()) {
                            showRegionPicker = true
                        }
                    },
                    onReceiverNameChange = onReceiverNameChange,
                    onReceiverPhoneChange = onReceiverPhoneChange,
                    onAddressLine1Change = onAddressLine1Change,
                    onPostalCodeChange = onPostalCodeChange,
                    onBuyerNoteChange = onBuyerNoteChange,
                    onBuy = onBuy,
                )
            }

            else -> {
                MarketDetailError(
                    message = state.errorMessage ?: "单品不可用",
                    onRetry = onRetry,
                )
            }
        }

        // 行政区划选择
        if (
            remoteOnline &&
            state.listing != null &&
            showRegionPicker &&
            regions.isNotEmpty()
        ) {
            MarketRegionPicker(
                regions = regions,
                onDismiss = { showRegionPicker = false },
                onSelected = { region ->
                    onRegionSelected(region)
                    showRegionPicker = false
                },
            )
        }
    }
}

// Main Content

@Composable
private fun MarketDetailContent(
    state: MarketDetailUiState,
    onDecreaseQuantity: () -> Unit,
    onIncreaseQuantity: () -> Unit,
    onRegionClick: () -> Unit,
    onReceiverNameChange: (String) -> Unit,
    onReceiverPhoneChange: (String) -> Unit,
    onAddressLine1Change: (String) -> Unit,
    onPostalCodeChange: (String) -> Unit,
    onBuyerNoteChange: (String) -> Unit,
    onBuy: () -> Unit,
) {
    val listing = state.listing ?: return

    val purchaseInProgress = state.paying || state.confirming

    val purchaseEnabled =
        !purchaseInProgress && marketCanPurchase(state)

    Column(modifier = Modifier.fillMaxSize()) {

        // 商品详情及购买信息滚动区域。
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            // 商品封面
            if (listing.cover.isNotBlank()) {
                AsyncImage(
                    model = listing.cover,
                    contentDescription = listing.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 10f),
                    contentScale = ContentScale.Crop,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                MarketProductInfo(listing)

                if (listing.description.isNotBlank()) {
                    HorizontalDivider()

                    MarketProductDescription(
                        description = listing.description,
                    )
                }

                HorizontalDivider()

                // 购买数量、收货信息及买家备注。
                MarketPurchaseSection(
                    listing = listing,
                    quantity = state.quantity,
                    selectedRegion = state.selectedRegion,
                    receiverName = state.receiverName,
                    receiverPhone = state.receiverPhone,
                    addressLine1 = state.addressLine1,
                    postalCode = state.postalCode,
                    buyerNote = state.buyerNote,
                    enabled = !purchaseInProgress,
                    onDecreaseQuantity = onDecreaseQuantity,
                    onIncreaseQuantity = onIncreaseQuantity,
                    onRegionClick = onRegionClick,
                    onReceiverNameChange = onReceiverNameChange,
                    onReceiverPhoneChange = onReceiverPhoneChange,
                    onAddressLine1Change = onAddressLine1Change,
                    onPostalCodeChange = onPostalCodeChange,
                    onBuyerNoteChange = onBuyerNoteChange,
                )

                HorizontalDivider()

                MarketSellerInfo(listing)
            }
        }

        // 固定底部购买区域。
        MarketPurchaseBar(
            state = state,
            purchaseEnabled = purchaseEnabled,
            onBuy = onBuy,
        )
    }
}

// Product Information

@Composable
private fun MarketProductInfo(listing: MobileMarketDetail) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = listing.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )

        if (listing.subtitle.isNotBlank()) {
            Text(
                text = listing.subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Text(
            text = listing.price,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )

        if (listing.feeEnabled && listing.feeText.isNotBlank()) {
            Text(
                text = listing.feeText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MarketProductLabel(
                text = listing.kindLabel,
            )

            MarketProductLabel(
                text = marketStockLabel(listing),
            )
        }
    }
}

@Composable
private fun MarketProductLabel(text: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 5.dp,
            ),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MarketProductDescription(description: String) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "商品说明",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

// Seller Information

@Composable
private fun MarketSellerInfo(listing: MobileMarketDetail) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "卖家空间",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = 1.dp,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = listing.sellerSpace.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )

                listing.sellerSpace.description
                    ?.takeIf { it.isNotBlank() }
                    ?.let { description ->
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
            }
        }
    }
}

// Purchase Bar

@Composable
private fun MarketPurchaseBar(
    state: MarketDetailUiState,
    purchaseEnabled: Boolean,
    onBuy: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            state.paymentError
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?.let { error ->
                    MarketPaymentNotice(error)
                }

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = purchaseEnabled,
                onClick = onBuy,
            ) {
                if (state.paying || state.confirming) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(
                    text = when {
                        state.confirming -> "正在确认支付…"
                        state.paying -> "正在发起支付…"
                        else -> "购买"
                    },
                )
            }
        }
    }
}

@Composable
private fun MarketPaymentNotice(error: String) {
    val isConfirming = error == "MARKET_PAYMENT_CONFIRMING"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = if (isConfirming) {
            MaterialTheme.colorScheme.surfaceVariant
        } else {
            MaterialTheme.colorScheme.errorContainer
        },
    ) {
        Text(
            text = resolveMarketPaymentError(error),
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isConfirming) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onErrorContainer
            },
        )
    }
}

// Purchase Validation

private fun marketCanPurchase(state: MarketDetailUiState): Boolean {
    val listing = state.listing ?: return false

    // 库存限制
    if (listing.stockMode != "unlimited") {
        val stockCount = listing.stockCount ?: 0.0

        if (stockCount <= 0.0) return false
    }

    // 购买数量
    if (state.quantity < 1.0) return false

    if (listing.stockMode != "unlimited") {
        val stockCount = listing.stockCount

        if (stockCount != null && state.quantity > stockCount) {
            return false
        }
    }

    // 实物商品收货信息
    if (listing.needsShippingAddress) {
        if (state.receiverName.isBlank()) return false
        if (state.receiverPhone.isBlank()) return false
        if (state.selectedRegion == null) return false
        if (state.addressLine1.isBlank()) return false
    }

    return true
}

private fun marketStockLabel(listing: MobileMarketDetail): String {
    if (listing.stockMode == "unlimited") {
        return "库存充足"
    }

    val count = listing.stockCount ?: 0.0

    return if (count <= 0.0) {
        "已售罄"
    } else {
        "库存 ${count.toInt()}"
    }
}

// Loading / Error

@Composable
private fun MarketDetailLoading() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "正在加载商品信息…",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MarketDetailError(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            12.dp,
            Alignment.CenterVertically,
        ),
    ) {
        Text(
            text = "暂时无法加载商品",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        OutlinedButton(onClick = onRetry) {
            Text("重试")
        }
    }
}

// Payment Error Messages

private fun resolveMarketPaymentError(code: String): String {
    return when (code.trim()) {
        "SHIPPING_ADDRESS_REQUIRED" ->
            "请完整填写收货信息。"

        "INVALID_MARKET_QUANTITY" ->
            "购买数量不正确。"

        "MARKET_STOCK_NOT_ENOUGH" ->
            "商品库存不足。"

        "MARKET_PAYMENT_CONFIRMING" ->
            "支付结果正在确认，可稍后在「我的购买」中查看订单状态。"

        "CREATE_MARKET_PURCHASE_FAILED" ->
            "暂时无法创建订单，请稍后重试。"

        "MARKET_ORDER_CANCELLED" ->
            "该订单已取消。"

        "MARKET_ORDER_REFUNDED" ->
            "该订单已退款。"

        else -> wechatPayDisplayMessage(code)
    }
}