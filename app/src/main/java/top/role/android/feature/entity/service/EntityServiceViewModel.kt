package top.role.android.feature.entity.service

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EntityServiceUiState(
    val mode: String = "mine",
    val isLoading: Boolean = true,
    val services: List<EntityServiceItem> = emptyList(),
    val errorMessage: String? = null,
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    val createdServiceId: String? = null,
    val selectedServiceId: String? = null,
    val serviceDetail: EntityServiceItem? = null,
    val isDetailLoading: Boolean = false,
    val detailErrorMessage: String? = null,
    val isActionRunning: Boolean = false,
    val actionErrorMessage: String? = null,
)

class EntityServiceViewModel(
    private val spaceId: String,
    initialMode: String,
    private val repository: EntityServiceRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        EntityServiceUiState(
            mode = normalizeMode(initialMode),
        )
    )

    val uiState: StateFlow<EntityServiceUiState> =
        _uiState.asStateFlow()

    private var subscriptionJob: Job? = null
    private var detailSubscriptionJob: Job? = null

    private var started = false

    /**
     * 认证就绪后开始订阅。
     */

    fun start() {
        if (started) return

        started = true
        observeServices()

        _uiState.value.selectedServiceId?.let { serviceId ->
            observeDetail(serviceId)
        }
    }

    /**
     * 认证失效或停止远端订阅。
     */

    fun stop() {
        started = false

        subscriptionJob?.cancel()
        subscriptionJob = null

        detailSubscriptionJob?.cancel()
        detailSubscriptionJob = null

        _uiState.update {
            it.copy(
                isLoading = false,
                services = emptyList(),
                serviceDetail = null,
                isDetailLoading = false,
            )
        }
    }

    fun setUnavailable(message: String) {
        stop()

        _uiState.update {
            it.copy(
                errorMessage = message,
                detailErrorMessage = message,
            )
        }
    }

    /**
     * 切换「我的请求 / 收到的请求」。
     *
     * 这里只决定调用哪个查询，
     * 不代表获得管理权限。
     */
    fun setMode(mode: String) {
        val normalized = normalizeMode(mode)

        if (_uiState.value.mode == normalized) {
            return
        }

        _uiState.update {
            it.copy(
                mode = normalized,
                isLoading = started,
                services = emptyList(),
                errorMessage = null,
            )
        }

        if (started) {
            observeServices()
        }
    }

    fun retry() {
        if (started) {
            observeServices()
        }
    }

    private fun observeServices() {
        subscriptionJob?.cancel()

        val mode = _uiState.value.mode

        android.util.Log.e(
            "EntityService",
            "observeServices START spaceId=$spaceId mode=$mode"
        )

        _uiState.update {
            it.copy(
                isLoading = true,
                services = emptyList(),
                errorMessage = null,
            )
        }

        subscriptionJob = viewModelScope.launch {
            try {
                val flow = if (mode == "managed") {
                    repository.observeManagedServices(
                        spaceId = spaceId,
                    )
                } else {
                    repository.observeMyServices(
                        spaceId = spaceId,
                    )
                }


                flow.collect { result ->

                    android.util.Log.e(
                        "EntityService",
                        "subscription result: " +
                                "success=${result.isSuccess}, " +
                                "error=${result.exceptionOrNull()?.message}"
                    )

                    result.fold(
                        onSuccess = { response ->

                            android.util.Log.e(
                                "EntityService",
                                "response ok=${response.ok}, " +
                                        "count=${response.services.size}, " +
                                        "code=${response.code}"
                            )

                            if (response.ok) {
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        services = response.services,
                                        errorMessage = null,
                                    )
                                }
                            } else {
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        services = emptyList(),
                                        errorMessage =
                                            serviceErrorMessage(response.code),
                                    )
                                }
                            }
                        },

                        onFailure = { error ->

                            android.util.Log.e(
                                "EntityService",
                                "subscription failed",
                                error,
                            )

                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage =
                                        error.message
                                            ?: "服务请求加载失败",
                                )
                            }
                        },
                    )
                }

            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                error.printStackTrace()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage =
                            error.message
                                ?: "服务请求加载失败",
                    )
                }
            }
        }
    }

    fun submitService(
        requestType: String,
        title: String,
        contextSummary: String,
        coreQuestion: String,
        expectedHelp: String,
        references: List<EntityServiceReference> = emptyList(),
    ) {
        if (_uiState.value.isSubmitting) return

        val normalizedTitle = title.trim()
        val normalizedContext = contextSummary.trim()
        val normalizedQuestion = coreQuestion.trim()
        val normalizedHelp = expectedHelp.trim()

        if (
            normalizedTitle.isEmpty() ||
            normalizedContext.isEmpty() ||
            normalizedQuestion.isEmpty() ||
            normalizedHelp.isEmpty()
        ) {
            _uiState.update {
                it.copy(
                    submitError = "请填写完整的请求信息",
                )
            }
            return
        }

        if (references.size > 20) {
            _uiState.update {
                it.copy(
                    submitError = "最多选择 20 条参考内容",
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSubmitting = true,
                    submitError = null,
                    createdServiceId = null,
                )
            }

            try {

                val response = repository.submitService(
                    spaceId = spaceId,
                    requestType = requestType,
                    title = normalizedTitle,
                    contextSummary = normalizedContext,
                    coreQuestion = normalizedQuestion,
                    expectedHelp = normalizedHelp,
                    references = references,
                )

                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        submitError = if (response.ok) {
                            null
                        } else {
                            serviceSubmitErrorMessage(response.code)
                        },
                        createdServiceId = if (response.ok) {
                            response.serviceId
                        } else {
                            null
                        },
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                error.printStackTrace()

                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        submitError =
                            error.message ?: "提交失败，请稍后重试",
                    )
                }
            }
        }
    }

    fun consumeCreatedService() {
        _uiState.update {
            it.copy(
                createdServiceId = null,
                submitError = null,
            )
        }
    }

    fun clearSubmitError() {
        _uiState.update {
            it.copy(
                submitError = null,
            )
        }
    }


    fun openDetail(serviceId: String) {
        val normalizedId = serviceId.trim()
        if (normalizedId.isEmpty()) return

        if (_uiState.value.selectedServiceId == normalizedId) {
            return
        }

        _uiState.update {
            it.copy(
                selectedServiceId = normalizedId,
                serviceDetail = null,
                isDetailLoading = started,
                detailErrorMessage = if (started) {
                    null
                } else {
                    "当前无法加载请求详情"
                },
            )
        }

        if (started) {
            observeDetail(normalizedId)
        }
    }

    fun closeDetail() {
        detailSubscriptionJob?.cancel()
        detailSubscriptionJob = null

        _uiState.update {
            it.copy(
                selectedServiceId = null,
                serviceDetail = null,
                isDetailLoading = false,
                detailErrorMessage = null,
            )
        }
    }

    fun retryDetail() {
        val serviceId =
            _uiState.value.selectedServiceId ?: return

        if (started) {
            observeDetail(serviceId)
        }
    }


    private fun observeDetail(serviceId: String) {
        detailSubscriptionJob?.cancel()

        _uiState.update {
            it.copy(
                isDetailLoading = true,
                serviceDetail = null,
                detailErrorMessage = null,
            )
        }

        detailSubscriptionJob = viewModelScope.launch {
            try {
                repository
                    .observeServiceDetail(serviceId)
                    .collect { result ->

                        // 避免旧请求的订阅结果覆盖新详情。
                        if (
                            _uiState.value.selectedServiceId != serviceId
                        ) {
                            return@collect
                        }

                        result.fold(
                            onSuccess = { response ->
                                if (
                                    response.ok &&
                                    response.service != null
                                ) {
                                    _uiState.update {
                                        it.copy(
                                            isDetailLoading = false,
                                            serviceDetail =
                                                response.service,
                                            detailErrorMessage = null,
                                        )
                                    }
                                } else {
                                    _uiState.update {
                                        it.copy(
                                            isDetailLoading = false,
                                            serviceDetail = null,
                                            detailErrorMessage =
                                                serviceErrorMessage(
                                                    response.code
                                                ),
                                        )
                                    }
                                }
                            },

                            onFailure = { error ->
                                error.printStackTrace()

                                _uiState.update {
                                    it.copy(
                                        isDetailLoading = false,
                                        serviceDetail = null,
                                        detailErrorMessage =
                                            error.message
                                                ?: "请求详情加载失败",
                                    )
                                }
                            },
                        )
                    }

            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                error.printStackTrace()

                _uiState.update {
                    it.copy(
                        isDetailLoading = false,
                        serviceDetail = null,
                        detailErrorMessage =
                            error.message ?: "请求详情加载失败",
                    )
                }
            }
        }
    }

    fun withdrawService() {
        val currentState = _uiState.value
        val service = currentState.serviceDetail ?: return

        if (currentState.isActionRunning) return
        if (service.isRequester != true) return

        if (!EntityServiceStatuses.canWithdraw(service.status)) {
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isActionRunning = true,
                    actionErrorMessage = null,
                )
            }

            try {
                val response = repository.withdrawService(
                    serviceId = service.id,
                )

                _uiState.update {
                    it.copy(
                        isActionRunning = false,
                        actionErrorMessage = if (response.ok) {
                            null
                        } else {
                            when (response.code) {
                                "ACCEPTED_SERVICE_CANNOT_WITHDRAW" ->
                                    "已受理的请求不能撤回"

                                "SPACE_SERVICE_ALREADY_FINISHED" ->
                                    "请求已经结束"

                                "ONLY_REQUESTER_CAN_WITHDRAW" ->
                                    "只有请求发起人可以撤回"

                                else ->
                                    "撤回失败：${response.code ?: "未知错误"}"
                            }
                        },
                    )
                }

                // 成功后等待原有 Convex 详情订阅更新状态。
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isActionRunning = false,
                        actionErrorMessage =
                            error.message ?: "撤回失败，请稍后重试",
                    )
                }
            }
        }
    }


    fun markInReview() {
        runManagedAction(
            canRun = EntityServiceStatuses::canReview,
        ) { serviceId ->
            repository.markInReview(serviceId)
        }
    }

    fun acceptService() {
        runManagedAction(
            canRun = EntityServiceStatuses::canAccept,
        ) { serviceId ->
            repository.acceptService(serviceId)
        }
    }

    fun declineService(responseNote: String) {
        runManagedAction(
            canRun = EntityServiceStatuses::canDecline,
        ) { serviceId ->
            repository.declineService(
                serviceId = serviceId,
                responseNote = responseNote,
            )
        }
    }


    fun closeService(
        resultRef: EntityServiceReference?,
        responseNote: String,
    ) {
        val normalizedNote = responseNote.trim()

        if (
            resultRef == null &&
            normalizedNote.isEmpty()
        ) {
            _uiState.update {
                it.copy(
                    actionErrorMessage =
                        "请填写回复说明或选择交付内容",
                )
            }
            return
        }

        runManagedAction(
            canRun = EntityServiceStatuses::canClose,
        ) { serviceId ->
            repository.closeService(
                serviceId = serviceId,
                resultRef = resultRef,
                responseNote = normalizedNote,
            )
        }
    }

    private fun runManagedAction(
        canRun: (String) -> Boolean,
        action: suspend (String) -> EntityServiceActionResponse,
    ) {
        val current = _uiState.value
        val service = current.serviceDetail ?: return

        if (!started) return
        if (current.mode != "managed") return
        if (current.isActionRunning) return
        if (!canRun(service.status)) return

        val serviceId = service.id

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isActionRunning = true,
                    actionErrorMessage = null,
                )
            }

            try {
                val response = action(serviceId)

                _uiState.update {
                    if (it.selectedServiceId != serviceId) {
                        it.copy(isActionRunning = false)
                    } else {
                        it.copy(
                            isActionRunning = false,
                            actionErrorMessage =
                                if (response.ok) null
                                else serviceActionErrorMessage(
                                    response.code
                                ),
                        )
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                error.printStackTrace()

                _uiState.update {
                    it.copy(
                        isActionRunning = false,
                        actionErrorMessage =
                            error.message
                                ?: "操作失败，请稍后重试",
                    )
                }
            }
        }
    }

    fun clearActionError() {
        _uiState.update {
            it.copy(actionErrorMessage = null)
        }
    }

    override fun onCleared() {
        subscriptionJob?.cancel()
        detailSubscriptionJob?.cancel()
        super.onCleared()
    }

}

