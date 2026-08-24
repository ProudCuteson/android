package top.role.android.feature.market.order.detail

import top.role.android.core.model.market.MobileMarketOrderDetail


data class MarketOrderDetailUiState(
    val isLoading: Boolean = true,

    val order: MobileMarketOrderDetail? = null,

    val errorMessage: String? = null,
)