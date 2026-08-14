package top.role.android.feature.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import top.role.android.core.model.discover.MobileDiscoverPage

data class DiscoverUiState(
    val isLoading: Boolean = true,
    val page: MobileDiscoverPage? = null,
    val errorMessage: String? = null,
)

class DiscoverViewModel(
    private val repository: DiscoverRepository,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            DiscoverUiState()
        )

    val uiState: StateFlow<DiscoverUiState> =
        _uiState.asStateFlow()

    init {
        observeDiscover()
    }

    private fun observeDiscover() {
        viewModelScope.launch {
            repository
                .observeDiscover()
                .collect { result ->

                    result
                        .onSuccess { page ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    page = page,
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
                                            ?: "加载发现内容失败",
                                )
                            }
                        }
                }
        }
    }
}

class DiscoverViewModelFactory(
    private val repository: DiscoverRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
    ): T {

        if (
            modelClass.isAssignableFrom(
                DiscoverViewModel::class.java
            )
        ) {
            return DiscoverViewModel(
                repository = repository,
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}