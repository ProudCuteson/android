package top.role.android.feature.market

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import top.role.android.core.model.engage.MobileMarketCard
import top.role.android.core.model.market.MobileMarketListMode


data class MarketListUiState(
    val mode:
    MobileMarketListMode =
        MobileMarketListMode.Featured,

    val items:
    List<MobileMarketCard> =
        emptyList(),

    val loading:
    Boolean = false,

    val loadingMore:
    Boolean = false,

    val signedOut:
    Boolean = false,

    val isDone:
    Boolean = false,

    val continueCursor:
    String = "",

    val errorMessage:
    String? = null,
)


class MarketListViewModel(
    private val mode:
    MobileMarketListMode,

    private val repository:
    MarketListRepository,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            MarketListUiState(
                mode = mode,
            )
        )

    val uiState:
            StateFlow<MarketListUiState> =
        _uiState.asStateFlow()

    private var observeJob:
            Job? = null


    fun start() {
        if (
            observeJob?.isActive ==
            true
        ) {
            return
        }

        loadInitial()
    }


    fun showSignedOut() {
        observeJob?.cancel()
        observeJob = null

        _uiState.value =
            MarketListUiState(
                mode = mode,
                signedOut = true,
            )
    }


    fun retry() {
        observeJob?.cancel()
        observeJob = null

        loadInitial()
    }


    fun loadMore() {
        val state =
            _uiState.value

        if (
            state.loading ||
            state.loadingMore ||
            state.isDone ||
            state.continueCursor
                .isBlank()
        ) {
            return
        }

        observeJob?.cancel()
        observeJob = null

        _uiState.value =
            state.copy(
                loadingMore = true,
                errorMessage = null,
            )

        observeJob =
            viewModelScope.launch {

                repository
                    .observePage(
                        mode = mode,

                        cursor =
                            state.continueCursor,
                    )
                    .collect { result ->

                        result.fold(
                            onSuccess = { page ->

                                _uiState.value =
                                    _uiState.value.copy(
                                        items =
                                            (
                                                    state.items +
                                                            page.page
                                                    )
                                                .distinctBy {
                                                    it.id
                                                },

                                        loadingMore =
                                            false,

                                        isDone =
                                            page.isDone,

                                        continueCursor =
                                            page.continueCursor,

                                        errorMessage =
                                            null,
                                    )
                            },

                            onFailure = { error ->

                                _uiState.value =
                                    _uiState.value.copy(
                                        loadingMore =
                                            false,

                                        errorMessage =
                                            error.message
                                                ?: "加载更多失败",
                                    )
                            },
                        )
                    }
            }
    }


    private fun loadInitial() {
        _uiState.value =
            MarketListUiState(
                mode = mode,
                loading = true,
            )

        observeJob =
            viewModelScope.launch {

                repository
                    .observePage(
                        mode = mode,
                    )
                    .collect { result ->

                        result.fold(
                            onSuccess = { page ->

                                _uiState.value =
                                    MarketListUiState(
                                        mode = mode,

                                        items =
                                            page.page,

                                        loading =
                                            false,

                                        isDone =
                                            page.isDone,

                                        continueCursor =
                                            page.continueCursor,
                                    )
                            },

                            onFailure = { error ->

                                _uiState.value =
                                    _uiState.value.copy(
                                        loading =
                                            false,

                                        errorMessage =
                                            error.message
                                                ?: "加载单品失败",
                                    )
                            },
                        )
                    }
            }
    }
}