package top.role.android.feature.market.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import top.role.android.core.model.market.MobileMarketDetail


data class MarketDetailUiState(
    val loading:
    Boolean = false,

    val signedOut:
    Boolean = false,

    val listing:
    MobileMarketDetail? = null,

    val errorMessage:
    String? = null,
)


class MarketDetailViewModel(
    private val listingId:
    String,

    private val repository:
    MarketDetailRepository,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            MarketDetailUiState()
        )

    val uiState:
            StateFlow<MarketDetailUiState> =
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

        observeDetail()
    }


    fun showSignedOut() {
        observeJob?.cancel()
        observeJob = null

        _uiState.value =
            MarketDetailUiState(
                signedOut = true,
            )
    }


    fun retry() {
        observeJob?.cancel()
        observeJob = null

        observeDetail()
    }


    private fun observeDetail() {
        _uiState.value =
            _uiState.value.copy(
                loading = true,
                signedOut = false,
                errorMessage = null,
            )

        observeJob =
            viewModelScope.launch {

                repository
                    .observeDetail(
                        listingId =
                            listingId,
                    )
                    .collect { result ->

                        result.fold(
                            onSuccess = {
                                    response ->

                                if (
                                    !response.ok ||
                                    response.listing == null
                                ) {
                                    _uiState.value =
                                        _uiState.value.copy(
                                            loading =
                                                false,

                                            errorMessage =
                                                response.code
                                                    ?: "单品不可用",
                                        )

                                    return@fold
                                }

                                _uiState.value =
                                    _uiState.value.copy(
                                        loading =
                                            false,

                                        listing =
                                            response.listing,

                                        errorMessage =
                                            null,
                                    )
                            },

                            onFailure = {
                                    error ->

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