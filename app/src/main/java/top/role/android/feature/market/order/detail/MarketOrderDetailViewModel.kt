package top.role.android.feature.market.order.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class MarketOrderDetailViewModel(
    private val orderId: String,
    private val repository: MarketOrderDetailRepository,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            MarketOrderDetailUiState()
        )

    val uiState:
            StateFlow<MarketOrderDetailUiState> =
        _uiState.asStateFlow()


    private var observeJob: Job? =
        null


    fun start() {

        if (
            observeJob != null
        ) {
            return
        }

        observeOrder()
    }


    fun retry() {
        observeOrder()
    }


    fun showSignedOut() {

        observeJob
            ?.cancel()

        observeJob =
            null

        _uiState.value =
            MarketOrderDetailUiState(
                isLoading =
                    false,

                errorMessage =
                    "登录后查看订单详情",
            )
    }


    private fun observeOrder() {

        observeJob
            ?.cancel()

        _uiState.value =
            _uiState.value.copy(
                isLoading =
                    true,

                errorMessage =
                    null,
            )

        observeJob =
            viewModelScope.launch {

                repository
                    .observeOrderDetail(
                        orderId =
                            orderId,
                    )
                    .collect { result ->

                        result
                            .onSuccess { response ->

                                val order =
                                    response.order

                                if (
                                    response.ok &&
                                    order != null
                                ) {

                                    _uiState.value =
                                        MarketOrderDetailUiState(
                                            isLoading =
                                                false,

                                            order =
                                                order,
                                        )

                                } else {

                                    _uiState.value =
                                        MarketOrderDetailUiState(
                                            isLoading =
                                                false,

                                            errorMessage =
                                                when (
                                                    response.code
                                                ) {
                                                    "ORDER_NOT_FOUND" ->
                                                        "订单不存在或无权查看"

                                                    else ->
                                                        "订单详情加载失败"
                                                },
                                        )
                                }
                            }
                            .onFailure { error ->

                                _uiState.value =
                                    _uiState.value.copy(
                                        isLoading =
                                            false,

                                        errorMessage =
                                            error.message
                                                ?: "订单详情加载失败",
                                    )
                            }
                    }
            }
    }


    companion object {

        fun factory(
            orderId: String,
            repository: MarketOrderDetailRepository,
        ): ViewModelProvider.Factory {

            return object :
                ViewModelProvider.Factory {

                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                ): T {

                    return MarketOrderDetailViewModel(
                        orderId =
                            orderId,

                        repository =
                            repository,
                    ) as T
                }
            }
        }
    }
}