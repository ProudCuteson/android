package top.role.android.feature.slide

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import top.role.android.core.model.slide.MobileSlideDetailResponse
import top.role.android.core.model.slide.MobileSlidePage
import top.role.android.core.model.slide.MobileSlideQuestionItem

data class SlideDetailUiState(
    val isLoading: Boolean = true,

    val detail: MobileSlideDetailResponse? = null,

    val currentPageIndex: Int = 0,

    val isUnavailable: Boolean = false,

    val errorMessage: String? = null,

    /*
     * ------------------------------------------------------------
     * Q&A
     * ------------------------------------------------------------
     */

    val questionsLoading: Boolean = false,

    val questions: List<MobileSlideQuestionItem> =
        emptyList(),

    val questionsError: String? = null,

    val submittingQuestion: Boolean = false,

    val questionSubmitError: String? = null,

    /**
     * 当前正在执行 presenter action 的 questionId。
     *
     * 用于：
     * - 标记已回答
     * - 隐藏
     */
    val questionActionId: String? = null,

    val questionActionError: String? = null,
) {

    val pages: List<MobileSlidePage>
        get() =
            detail?.pages
                ?: emptyList()

    val currentPage: MobileSlidePage?
        get() =
            pages.getOrNull(
                currentPageIndex
            )

    val pageCount: Int
        get() =
            pages.size

    val hasPreviousPage: Boolean
        get() =
            currentPageIndex > 0 &&
                    pages.isNotEmpty()

    val hasNextPage: Boolean
        get() =
            currentPageIndex >= 0 &&
                    currentPageIndex <
                    pages.lastIndex

    val isAuthed: Boolean
        get() =
            detail?.viewer?.isAuthed == true

    val canAsk: Boolean
        get() =
            detail?.viewer?.canAsk == true

    /**
     * 后端当前字段名是 canEdit，
     * 但移动端不提供编辑功能。
     *
     * 当前实际语义：
     * slide owner / presenter 权限。
     */
    val isOwner: Boolean
        get() =
            detail?.viewer?.canEdit == true

    /**
     * Presenter 视图使用：
     *
     * open 优先；
     * open 内按创建时间从早到晚；
     * answered 放在后面。
     */
    val presenterQuestions:
            List<MobileSlideQuestionItem>
        get() =
            questions.sortedWith(
                compareBy<MobileSlideQuestionItem> {
                    if (
                        it.status ==
                        "open"
                    ) {
                        0
                    } else {
                        1
                    }
                }.thenBy {
                    it.createdAt
                }
            )
}

