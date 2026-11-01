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

    private val _uiState = MutableStateFlow(
        MarketOrderDetailUiState()
    )

    val uiState: StateFlow<MarketOrderDetailUiState> =
        _uiState.asStateFlow()

    private var observeJob: Job? = null

    private var started = false


    /*
     * ================================================================
     * Lifecycle
     * ================================================================
     */

    fun start() {
        if (observeJob?.isActive == true) {
            return
        }

        started = true
        observeOrder()
    }

    fun stop() {
        started = false

        observeJob?.cancel()
        observeJob = null

        // 订单详情属于账户私有数据，停止时清理。
        _uiState.value = MarketOrderDetailUiState()
    }


    /*
     * ================================================================
     * Retry
     * ================================================================
     */

    fun retry() {
        if (!started) {
            return
        }

        observeOrder()
    }


    /*
     * ================================================================
     * Observe Order
     * ================================================================
     */

    private fun observeOrder() {
        observeJob?.cancel()
        observeJob = null

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null,
        )

        observeJob = viewModelScope.launch {
            repository
                .observeOrderDetail(
                    orderId = orderId,
                )
                .collect { result ->

                    result
                        .onSuccess { response ->
                            val order = response.order

                            if (
                                response.ok &&
                                order != null
                            ) {
                                _uiState.value =
                                    MarketOrderDetailUiState(
                                        isLoading = false,
                                        order = order,
                                    )
                            } else {
                                _uiState.value =
                                    MarketOrderDetailUiState(
                                        isLoading = false,
                                        errorMessage = when (response.code) {
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
                                    isLoading = false,
                                    errorMessage = error.message
                                        ?: "订单详情加载失败",
                                )
                        }
                }
        }
    }


    /*
     * ================================================================
     * Cleanup
     * ================================================================
     */

    override fun onCleared() {
        stop()
        super.onCleared()
    }


    /*
     * ================================================================
     * Factory
     * ================================================================
     */

    companion object {

        fun factory(
            orderId: String,
            repository: MarketOrderDetailRepository,
        ): ViewModelProvider.Factory {

            return object : ViewModelProvider.Factory {

                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                ): T {
                    if (
                        modelClass.isAssignableFrom(
                            MarketOrderDetailViewModel::class.java
                        )
                    ) {
                        return MarketOrderDetailViewModel(
                            orderId = orderId,
                            repository = repository,
                        ) as T
                    }

                    throw IllegalArgumentException(
                        "Unknown ViewModel class: ${modelClass.name}"
                    )
                }
            }
        }
    }
}