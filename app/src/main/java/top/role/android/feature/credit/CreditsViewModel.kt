package top.role.android.feature.credit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import kotlin.time.Duration.Companion.milliseconds

import top.role.android.core.model.account.CreateCreditPurchaseRequest
import top.role.android.core.model.account.MobileCreditPurchaseOption
import top.role.android.core.model.account.MobileRecentCreditPurchase
import top.role.android.core.payment.WechatPayLauncher
import top.role.android.core.payment.WechatPayResultBus


class CreditsViewModel(
    private val repository:
    CreditsRepository,

    private val wechatPayLauncher:
    WechatPayLauncher,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            CreditsUiState()
        )

    val uiState:
            StateFlow<CreditsUiState> =
        _uiState.asStateFlow()


    private var paymentResultJob:
            Job? = null

    private var pendingOutTradeNo:
            String? = null


    init {
        observePurchaseOptions()
        observeRecentPurchases()
        observeWechatPaymentResult()
    }


    private fun observePurchaseOptions() {

        viewModelScope.launch {

            repository
                .observePurchaseOptions()
                .collect { result ->

                    result
                        .onSuccess { response ->

                            _uiState.update {
                                it.copy(
                                    loading =
                                        false,

                                    packs =
                                        response.packs,

                                    custom =
                                        response.custom,

                                    errorMessage =
                                        null,
                                )
                            }
                        }
                        .onFailure {

                            _uiState.update {
                                it.copy(
                                    loading =
                                        false,

                                    errorMessage =
                                        "暂时无法加载 Credits 购买选项。",
                                )
                            }
                        }
                }
        }
    }


    private fun observeRecentPurchases() {

        viewModelScope.launch {

            repository
                .observeRecentPurchases()
                .collect { result ->

                    result
                        .onSuccess { response ->

                            _uiState.update {
                                it.copy(
                                    recentPurchases =
                                        response.items,

                                    recentLoading =
                                        false,
                                )
                            }
                        }
                        .onFailure {

                            /*
                             * 最近购买加载失败不阻塞
                             * Credits 购买页面。
                             */
                            _uiState.update {
                                it.copy(
                                    recentLoading =
                                        false,
                                )
                            }
                        }
                }
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

                        when (
                            result.errCode
                        ) {

                            /*
                             * 微信客户端返回成功。
                             *
                             * 这里不能直接认为 Credits
                             * 已经到账。
                             *
                             * 还需要查询后端 billingIntent
                             * 是否已经 settled。
                             */
                            0 -> {

                                _uiState.value =
                                    _uiState.value.copy(
                                        paying =
                                            false,

                                        confirming =
                                            true,

                                        purchaseErrorMessage =
                                            null,
                                    )

                                confirmPurchase(
                                    outTradeNo
                                )
                            }


                            /*
                             * 用户取消支付。
                             *
                             * 不作为错误展示。
                             */
                            -2 -> {

                                pendingOutTradeNo =
                                    null

                                _uiState.value =
                                    _uiState.value.copy(
                                        purchasingPackKey =
                                            null,

                                        paying =
                                            false,

                                        confirming =
                                            false,

                                        purchaseErrorMessage =
                                            null,
                                    )
                            }


                            /*
                             * 微信支付失败。
                             */
                            else -> {

                                pendingOutTradeNo =
                                    null

                                _uiState.value =
                                    _uiState.value.copy(
                                        purchasingPackKey =
                                            null,

                                        paying =
                                            false,

                                        confirming =
                                            false,

                                        purchaseErrorMessage =
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


    fun updateCustomAmount(
        value:
        String,
    ) {

        val state =
            _uiState.value

        if (
            state.paying ||
            state.confirming
        ) {
            return
        }

        _uiState.update {
            it.copy(
                customAmountText =
                    value,

                purchaseErrorMessage =
                    null,
            )
        }
    }


    fun purchaseFixedPack(
        pack:
        MobileCreditPurchaseOption,
    ) {

        createPurchase(
            request =
                CreateCreditPurchaseRequest(
                    packKey =
                        pack.packKey,
                ),
        )
    }


    fun purchaseCustomPack() {

        val state =
            _uiState.value

        if (
            state.paying ||
            state.confirming
        ) {
            return
        }

        val config =
            state.custom
                ?: return

        if (
            !config.enabled
        ) {
            return
        }

        val amountMinor =
            parseCustomAmountMinor(
                state.customAmountText
            )
                ?: run {

                    setPurchaseError(
                        "请输入有效的充值金额。"
                    )

                    return
                }


        if (
            amountMinor <
            config.minAmountMinor
        ) {

            setPurchaseError(
                "充值金额低于最低限制。"
            )

            return
        }


        if (
            amountMinor >
            config.maxAmountMinor
        ) {

            setPurchaseError(
                "充值金额超过最高限制。"
            )

            return
        }


        createPurchase(
            request =
                CreateCreditPurchaseRequest(
                    packKey =
                        "custom",

                    customAmountMinor =
                        amountMinor,
                ),
        )
    }


    private fun createPurchase(
        request:
        CreateCreditPurchaseRequest,
    ) {

        val state =
            _uiState.value

        if (
            state.paying ||
            state.confirming ||
            state.purchasingPackKey != null
        ) {
            return
        }


        viewModelScope.launch {

            _uiState.value =
                _uiState.value.copy(
                    purchasingPackKey =
                        request.packKey,

                    paying =
                        true,

                    confirming =
                        false,

                    paymentSuccess =
                        false,

                    purchaseErrorMessage =
                        null,
                )


            val result =
                repository.createPurchase(
                    request
                )


            result.fold(

                onSuccess = { response ->

                    val purchase =
                        response.purchase

                    val payment =
                        response.payment


                    if (
                        !response.ok ||
                        purchase == null ||
                        payment == null
                    ) {

                        pendingOutTradeNo =
                            null

                        _uiState.value =
                            _uiState.value.copy(
                                purchasingPackKey =
                                    null,

                                paying =
                                    false,

                                confirming =
                                    false,

                                purchaseErrorMessage =
                                    response.message
                                        ?: creditPurchaseErrorMessage(
                                            response.code
                                        ),
                            )

                        return@fold
                    }


                    pendingOutTradeNo =
                        purchase.outTradeNo


                    val launchResult =
                        wechatPayLauncher.launch(
                            payment
                        )


                    launchResult
                        .exceptionOrNull()
                        ?.let { error ->

                            pendingOutTradeNo =
                                null

                            _uiState.value =
                                _uiState.value.copy(
                                    purchasingPackKey =
                                        null,

                                    paying =
                                        false,

                                    confirming =
                                        false,

                                    purchaseErrorMessage =
                                        error.message
                                            ?: "WECHAT_PAY_LAUNCH_FAILED",
                                )
                        }
                },


                onFailure = { error ->

                    pendingOutTradeNo =
                        null

                    _uiState.value =
                        _uiState.value.copy(
                            purchasingPackKey =
                                null,

                            paying =
                                false,

                            confirming =
                                false,

                            purchaseErrorMessage =
                                error.message
                                    ?: "CREATE_CREDIT_PURCHASE_FAILED",
                        )
                },
            )
        }
    }


    private suspend fun confirmPurchase(
        outTradeNo:
        String,
    ) {

        repeat(
            STATUS_POLL_MAX_ATTEMPTS
        ) { attempt ->

            if (
                attempt > 0
            ) {
                delay(
                    STATUS_POLL_INTERVAL
                )
            }


            val result =
                repository
                    .getPurchaseStatus(
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


            when (
                status.status
            ) {

                /*
                 * billingIntent 已经完成结算，
                 * Credits 已实际写入账户。
                 */
                "settled" -> {

                    pendingOutTradeNo =
                        null

                    _uiState.value =
                        _uiState.value.copy(
                            purchasingPackKey =
                                null,

                            paying =
                                false,

                            confirming =
                                false,

                            paymentSuccess =
                                true,

                            purchaseErrorMessage =
                                null,

                            customAmountText =
                                "",
                        )

                    return
                }


                /*
                 * paid:
                 *
                 * 微信已经付款，但 credits
                 * settlement 尚未完成。
                 *
                 * 继续轮询。
                 */
                "paid",
                "pending" -> {
                    // continue polling
                }


                "failed",
                "canceled" -> {

                    pendingOutTradeNo =
                        null

                    _uiState.value =
                        _uiState.value.copy(
                            purchasingPackKey =
                                null,

                            paying =
                                false,

                            confirming =
                                false,

                            purchaseErrorMessage =
                                status.failureCode
                                    ?: "CREDIT_PAYMENT_NOT_SETTLED",
                        )

                    return
                }
            }
        }


        /*
         * 微信客户端已经返回成功，
         * 但是微信 notify / Credits settlement
         * 在当前短轮询窗口内尚未完成。
         *
         * 这里不能显示“支付失败”。
         */
        pendingOutTradeNo =
            null

        _uiState.value =
            _uiState.value.copy(
                purchasingPackKey =
                    null,

                paying =
                    false,

                confirming =
                    false,

                purchaseErrorMessage =
                    "CREDIT_PAYMENT_CONFIRMING",
            )
    }


    fun consumePaymentSuccess() {

        _uiState.value =
            _uiState.value.copy(
                paymentSuccess =
                    false,
            )
    }


    fun clearPurchaseError() {

        _uiState.value =
            _uiState.value.copy(
                purchaseErrorMessage =
                    null,
            )
    }

    fun continuePurchase(
        purchase:
        MobileRecentCreditPurchase,
    ) {
        val state =
            _uiState.value

        if (
            state.paying ||
            state.confirming ||
            state.purchasingPackKey != null
        ) {
            return
        }

        if (
            purchase.status !=
            "pending"
        ) {
            return
        }

        val dueAt =
            purchase.dueAt

        if (
            dueAt != null &&
            System.currentTimeMillis() >=
            dueAt.toLong()
        ) {
            setPurchaseError(
                "该购买订单已过期，请重新购买。"
            )

            return
        }

        continuePurchase(
            outTradeNo =
                purchase.outTradeNo,
        )
    }

    private fun continuePurchase(
        outTradeNo:
        String,
    ) {
        if (
            outTradeNo.isBlank()
        ) {
            return
        }

        viewModelScope.launch {

            _uiState.value =
                _uiState.value.copy(
                    purchasingPackKey =
                        "continue:$outTradeNo",

                    paying =
                        true,

                    confirming =
                        false,

                    paymentSuccess =
                        false,

                    purchaseErrorMessage =
                        null,
                )

            val result =
                repository.continuePurchase(
                    outTradeNo =
                        outTradeNo,
                )

            result.fold(
                onSuccess = { response ->

                    val purchase =
                        response.purchase

                    val payment =
                        response.payment

                    if (
                        !response.ok ||
                        purchase == null ||
                        payment == null
                    ) {
                        pendingOutTradeNo =
                            null

                        _uiState.value =
                            _uiState.value.copy(
                                purchasingPackKey =
                                    null,

                                paying =
                                    false,

                                confirming =
                                    false,

                                purchaseErrorMessage =
                                    response.message
                                        ?: continuePurchaseErrorMessage(
                                            response.code
                                        ),
                            )

                        return@fold
                    }

                    pendingOutTradeNo =
                        purchase.outTradeNo

                    val launchResult =
                        wechatPayLauncher.launch(
                            payment
                        )

                    launchResult
                        .exceptionOrNull()
                        ?.let { error ->

                            pendingOutTradeNo =
                                null

                            _uiState.value =
                                _uiState.value.copy(
                                    purchasingPackKey =
                                        null,

                                    paying =
                                        false,

                                    confirming =
                                        false,

                                    purchaseErrorMessage =
                                        error.message
                                            ?: "WECHAT_PAY_LAUNCH_FAILED",
                                )
                        }
                },

                onFailure = { error ->

                    pendingOutTradeNo =
                        null

                    _uiState.value =
                        _uiState.value.copy(
                            purchasingPackKey =
                                null,

                            paying =
                                false,

                            confirming =
                                false,

                            purchaseErrorMessage =
                                error.message
                                    ?: "CONTINUE_CREDIT_PURCHASE_FAILED",
                        )
                },
            )
        }
    }

    private fun setPurchaseError(
        message:
        String,
    ) {

        _uiState.value =
            _uiState.value.copy(
                purchaseErrorMessage =
                    message,
            )
    }


    private fun parseCustomAmountMinor(
        value:
        String,
    ): Double? {

        val yuan =
            value
                .trim()
                .toDoubleOrNull()
                ?: return null


        if (
            !yuan.isFinite() ||
            yuan <= 0.0
        ) {
            return null
        }


        return kotlin.math.round(
            yuan * 100.0
        )
    }


    private fun creditPurchaseErrorMessage(
        code:
        String?,
    ): String {

        return when (
            code
        ) {

            "INVALID_CUSTOM_AMOUNT" ->
                "请输入有效的充值金额。"

            "CUSTOM_AMOUNT_TOO_SMALL" ->
                "充值金额低于最低限制。"

            "CUSTOM_AMOUNT_TOO_LARGE" ->
                "充值金额超过最高限制。"

            "INVALID_CUSTOM_CREDITS" ->
                "无法计算本次充值 Credits。"

            "CREDIT_PACK_NOT_FOUND" ->
                "该充值档位暂不可用。"

            "UNSUPPORTED_CURRENCY" ->
                "当前币种暂不支持购买。"

            "USER_NOT_FOUND" ->
                "当前账户不可用，请重新登录。"

            else ->
                code
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?: "暂时无法创建 Credits 购买订单。"
        }
    }

    private fun continuePurchaseErrorMessage(
        code:
        String?,
    ): String {

        return when (code) {

            "BILLING_INTENT_EXPIRED" ->
                "该购买订单已过期，请重新购买。"

            "BILLING_INTENT_NOT_PENDING" ->
                "该购买订单当前无法继续支付。"

            "BILLING_INTENT_NOT_FOUND" ->
                "未找到该购买订单。"

            "PAYMENT_CHANNEL_MISMATCH" ->
                "该订单的支付方式不匹配。"

            "WECHAT_PREPAY_ID_MISSING" ->
                "暂时无法获取微信支付信息。"

            else ->
                code
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?: "暂时无法继续支付。"
        }
    }

    companion object {

        private val STATUS_POLL_INTERVAL =
            800.milliseconds

        private const val STATUS_POLL_MAX_ATTEMPTS =
            10


        fun factory(
            repository:
            CreditsRepository,

            wechatPayLauncher:
            WechatPayLauncher,
        ): ViewModelProvider.Factory {

            return object :
                ViewModelProvider.Factory {

                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass:
                    Class<T>,
                ): T {

                    if (
                        modelClass.isAssignableFrom(
                            CreditsViewModel::class.java
                        )
                    ) {
                        return CreditsViewModel(
                            repository =
                                repository,

                            wechatPayLauncher =
                                wechatPayLauncher,
                        ) as T
                    }

                    throw IllegalArgumentException(
                        "Unknown ViewModel class: ${modelClass.name}"
                    )
                }
            }
        }
    }

}