class SlideDetailViewModel(
    private val slideId: String,
    private val repository: SlideDetailRepository,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            SlideDetailUiState()
        )

    val uiState:
            StateFlow<SlideDetailUiState> =
        _uiState.asStateFlow()

    init {
        load()
    }

    fun retry() {
        load()
    }

    fun goToPreviousPage() {
        val target =
            _uiState.value
                .currentPageIndex -
                    1

        changePage(
            target
        )
    }

    fun goToNextPage() {
        val target =
            _uiState.value
                .currentPageIndex +
                    1

        changePage(
            target
        )
    }

    fun goToPage(
        index: Int,
    ) {
        changePage(
            index
        )
    }

    /**
     * 手动加载当前页问题。
     *
     * 用于：
     * - 打开 Q&A Sheet
     * - 手动刷新
     * - 提交成功后刷新
     * - presenter 操作成功后刷新
     */
    fun loadQuestions() {
        val state =
            _uiState.value

        if (
            state.detail == null ||
            state.pageCount <= 0
        ) {
            return
        }

        val cardIndex =
            state.currentPageIndex

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    questionsLoading =
                        true,

                    questionsError =
                        null,
                )
            }

            repository
                .loadQuestions(
                    slideId =
                        slideId,

                    cardIndex =
                        cardIndex,

                    limit =
                        20,
                )
                .onSuccess { response ->

                    /*
                     * 页面可能已经切换。
                     *
                     * 旧页面的请求回来以后，
                     * 不应该覆盖新页面的问题。
                     */
                    if (
                        _uiState.value
                            .currentPageIndex !=
                        cardIndex
                    ) {
                        return@onSuccess
                    }

                    if (response.ok) {
                        _uiState.update {
                            it.copy(
                                questionsLoading =
                                    false,

                                questions =
                                    response.data
                                        ?: emptyList(),

                                questionsError =
                                    null,
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                questionsLoading =
                                    false,

                                questions =
                                    emptyList(),

                                questionsError =
                                    response.message
                                        ?: questionFallbackMessage(
                                            response.code
                                        ),
                            )
                        }
                    }
                }
                .onFailure { error ->

                    error.printStackTrace()

                    if (
                        _uiState.value
                            .currentPageIndex !=
                        cardIndex
                    ) {
                        return@onFailure
                    }

                    _uiState.update {
                        it.copy(
                            questionsLoading =
                                false,

                            questions =
                                emptyList(),

                            questionsError =
                                error.message
                                    ?: "问题加载失败",
                        )
                    }
                }
        }
    }

    fun submitQuestion(
        text: String,
        onSuccess: (() -> Unit)? = null,
    ) {
        val state =
            _uiState.value

        if (
            state.submittingQuestion ||
            state.detail == null
        ) {
            return
        }

        val trimmed =
            text.trim()

        if (trimmed.isEmpty()) {
            _uiState.update {
                it.copy(
                    questionSubmitError =
                        "请输入问题。"
                )
            }

            return
        }

        if (
            trimmed.length >
            500
        ) {
            _uiState.update {
                it.copy(
                    questionSubmitError =
                        "问题太长，请控制在 500 字以内。"
                )
            }

            return
        }

        val cardIndex =
            state.currentPageIndex

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    submittingQuestion =
                        true,

                    questionSubmitError =
                        null,
                )
            }

            repository
                .createQuestion(
                    slideId =
                        slideId,

                    cardIndex =
                        cardIndex,

                    text =
                        trimmed,
                )
                .onSuccess { response ->

                    if (response.ok) {
                        _uiState.update {
                            it.copy(
                                submittingQuestion =
                                    false,

                                questionSubmitError =
                                    null,
                            )
                        }

                        /*
                         * 先刷新问题列表。
                         *
                         * 当前实现还是手动 reload，
                         * 暂时不做 realtime subscription。
                         */
                        loadQuestions()

                        onSuccess?.invoke()
                    } else {
                        _uiState.update {
                            it.copy(
                                submittingQuestion =
                                    false,

                                questionSubmitError =
                                    response.message
                                        ?: questionFallbackMessage(
                                            response.code
                                        ),
                            )
                        }
                    }
                }
                .onFailure { error ->

                    error.printStackTrace()

                    _uiState.update {
                        it.copy(
                            submittingQuestion =
                                false,

                            questionSubmitError =
                                error.message
                                    ?: "提交失败，请稍后再试。",
                        )
                    }
                }
        }
    }

    fun markQuestionAnswered(
        questionId: String,
    ) {
        if (
            questionId.isBlank() ||
            _uiState.value
                .questionActionId !=
            null
        ) {
            return
        }

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    questionActionId =
                        questionId,

                    questionActionError =
                        null,
                )
            }

            repository
                .markQuestionAnswered(
                    slideId =
                        slideId,

                    questionId =
                        questionId,
                )
                .onSuccess { response ->

                    if (response.ok) {
                        _uiState.update {
                            it.copy(
                                questionActionId =
                                    null,

                                questionActionError =
                                    null,
                            )
                        }

                        loadQuestions()
                    } else {
                        _uiState.update {
                            it.copy(
                                questionActionId =
                                    null,

                                questionActionError =
                                    response.message
                                        ?: questionFallbackMessage(
                                            response.code
                                        ),
                            )
                        }
                    }
                }
                .onFailure { error ->

                    error.printStackTrace()

                    _uiState.update {
                        it.copy(
                            questionActionId =
                                null,

                            questionActionError =
                                error.message
                                    ?: "操作失败，请稍后再试。",
                        )
                    }
                }
        }
    }

    fun hideQuestion(
        questionId: String,
    ) {
        if (
            questionId.isBlank() ||
            _uiState.value
                .questionActionId !=
            null
        ) {
            return
        }

        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    questionActionId =
                        questionId,

                    questionActionError =
                        null,
                )
            }

            repository
                .hideQuestion(
                    slideId =
                        slideId,

                    questionId =
                        questionId,
                )
                .onSuccess { response ->

                    if (response.ok) {
                        _uiState.update {
                            it.copy(
                                questionActionId =
                                    null,

                                questionActionError =
                                    null,
                            )
                        }

                        loadQuestions()
                    } else {
                        _uiState.update {
                            it.copy(
                                questionActionId =
                                    null,

                                questionActionError =
                                    response.message
                                        ?: questionFallbackMessage(
                                            response.code
                                        ),
                            )
                        }
                    }
                }
                .onFailure { error ->

                    error.printStackTrace()

                    _uiState.update {
                        it.copy(
                            questionActionId =
                                null,

                            questionActionError =
                                error.message
                                    ?: "操作失败，请稍后再试。",
                        )
                    }
                }
        }
    }

    fun clearQuestionSubmitError() {
        _uiState.update {
            it.copy(
                questionSubmitError =
                    null
            )
        }
    }

    fun clearQuestionActionError() {
        _uiState.update {
            it.copy(
                questionActionError =
                    null
            )
        }
    }

    private fun changePage(
        index: Int,
    ) {
        val state =
            _uiState.value

        if (
            index < 0 ||
            index >=
            state.pageCount ||
            index ==
            state.currentPageIndex
        ) {
            return
        }

        _uiState.update {
            it.copy(
                currentPageIndex =
                    index,

                /*
                 * Questions 是 page-scoped。
                 *
                 * 切页以后不能继续显示上一页的问题。
                 */
                questions =
                    emptyList(),

                questionsLoading =
                    false,

                questionsError =
                    null,

                questionSubmitError =
                    null,

                questionActionId =
                    null,

                questionActionError =
                    null,
            )
        }
    }

    private fun load() {
        viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isLoading =
                        true,

                    detail =
                        null,

                    currentPageIndex =
                        0,

                    isUnavailable =
                        false,

                    errorMessage =
                        null,

                    questionsLoading =
                        false,

                    questions =
                        emptyList(),

                    questionsError =
                        null,

                    submittingQuestion =
                        false,

                    questionSubmitError =
                        null,

                    questionActionId =
                        null,

                    questionActionError =
                        null,
                )
            }

            repository
                .loadDetail(
                    slideId =
                        slideId,
                )
                .onSuccess { result ->

                    if (result == null) {
                        _uiState.update {
                            it.copy(
                                isLoading =
                                    false,

                                detail =
                                    null,

                                currentPageIndex =
                                    0,

                                isUnavailable =
                                    true,

                                errorMessage =
                                    null,
                            )
                        }

                        return@onSuccess
                    }

                    _uiState.update {
                        it.copy(
                            isLoading =
                                false,

                            detail =
                                result,

                            currentPageIndex =
                                0,

                            isUnavailable =
                                false,

                            errorMessage =
                                null,
                        )
                    }
                }
                .onFailure { error ->

                    error.printStackTrace()

                    _uiState.update {
                        it.copy(
                            isLoading =
                                false,

                            detail =
                                null,

                            currentPageIndex =
                                0,

                            isUnavailable =
                                false,

                            errorMessage =
                                error.message
                                    ?: "演示加载失败",
                        )
                    }
                }
        }
    }

    private fun questionFallbackMessage(
        code: String?,
    ): String {
        return when (code) {
            "SLIDE_NOT_FOUND" ->
                "演示不存在。"

            "SLIDE_NOT_READY" ->
                "演示暂不可提问。"

            "SLIDE_PRIVATE" ->
                "该演示暂不可提问。"

            "EMPTY_QUESTION" ->
                "请输入问题。"

            "QUESTION_TOO_LONG" ->
                "问题太长，请控制在 500 字以内。"

            "TOO_MANY_QUESTIONS" ->
                "提问太频繁，请稍后再试。"

            "APP_NOT_AUTHENTICATED" ->
                "请先登录后再提问。"

            "FORBIDDEN" ->
                "你没有处理该问题的权限。"

            "QUESTION_NOT_FOUND" ->
                "问题不存在。"

            "QUESTION_NOT_IN_SLIDE" ->
                "问题不属于当前演示。"

            "QUESTION_HIDDEN" ->
                "该问题已隐藏。"

            else ->
                "操作失败，请稍后再试。"
        }
    }
}

class SlideDetailViewModelFactory(
    private val slideId: String,
    private val repository: SlideDetailRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
    ): T {

        if (
            modelClass.isAssignableFrom(
                SlideDetailViewModel::class.java
            )
        ) {
            return SlideDetailViewModel(
                slideId =
                    slideId,

                repository =
                    repository,
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}