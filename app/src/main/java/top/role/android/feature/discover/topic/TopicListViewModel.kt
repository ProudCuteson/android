package top.role.android.feature.discover.topic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import top.role.android.core.model.topic.MobileTopicCard
import top.role.android.core.model.topic.TopicListMode

data class TopicListSectionState(
    val items: List<MobileTopicCard> = emptyList(),

    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,

    val isDone: Boolean = false,
    val continueCursor: String? = null,

    val errorMessage: String? = null,
)

data class TopicListUiState(
    val isLoading: Boolean = true,

    val featured: TopicListSectionState =
        TopicListSectionState(),

    val following: TopicListSectionState =
        TopicListSectionState(),

    val latest: TopicListSectionState =
        TopicListSectionState(),
)

class TopicListViewModel(
    private val repository: TopicListRepository,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            TopicListUiState()
        )

    val uiState: StateFlow<TopicListUiState> =
        _uiState.asStateFlow()

    init {
        loadInitial()
    }

    fun retry() {
        loadInitial()
    }

    fun retrySection(
        mode: TopicListMode,
    ) {
        loadSectionInitial(
            mode = mode,
        )
    }

    fun loadMore(
        mode: TopicListMode,
    ) {
        val section =
            sectionState(
                state = _uiState.value,
                mode = mode,
            )

        if (
            section.isLoading ||
            section.isLoadingMore ||
            section.isDone
        ) {
            return
        }

        val cursor =
            section.continueCursor
                ?: return

        viewModelScope.launch {
            updateSection(
                mode = mode,
            ) {
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
                    updateSection(
                        mode = mode,
                    ) { current ->
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

                    updateSection(
                        mode = mode,
                    ) {
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
        viewModelScope.launch {
            _uiState.value =
                TopicListUiState(
                    isLoading = true,

                    featured =
                        TopicListSectionState(
                            isLoading = true,
                        ),

                    following =
                        TopicListSectionState(
                            isLoading = true,
                        ),

                    latest =
                        TopicListSectionState(
                            isLoading = true,
                        ),
                )

            coroutineScope {
                listOf(
                    TopicListMode.Featured,
                    TopicListMode.Following,
                    TopicListMode.Latest,
                )
                    .map { mode ->
                        async {
                            loadSectionResult(
                                mode = mode,
                            )
                        }
                    }
                    .awaitAll()
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                )
            }
        }
    }

    private fun loadSectionInitial(
        mode: TopicListMode,
    ) {
        val current =
            sectionState(
                state = _uiState.value,
                mode = mode,
            )

        if (
            current.isLoading ||
            current.isLoadingMore
        ) {
            return
        }

        viewModelScope.launch {
            updateSection(
                mode = mode,
            ) {
                TopicListSectionState(
                    isLoading = true,
                )
            }

            loadSectionResult(
                mode = mode,
            )
        }
    }

    private suspend fun loadSectionResult(
        mode: TopicListMode,
    ) {
        repository
            .loadPage(
                mode = mode,
                cursor = null,
            )
            .onSuccess { result ->
                updateSection(
                    mode = mode,
                ) {
                    it.copy(
                        items =
                            result.page,

                        isLoading = false,
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

                updateSection(
                    mode = mode,
                ) {
                    it.copy(
                        isLoading = false,
                        isLoadingMore = false,

                        errorMessage =
                            error.message
                                ?: initialErrorMessage(
                                    mode = mode,
                                ),
                    )
                }
            }
    }

    private fun sectionState(
        state: TopicListUiState,
        mode: TopicListMode,
    ): TopicListSectionState {
        return when (mode) {
            TopicListMode.Featured ->
                state.featured

            TopicListMode.Following ->
                state.following

            TopicListMode.Latest ->
                state.latest
        }
    }

    private fun updateSection(
        mode: TopicListMode,
        transform:
            (TopicListSectionState) ->
        TopicListSectionState,
    ) {
        _uiState.update { state ->
            when (mode) {
                TopicListMode.Featured ->
                    state.copy(
                        featured =
                            transform(
                                state.featured
                            )
                    )

                TopicListMode.Following ->
                    state.copy(
                        following =
                            transform(
                                state.following
                            )
                    )

                TopicListMode.Latest ->
                    state.copy(
                        latest =
                            transform(
                                state.latest
                            )
                    )
            }
        }
    }

    private fun initialErrorMessage(
        mode: TopicListMode,
    ): String {
        return when (mode) {
            TopicListMode.Featured ->
                "推荐话题加载失败"

            TopicListMode.Following ->
                "关注话题加载失败"

            TopicListMode.Latest ->
                "最新话题加载失败"
        }
    }

    private fun mergeById(
        current:
        List<MobileTopicCard>,

        incoming:
        List<MobileTopicCard>,
    ): List<MobileTopicCard> {
        if (current.isEmpty()) {
            return incoming
        }

        if (incoming.isEmpty()) {
            return current
        }

        val result =
            LinkedHashMap<
                    String,
                    MobileTopicCard
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

class TopicListViewModelFactory(
    private val repository: TopicListRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
    ): T {

        if (
            modelClass.isAssignableFrom(
                TopicListViewModel::class.java
            )
        ) {
            return TopicListViewModel(
                repository = repository,
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}