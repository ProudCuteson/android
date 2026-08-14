package top.role.android.feature.discover.space

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import top.role.android.core.model.space.MobileSpaceCard
import top.role.android.core.model.space.SpaceListMode

data class SpaceListUiState(
    val mode: SpaceListMode,

    val items: List<MobileSpaceCard> = emptyList(),

    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,

    val isDone: Boolean = false,
    val continueCursor: String? = null,

    val errorMessage: String? = null,
)

class SpaceListViewModel(
    private val mode: SpaceListMode,
    private val repository: SpaceListRepository,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            SpaceListUiState(
                mode = mode,
            )
        )

    val uiState: StateFlow<SpaceListUiState> =
        _uiState.asStateFlow()

    init {
        loadInitial()
    }

    fun retry() {
        loadInitial()
    }

    fun loadMore() {
        val state =
            _uiState.value

        if (
            state.isLoading ||
            state.isLoadingMore ||
            state.isDone
        ) {
            return
        }

        val cursor =
            state.continueCursor
                ?: return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingMore = true,
                    errorMessage = null,
                )
            }

            repository
                .loadPage(
                    mode = mode,
                    cursor = cursor,
                )
                .onSuccess { result ->
                    _uiState.update { current ->
                        current.copy(
                            items =
                                mergeById(
                                    current.items,
                                    result.page,
                                ),

                            isLoadingMore = false,

                            isDone =
                                result.isDone,

                            continueCursor =
                                result.continueCursor
                                    .takeIf {
                                        !result.isDone
                                    },

                            errorMessage = null,
                        )
                    }
                }
                .onFailure { error ->
                    error.printStackTrace()

                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            errorMessage =
                                error.message
                                    ?: "加载更多失败",
                        )
                    }
                }
        }
    }

    private fun loadInitial() {
        val current =
            _uiState.value

        if (current.isLoadingMore) {
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    isLoadingMore = false,

                    items = emptyList(),

                    isDone = false,
                    continueCursor = null,

                    errorMessage = null,
                )
            }

            repository
                .loadPage(
                    mode = mode,
                    cursor = null,
                )
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            items =
                                result.page,

                            isLoading = false,

                            isDone =
                                result.isDone,

                            continueCursor =
                                result.continueCursor
                                    .takeIf {
                                        !result.isDone
                                    },

                            errorMessage = null,
                        )
                    }
                }
                .onFailure { error ->
                    error.printStackTrace()

                    _uiState.update {
                        it.copy(
                            isLoading = false,

                            errorMessage =
                                error.message
                                    ?: "加载空间失败",
                        )
                    }
                }
        }
    }

    private fun mergeById(
        current:
        List<MobileSpaceCard>,

        incoming:
        List<MobileSpaceCard>,
    ): List<MobileSpaceCard> {
        if (current.isEmpty()) {
            return incoming
        }

        if (incoming.isEmpty()) {
            return current
        }

        val result =
            LinkedHashMap<
                    String,
                    MobileSpaceCard
                    >()

        current.forEach {
            result[it.id] = it
        }

        incoming.forEach {
            result[it.id] = it
        }

        return result.values.toList()
    }
}

class SpaceListViewModelFactory(
    private val mode: SpaceListMode,
    private val repository: SpaceListRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
    ): T {

        if (
            modelClass.isAssignableFrom(
                SpaceListViewModel::class.java
            )
        ) {
            return SpaceListViewModel(
                mode = mode,
                repository = repository,
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}