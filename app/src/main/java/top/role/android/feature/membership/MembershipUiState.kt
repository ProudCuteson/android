package top.role.android.feature.membership

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import top.role.android.core.model.membership.MobileMembershipPageResponse
import top.role.android.core.payment.WechatPayLauncher
import top.role.android.core.payment.WechatPayResultBus

data class MembershipUiState(
    val loading: Boolean = true,
    val loadError: String? = null,

    val page: MobileMembershipPageResponse? = null,
    val selectedPlanKey: String? = null,

    val paying: Boolean = false,
    val confirming: Boolean = false,

    val paymentError: String? = null,
    val paymentSuccess: Boolean = false,
)

class MembershipViewModel(
    private val spaceId: String,
    private val repository: MembershipRepository,
    private val wechatPayLauncher: WechatPayLauncher,
) : ViewModel() {

    companion object {
        private val STATUS_POLL_INTERVAL = 800.milliseconds
        private const val STATUS_POLL_MAX_ATTEMPTS = 10
    }

    private val _uiState =
        MutableStateFlow(
            MembershipUiState()
        )

    val uiState:
            StateFlow<MembershipUiState> =
        _uiState.asStateFlow()

    private var pageJob: Job? = null
    private var paymentResultJob: Job? = null

    private var pendingOutTradeNo: String? = null

    init {
        observePage()
        observeWechatPaymentResult()
    }

    private fun observePage() {
        pageJob?.cancel()

        pageJob =
            viewModelScope.launch {
                repository
                    .observePage(
                        spaceId = spaceId,
                    )
                    .collectLatest { result ->
                        result.fold(
                            onSuccess = { response ->
                                if (!response.ok) {
                                    _uiState.value =
                                        _uiState.value.copy(
                                            loading = false,
                                            loadError =
                                                response.code
                                                    ?: "MEMBERSHIP_LOAD_FAILED",
                                        )

                                    return@fold
                                }

                                val firstPlanKey =
                                    response
                                        .membership
                                        ?.plans
                                        ?.firstOrNull()
                                        ?.key

                                val currentSelected =
                                    _uiState.value
                                        .selectedPlanKey

                                val stillExists =
                                    response
                                        .membership
                                        ?.plans
                                        ?.any {
                                            it.key ==
                                                    currentSelected
                                        } == true

                                _uiState.value =
                                    _uiState.value.copy(
                                        loading = false,
                                        loadError = null,
                                        page = response,
                                        selectedPlanKey =
                                            if (stillExists) {
                                                currentSelected
                                            } else {
                                                firstPlanKey
                                            },
                                    )
                            },

                            onFailure = { error ->
                                _uiState.value =
                                    _uiState.value.copy(
                                        loading = false,
                                        loadError =
                                            error.message
                                                ?: "MEMBERSHIP_LOAD_FAILED",
                                    )
                            },
                        )
                    }
            }
    }

    fun retry() {
        _uiState.value =
            _uiState.value.copy(
                loading = true,
                loadError = null,
            )

        observePage()
    }

    fun selectPlan(
        planKey: String,
    ) {
        val state =
            _uiState.value

        if (
            state.paying ||
            state.confirming
        ) {
            return
        }

        val exists =
            state.page
                ?.membership
                ?.plans
                ?.any {
                    it.key == planKey
                } == true

        if (!exists) {
            return
        }

        _uiState.value =
            state.copy(
                selectedPlanKey = planKey,
                paymentError = null,
            )
    }

    fun purchase() {
        val state =
            _uiState.value

        if (
            state.paying ||
            state.confirming
        ) {
            return
        }

        val page =
            state.page
                ?: return

        val membership =
            page.membership
                ?: return

        val viewer =
            page.viewer
                ?: return

        if (!membership.enabled) {
            setPaymentError(
                "MEMBERSHIP_NOT_ENABLED"
            )
            return
        }

        if (viewer.isManager) {
            setPaymentError(
                "ALREADY_SPACE_MANAGER"
            )
            return
        }

        if (
            viewer.isPermanentMember
        ) {
            setPaymentError(
                "ALREADY_PERMANENT_MEMBER"
            )
            return
        }

        val planKey =
            state.selectedPlanKey

        if (planKey.isNullOrBlank()) {
            setPaymentError(
                "PLAN_NOT_SELECTED"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value =
                _uiState.value.copy(
                    paying = true,
                    confirming = false,
                    paymentError = null,
                    paymentSuccess = false,
                )

            val result =
                repository.createPurchase(
                    spaceId = spaceId,
                    planKey = planKey,
                )

            result.fold(
                onSuccess = { response ->
                    val purchase =
                        response.purchase

                    val payment =
                        response.payment

                    if (
                        purchase == null ||
                        payment == null
                    ) {
                        _uiState.value =
                            _uiState.value.copy(
                                paying = false,
                                paymentError =
                                    "INVALID_PURCHASE_RESPONSE",
                            )

                        return@fold
                    }

                    pendingOutTradeNo =
                        purchase.outTradeNo

                    val launchResult =
                        wechatPayLauncher.launch(
                            payment
                        )

                    launchResult.exceptionOrNull()
                        ?.let { error ->
                            pendingOutTradeNo = null

                            _uiState.value =
                                _uiState.value.copy(
                                    paying = false,
                                    paymentError =
                                        error.message
                                            ?: "WECHAT_PAY_LAUNCH_FAILED",
                                )
                        }
                },

                onFailure = { error ->
                    pendingOutTradeNo = null

                    _uiState.value =
                        _uiState.value.copy(
                            paying = false,
                            paymentError =
                                error.message
                                    ?: "CREATE_PURCHASE_FAILED",
                        )
                },
            )
        }
    }

    private fun observeWechatPaymentResult() {
        paymentResultJob?.cancel()

        paymentResultJob =
            viewModelScope.launch {
                WechatPayResultBus
                    .results
                    .collect { result ->
                        val outTradeNo =
                            pendingOutTradeNo
                                ?: return@collect

                        when (result.errCode) {
                            0 -> {
                                _uiState.value =
                                    _uiState.value.copy(
                                        paying = false,
                                        confirming = true,
                                        paymentError = null,
                                    )

                                confirmPurchase(
                                    outTradeNo
                                )
                            }

                            -2 -> {
                                pendingOutTradeNo = null

                                _uiState.value =
                                    _uiState.value.copy(
                                        paying = false,
                                        confirming = false,
                                        paymentError = null,
                                    )
                            }

                            else -> {
                                pendingOutTradeNo = null

                                _uiState.value =
                                    _uiState.value.copy(
                                        paying = false,
                                        confirming = false,
                                        paymentError =
                                            result.errStr
                                                ?.takeIf {
                                                    it.isNotBlank()
                                                }
                                                ?: "WECHAT_PAY_FAILED",
                                    )
                            }
                        }
                    }
            }
    }

    private suspend fun confirmPurchase(
        outTradeNo: String,
    ) {
        repeat(
            STATUS_POLL_MAX_ATTEMPTS
        ) { attempt ->

            if (attempt > 0) {
                delay(
                    STATUS_POLL_INTERVAL
                )
            }

            val result =
                repository.getPurchaseStatus(
                    outTradeNo =
                        outTradeNo,
                )

            val status =
                result.getOrNull()
                    ?: return@repeat

            if (
                !status.ok ||
                !status.found
            ) {
                return@repeat
            }

            when (status.status) {
                "settled" -> {
                    pendingOutTradeNo = null

                    _uiState.value =
                        _uiState.value.copy(
                            paying = false,
                            confirming = false,
                            paymentSuccess = true,
                            paymentError = null,
                        )

                    /*
                     * Membership 已经激活，
                     * 页面 subscription 本身通常也会更新。
                     * retry/重新订阅不是必须的。
                     */
                    return
                }

                "failed",
                "canceled" -> {
                    pendingOutTradeNo = null

                    _uiState.value =
                        _uiState.value.copy(
                            paying = false,
                            confirming = false,
                            paymentError =
                                status.failureCode
                                    ?: "MEMBERSHIP_PAYMENT_NOT_SETTLED",
                        )

                    return
                }
            }
        }

        /*
         * 微信已经返回，但 notify / settle
         * 在短轮询窗口内还没有完成。
         *
         * 这不是“支付失败”。
         */
        pendingOutTradeNo = null

        _uiState.value =
            _uiState.value.copy(
                paying = false,
                confirming = false,
                paymentError =
                    "MEMBERSHIP_PAYMENT_CONFIRMING",
            )
    }

    fun consumePaymentSuccess() {
        _uiState.value =
            _uiState.value.copy(
                paymentSuccess = false,
            )
    }

    fun clearPaymentError() {
        _uiState.value =
            _uiState.value.copy(
                paymentError = null,
            )
    }

    private fun setPaymentError(
        code: String,
    ) {
        _uiState.value =
            _uiState.value.copy(
                paymentError = code,
            )
    }
}

class MembershipViewModelFactory(
    private val spaceId: String,
    private val repository: MembershipRepository,
    private val wechatPayLauncher: WechatPayLauncher,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
    ): T {

        if (
            modelClass.isAssignableFrom(
                MembershipViewModel::class.java
            )
        ) {
            return MembershipViewModel(
                spaceId = spaceId,
                repository = repository,
                wechatPayLauncher = wechatPayLauncher,
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}