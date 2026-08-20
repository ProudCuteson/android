package top.role.android.feature.engage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import top.role.android.core.model.engage.MobileEngageHome

data class EngageUiState(
    val loading: Boolean = false,

    val signedOut: Boolean = false,

    val home:
    MobileEngageHome? = null,

    val errorMessage:
    String? = null,
)


class EngageViewModel(
    private val repository:
    EngageRepository,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            EngageUiState()
        )

    val uiState:
            StateFlow<EngageUiState> =
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

        observeHome()
    }


    fun showSignedOut() {
        observeJob?.cancel()
        observeJob = null

        _uiState.value =
            EngageUiState(
                loading = false,
                signedOut = true,
                home = null,
                errorMessage = null,
            )
    }


    fun retry() {
        observeJob?.cancel()
        observeJob = null

        observeHome()
    }


    private fun observeHome() {
        _uiState.value =
            _uiState.value.copy(
                loading = true,
                signedOut = false,
                errorMessage = null,
            )

        observeJob =
            viewModelScope.launch {

                repository
                    .observeHome()
                    .collect { result ->

                        result.fold(
                            onSuccess = { home ->

                                _uiState.value =
                                    EngageUiState(
                                        loading = false,
                                        signedOut = false,
                                        home = home,
                                        errorMessage = null,
                                    )
                            },

                            onFailure = { error ->

                                _uiState.value =
                                    _uiState.value.copy(
                                        loading = false,
                                        signedOut = false,
                                        errorMessage =
                                            error.message
                                                ?: "加载参与内容失败",
                                    )
                            },
                        )
                    }
            }
    }
}