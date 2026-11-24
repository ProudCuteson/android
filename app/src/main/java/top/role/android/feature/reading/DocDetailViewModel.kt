package top.role.android.feature.reading

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import top.role.android.core.model.content.MobileDocDetailResponse

data class DocDetailUiState(
    val isLoading: Boolean = true,
    val detail: MobileDocDetailResponse? = null,
    val errorMessage: String? = null,
)

class DocDetailViewModel(
    private val docId: String,
    private val topicId: String?,
    private val repository: DocDetailRepository,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(DocDetailUiState())

    val uiState: StateFlow<DocDetailUiState> =
        _uiState.asStateFlow()

    init {
        observeDoc()
    }

    private fun observeDoc() {
        viewModelScope.launch {

            repository
                .observeDocDetail(
                    docId = docId,
                    topicId = topicId,
                )
                .catch { error ->

                    error.printStackTrace()

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            detail = null,
                            errorMessage =
                                error.message
                                    ?: "加载文档失败",
                        )
                    }
                }
                .collect { result ->

                    result
                        .onSuccess { detail ->

                            if (detail == null) {
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        detail = null,
                                        errorMessage =
                                            "文档不存在或暂不可访问",
                                    )
                                }
                                return@onSuccess
                            }

                            println(
                                "DocDetail success: " +
                                        "doc=${detail.doc.id} " +
                                        "version=${detail.renderDoc.version} " +
                                        "blocks=${detail.renderDoc.blocks.size} " +
                                        "charts=${detail.charts.size}"
                            )

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
                                    detail = null,
                                    errorMessage =
                                        error.message
                                            ?: "加载文档失败",
                                )
                            }
                        }
                }
        }
    }
}

class DocDetailViewModelFactory(
    private val docId: String,
    private val topicId: String?,
    private val repository: DocDetailRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
    ): T {

        if (
            modelClass.isAssignableFrom(
                DocDetailViewModel::class.java
            )
        ) {
            return DocDetailViewModel(
                docId = docId,
                topicId = topicId,
                repository = repository,
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}