private fun normalizeMode(mode: String): String =
    if (mode == "managed") "managed" else "mine"

private fun serviceErrorMessage(code: String?): String =
    when (code) {
        "SPACE_NOT_FOUND" ->
            "未找到空间"

        "SPACE_NOT_ACTIVE" ->
            "空间未激活"

        "SPACE_MEMBERSHIP_NOT_ENABLED" ->
            "该空间暂未开启会员服务"

        "SPACE_MEMBER_REQUIRED" ->
            "需要空间会员资格"

        "SPACE_MEMBERSHIP_EXPIRED" ->
            "空间会员已过期"

        else ->
            "无法加载服务请求${code?.let { "：$it" } ?: ""}"
    }


private fun serviceSubmitErrorMessage(
    code: String?,
): String = when (code) {
    "SPACE_NOT_FOUND" ->
        "空间不存在"

    "SPACE_NOT_ACTIVE" ->
        "空间尚未激活"

    "SPACE_MEMBERSHIP_NOT_ENABLED" ->
        "该空间尚未开启会员服务"

    "SPACE_MEMBER_REQUIRED" ->
        "需要加入空间会员后才能发起请求"

    "SPACE_MEMBERSHIP_EXPIRED" ->
        "你的空间会员已过期"

    "TITLE_REQUIRED" ->
        "请填写标题"

    "CONTEXT_SUMMARY_REQUIRED" ->
        "请填写背景说明"

    "CORE_QUESTION_REQUIRED" ->
        "请填写核心问题"

    "EXPECTED_HELP_REQUIRED" ->
        "请填写期望帮助"

    else ->
        "提交失败${code?.let { "：$it" } ?: ""}"
}

