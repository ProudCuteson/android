package top.role.android.feature.bounty

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import top.role.android.core.model.bounty.MobileBountyListMode
import top.role.android.core.model.engage.MobileBountyCard

data class BountyListUiState(
    val mode:
    MobileBountyListMode =
        MobileBountyListMode.Featured,

    val items:
    List<MobileBountyCard> =
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


class BountyListViewModel(
    private val mode:
    MobileBountyListMode,

    private val repository:
    BountyListRepository,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            BountyListUiState(
                mode = mode,
            )
        )

    val uiState:
            StateFlow<BountyListUiState> =
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
            BountyListUiState(
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
            BountyListUiState(
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
                                    BountyListUiState(
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
                                                ?: "加载任务失败",
                                    )
                            },
                        )
                    }
            }
    }
}