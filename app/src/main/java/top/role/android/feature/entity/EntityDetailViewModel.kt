package top.role.android.feature.entity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import top.role.android.core.model.entity.EntityDetailResponse

data class EntityDetailUiState(
    val isLoading: Boolean = true,
    val detail: EntityDetailResponse? = null,
    val errorMessage: String? = null,
)

class EntityDetailViewModel(
    private val id: String,
    private val repository: EntityDetailRepository,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            EntityDetailUiState()
        )

    val uiState: StateFlow<EntityDetailUiState> =
        _uiState.asStateFlow()

    init {
        observeDetail()
    }

    private fun observeDetail() {
        viewModelScope.launch {
            repository
                .observeSpaceDetail(id)
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
                                            ?: "加载空间失败",
                                )
                            }
                        }
                }
        }
    }
}

class EntityDetailViewModelFactory(
    private val id: String,
    private val repository: EntityDetailRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
    ): T {

        if (
            modelClass.isAssignableFrom(
                EntityDetailViewModel::class.java
            )
        ) {
            return EntityDetailViewModel(
                id = id,
                repository = repository,
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}