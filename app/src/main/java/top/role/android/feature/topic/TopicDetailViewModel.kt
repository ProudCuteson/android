package top.role.android.feature.topic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import top.role.android.core.model.topic.TopicDetailResponse

data class TopicDetailUiState(
    val isLoading: Boolean = true,
    val detail: TopicDetailResponse? = null,
    val errorMessage: String? = null,
)

class TopicDetailViewModel(
    private val topicId: String,
    private val repository: TopicDetailRepository,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            TopicDetailUiState()
        )

    val uiState: StateFlow<TopicDetailUiState> =
        _uiState.asStateFlow()

    init {
        observeTopic()
    }

    private fun observeTopic() {
        viewModelScope.launch {
            repository
                .observeTopicDetail(
                    topicId = topicId,
                )
                .collect { result ->

                    result
                        .onSuccess { detail ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    detail = detail,
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
                                            ?: "加载话题失败",
                                )
                            }
                        }
                }
        }
    }
}

class TopicDetailViewModelFactory(
    private val topicId: String,
    private val repository: TopicDetailRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
    ): T {

        if (
            modelClass.isAssignableFrom(
                TopicDetailViewModel::class.java
            )
        ) {
            return TopicDetailViewModel(
                topicId = topicId,
                repository = repository,
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}