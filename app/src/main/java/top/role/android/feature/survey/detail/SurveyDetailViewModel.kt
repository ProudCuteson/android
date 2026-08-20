package top.role.android.feature.survey.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import top.role.android.core.model.survey.MobileSurveyDetail


data class SurveyDetailUiState(
    val loading:
    Boolean = false,

    val signedOut:
    Boolean = false,

    val detail:
    MobileSurveyDetail? = null,

    val groups:
    List<
            top.role.android.core.model.survey.MobileSurveyGroup
            > =
        emptyList(),

    val currentGroupIndex:
    Int = 0,

    val answers:
    Map<String, Any?> =
        emptyMap(),

    val submitting:
    Boolean = false,

    val submitted:
    Boolean = false,

    val submitMessage:
    String? = null,

    val errorMessage:
    String? = null,
)


class SurveyDetailViewModel(
    private val surveyId:
    String,

    private val repository:
    SurveyDetailRepository,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            SurveyDetailUiState()
        )

    val uiState:
            StateFlow<SurveyDetailUiState> =
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

        observeDetail()
    }


    fun showSignedOut() {
        observeJob?.cancel()
        observeJob = null

        _uiState.value =
            SurveyDetailUiState(
                signedOut = true,
            )
    }


    fun retry() {
        observeJob?.cancel()
        observeJob = null

        observeDetail()
    }


    fun updateAnswer(
        fieldId: String,
        value: Any?,
    ) {
        if (
            fieldId.isBlank()
        ) {
            return
        }

        val newAnswers =
            _uiState.value
                .answers
                .toMutableMap()

        if (
            value == null
        ) {
            newAnswers.remove(
                fieldId
            )
        } else {
            newAnswers[fieldId] =
                value
        }

        _uiState.value =
            _uiState.value.copy(
                answers =
                    newAnswers,
            )
    }


    fun nextGroup() {
        val state =
            _uiState.value

        if (
            state.currentGroupIndex >=
            state.groups.lastIndex
        ) {
            return
        }

        _uiState.value =
            state.copy(
                currentGroupIndex =
                    state.currentGroupIndex +
                            1,
            )
    }


    fun previousGroup() {
        val state =
            _uiState.value

        if (
            state.currentGroupIndex <=
            0
        ) {
            return
        }

        _uiState.value =
            state.copy(
                currentGroupIndex =
                    state.currentGroupIndex -
                            1,
            )
    }


    fun submit() {
        val state =
            _uiState.value

        if (
            state.submitting
        ) {
            return
        }

        _uiState.value =
            state.copy(
                submitting = true,
                errorMessage = null,
            )

        viewModelScope.launch {

            runCatching {
                repository.submit(
                    surveyId =
                        surveyId,

                    answers =
                        _uiState.value
                            .answers,
                )
            }
                .onSuccess { result ->

                    if (
                        result.ok
                    ) {
                        _uiState.value =
                            _uiState.value.copy(
                                submitting =
                                    false,

                                submitted =
                                    true,

                                submitMessage =
                                    result.message
                                        ?: "提交成功",
                            )
                    } else {
                        _uiState.value =
                            _uiState.value.copy(
                                submitting =
                                    false,

                                errorMessage =
                                    when (
                                        result.code
                                    ) {
                                        "MISSING_REQUIRED_FIELDS" ->
                                            "还有必填题目未完成"

                                        else ->
                                            result.code
                                                ?: "提交失败"
                                    },
                            )
                    }
                }
                .onFailure { error ->

                    _uiState.value =
                        _uiState.value.copy(
                            submitting =
                                false,

                            errorMessage =
                                error.message
                                    ?: "提交失败",
                        )
                }
        }
    }


    private fun observeDetail() {
        _uiState.value =
            _uiState.value.copy(
                loading = true,
                signedOut = false,
                errorMessage = null,
            )

        observeJob =
            viewModelScope.launch {

                repository
                    .observeDetail(
                        surveyId =
                            surveyId,
                    )
                    .collect { result ->

                        result.fold(
                            onSuccess = {
                                    response ->

                                if (
                                    !response.ok
                                ) {
                                    _uiState.value =
                                        _uiState.value.copy(
                                            loading =
                                                false,

                                            errorMessage =
                                                response.code
                                                    ?: "调查不可用",
                                        )

                                    return@fold
                                }

                                _uiState.value =
                                    _uiState.value.copy(
                                        loading =
                                            false,

                                        detail =
                                            response.survey,

                                        groups =
                                            response.groups,

                                        errorMessage =
                                            null,
                                    )
                            },

                            onFailure = {
                                    error ->

                                _uiState.value =
                                    _uiState.value.copy(
                                        loading =
                                            false,

                                        errorMessage =
                                            error.message
                                                ?: "加载调查失败",
                                    )
                            },
                        )
                    }
            }
    }
}