private fun serviceActionErrorMessage(
    code: String?,
): String = when (code) {
    "SPACE_SERVICE_ALREADY_FINISHED" ->
        "请求已经结束"

    "SPACE_SERVICE_ALREADY_ACCEPTED" ->
        "请求已经受理"

    "SPACE_SERVICE_STATUS_NOT_EDITABLE" ->
        "当前状态不可修改"

    "SPACE_SERVICE_ALREADY_DECLINED" ->
        "请求已经拒绝"

    "SPACE_SERVICE_ALREADY_CLOSED" ->
        "请求已经关闭"

    "SPACE_SERVICE_DECLINED_CANNOT_CLOSE" ->
        "已拒绝的请求不能关闭"

    "CONTENT_REF_INVALID" ->
        "内容引用无效"

    "CONTENT_REF_UNAVAILABLE" ->
        "所选内容已不可用，请重新选择"

    "CONTENT_RESULT_TITLE_REQUIRED" ->
        "用于正式回复的 Note 必须有标题"

    else ->
        "操作失败${code?.let { "：$it" } ?: ""}"
}

class EntityServiceViewModelFactory(
    private val spaceId: String,
    private val initialMode: String,
    private val repository: EntityServiceRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
    ): T {
        if (
            modelClass.isAssignableFrom(
                EntityServiceViewModel::class.java
            )
        ) {
            return EntityServiceViewModel(
                spaceId = spaceId,
                initialMode = initialMode,
                repository = repository,
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}