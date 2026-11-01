package top.role.android.feature.banner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BannerUiState(
    val isLoading: Boolean = true,
    val items: List<MobileShareCard> = emptyList(),
    val errorMessage: String? = null,
)

class BannerViewModel(
    private val repository: BannerRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BannerUiState())

    val uiState: StateFlow<BannerUiState> =
        _uiState.asStateFlow()

    private var observeJob: Job? = null

    fun start() {
        if (observeJob?.isActive == true) return

        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null,
            )
        }

        observeJob = viewModelScope.launch {
            repository.observeBanner().collect { result ->
                result.onSuccess { items ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            items = items,
                            errorMessage = null,
                        )
                    }
                }.onFailure { error ->
                    error.printStackTrace()

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message
                                ?: "加载 Banner 失败",
                        )
                    }
                }
            }
        }
    }

    fun retry() {
        stopSubscription()
        start()
    }

    private fun stopSubscription() {
        observeJob?.cancel()
        observeJob = null
    }

    override fun onCleared() {
        stopSubscription()
        super.onCleared()
    }
}

class BannerViewModelFactory(
    private val repository: BannerRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
    ): T {
        if (modelClass.isAssignableFrom(BannerViewModel::class.java)) {
            return BannerViewModel(repository) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}