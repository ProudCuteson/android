package top.role.android.feature.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import top.role.android.core.model.discover.MobileDiscoverPage


data class DiscoverUiState(
    val isLoading: Boolean = true,
    val isOffline: Boolean = false,
    val isSignedOut: Boolean = false,

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


    private var observeJob: Job? =
        null


    fun start() {
        if (
            observeJob != null
        ) {
            return
        }


        _uiState.update {
            it.copy(
                isLoading = true,
                isOffline = false,
                isSignedOut = false,
                errorMessage = null,
            )
        }


        observeJob =
            viewModelScope.launch {

                repository
                    .observeDiscover()
                    .collect { result ->

                        result
                            .onSuccess { page ->

                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        isOffline = false,
                                        isSignedOut = false,

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
                                        isOffline = false,
                                        isSignedOut = false,

                                        errorMessage =
                                            error.message
                                                ?: "加载发现内容失败",
                                    )
                                }
                            }
                    }
            }
    }


    fun showOffline() {
        stopSubscription()


        _uiState.update {
            it.copy(
                isLoading = false,

                isOffline = true,
                isSignedOut = false,

                errorMessage = null,
            )
        }
    }


    fun showSignedOut() {
        stopSubscription()


        _uiState.update {
            it.copy(
                isLoading = false,

                isOffline = false,
                isSignedOut = true,

                errorMessage = null,
            )
        }
    }


    fun retry() {
        stopSubscription()
        start()
    }


    private fun stopSubscription() {
        observeJob?.cancel()
        observeJob =
            null
    }


    override fun onCleared() {
        stopSubscription()

        super.onCleared()
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