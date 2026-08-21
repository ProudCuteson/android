package top.role.android.feature.market.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import top.role.android.core.model.market.MobileMarketOrderCard


data class MarketOrderListUiState(
    val isLoading:
    Boolean = false,

    val isLoadingMore:
    Boolean = false,

    val items:
    List<MobileMarketOrderCard> =
        emptyList(),

    /*
     * 后端实际使用 offset pagination。
     */
    val nextOffset:
    Double? = null,

    val hasMore:
    Boolean = false,

    val errorMessage:
    String? = null,
)


class MarketOrderListViewModel(
    private val repository:
    MarketOrderListRepository,
) : ViewModel() {

    companion object {

        private const val PAGE_SIZE =
            12.0
    }


    private val _uiState =
        MutableStateFlow(
            MarketOrderListUiState()
        )

    val uiState:
            StateFlow<MarketOrderListUiState> =
        _uiState.asStateFlow()


    private var loadJob:
            Job? = null

    private var loadMoreJob:
            Job? = null

    private var started =
        false


    fun start() {

        if (
            started
        ) {
            return
        }

        started =
            true

        loadInitial()
    }


    fun showSignedOut() {

        started =
            false

        loadJob?.cancel()
        loadJob =
            null

        loadMoreJob?.cancel()
        loadMoreJob =
            null


        _uiState.value =
            MarketOrderListUiState(
                isLoading =
                    false,

                errorMessage =
                    "请先登录",
            )
    }


    /*
     * ------------------------------------------------
     * Initial
     * ------------------------------------------------
     */

    private fun loadInitial() {

        loadJob?.cancel()

        loadMoreJob?.cancel()
        loadMoreJob =
            null


        _uiState.update {
            it.copy(
                isLoading =
                    true,

                isLoadingMore =
                    false,

                errorMessage =
                    null,
            )
        }


        loadJob =
            viewModelScope.launch {

                repository
                    .observeOrders(
                        offset =
                            0.0,

                        limit =
                            PAGE_SIZE,
                    )
                    .collect { result ->

                        result
                            .onSuccess { response ->

                                /*
                                 * 如果后端未来返回业务错误，
                                 * Repository/DTO 可继续补 code。
                                 */
                                if (
                                    !response.ok
                                ) {

                                    _uiState.update {
                                        it.copy(
                                            isLoading =
                                                false,

                                            isLoadingMore =
                                                false,

                                            errorMessage =
                                                "加载订单失败",
                                        )
                                    }

                                    return@onSuccess
                                }


                                _uiState.update {
                                    it.copy(
                                        isLoading =
                                            false,

                                        isLoadingMore =
                                            false,

                                        items =
                                            response.items,

                                        nextOffset =
                                            response
                                                .pagination
                                                .nextOffset,

                                        hasMore =
                                            response
                                                .pagination
                                                .hasMore,

                                        errorMessage =
                                            null,
                                    )
                                }
                            }
                            .onFailure { error ->

                                error.printStackTrace()


                                _uiState.update {
                                    it.copy(
                                        isLoading =
                                            false,

                                        isLoadingMore =
                                            false,

                                        errorMessage =
                                            error.message
                                                ?: "加载订单失败",
                                    )
                                }
                            }
                    }
            }
    }


    /*
     * ------------------------------------------------
     * Load more
     * ------------------------------------------------
     */

    fun loadMore() {

        val state =
            _uiState.value


        if (
            state.isLoading ||
            state.isLoadingMore ||
            !state.hasMore
        ) {
            return
        }


        val offset =
            state.nextOffset
                ?: return


        if (
            loadMoreJob?.isActive ==
            true
        ) {
            return
        }


        _uiState.update {
            it.copy(
                isLoadingMore =
                    true,

                errorMessage =
                    null,
            )
        }


        loadMoreJob =
            viewModelScope.launch {

                repository
                    .observeOrders(
                        offset =
                            offset,

                        limit =
                            PAGE_SIZE,
                    )
                    .collect { result ->

                        result
                            .onSuccess { response ->

                                if (
                                    !response.ok
                                ) {

                                    _uiState.update {
                                        it.copy(
                                            isLoadingMore =
                                                false,

                                            errorMessage =
                                                "加载更多订单失败",
                                        )
                                    }

                                    return@onSuccess
                                }


                                _uiState.update { current ->

                                    val merged =
                                        (
                                                current.items +
                                                        response.items
                                                )
                                            .distinctBy {
                                                it.id
                                            }


                                    current.copy(
                                        isLoadingMore =
                                            false,

                                        items =
                                            merged,

                                        nextOffset =
                                            response
                                                .pagination
                                                .nextOffset,

                                        hasMore =
                                            response
                                                .pagination
                                                .hasMore,

                                        errorMessage =
                                            null,
                                    )
                                }
                            }
                            .onFailure { error ->

                                error.printStackTrace()


                                _uiState.update {
                                    it.copy(
                                        isLoadingMore =
                                            false,

                                        errorMessage =
                                            error.message
                                                ?: "加载更多订单失败",
                                    )
                                }
                            }
                    }
            }
    }


    fun retry() {

        if (
            !started
        ) {
            return
        }

        loadInitial()
    }


    override fun onCleared() {

        loadJob?.cancel()
        loadMoreJob?.cancel()

        super.onCleared()
    }
}


class MarketOrderListViewModelFactory(
    private val repository:
    MarketOrderListRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass:
        Class<T>,
    ): T {

        if (
            modelClass.isAssignableFrom(
                MarketOrderListViewModel::class.java
            )
        ) {

            return MarketOrderListViewModel(
                repository =
                    repository,
            ) as T
        }


        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}