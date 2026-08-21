package top.role.android.feature.market.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import kotlin.time.Duration.Companion.milliseconds

import top.role.android.core.model.market.CreateMarketPurchaseRequest
import top.role.android.core.model.market.MarketShippingInfo
import top.role.android.core.model.market.MobileMarketDetail
import top.role.android.core.model.region.MarketRegionSelection
import top.role.android.core.payment.WechatPayLauncher
import top.role.android.core.payment.WechatPayResultBus


data class MarketDetailUiState(
    val loading:
    Boolean = false,

    val signedOut:
    Boolean = false,

    val listing:
    MobileMarketDetail? = null,

    val errorMessage:
    String? = null,

    /*
     * ------------------------------------------------
     * Purchase form
     * ------------------------------------------------
     */

    val quantity:
    Double = 1.0,

    val selectedRegion:
    MarketRegionSelection? = null,

    val receiverName:
    String = "",

    val receiverPhone:
    String = "",

    val addressLine1:
    String = "",

    val postalCode:
    String = "",

    val buyerNote:
    String = "",

    /*
     * ------------------------------------------------
     * Payment
     * ------------------------------------------------
     */

    val paying:
    Boolean = false,

    val confirming:
    Boolean = false,

    val paymentError:
    String? = null,

    val paymentSuccess:
    Boolean = false,

    val purchasedOrderId:
    String? = null,
)


class MarketDetailViewModel(
    private val listingId:
    String,

    private val repository:
    MarketDetailRepository,

    private val wechatPayLauncher:
    WechatPayLauncher,
) : ViewModel() {

    companion object {

        private val STATUS_POLL_INTERVAL =
            800.milliseconds

        private const val STATUS_POLL_MAX_ATTEMPTS =
            10
    }


    private val _uiState =
        MutableStateFlow(
            MarketDetailUiState()
        )

    val uiState:
            StateFlow<MarketDetailUiState> =
        _uiState.asStateFlow()


    private var observeJob:
            Job? = null

    private var paymentResultJob:
            Job? = null


    /*
     * 当前这一次微信支付关联的订单。
     */

    private var pendingOrderId:
            String? = null

    private var pendingOutTradeNo:
            String? = null


    init {
        observeWechatPaymentResult()
    }


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

        pendingOrderId =
            null

        pendingOutTradeNo =
            null

        _uiState.value =
            MarketDetailUiState(
                signedOut =
                    true,
            )
    }


    fun retry() {

        observeJob?.cancel()
        observeJob = null

        observeDetail()
    }


    /*
     * ------------------------------------------------
     * Detail
     * ------------------------------------------------
     */

    private fun observeDetail() {

        _uiState.value =
            _uiState.value.copy(
                loading =
                    true,

                signedOut =
                    false,

                errorMessage =
                    null,
            )

        observeJob =
            viewModelScope.launch {

                repository
                    .observeDetail(
                        listingId =
                            listingId,
                    )
                    .collect { result ->

                        result.fold(
                            onSuccess = {
                                    response ->

                                if (
                                    !response.ok ||
                                    response.listing ==
                                    null
                                ) {

                                    _uiState.value =
                                        _uiState.value.copy(
                                            loading =
                                                false,

                                            errorMessage =
                                                response.code
                                                    ?: "单品不可用",
                                        )

                                    return@fold
                                }


                                _uiState.value =
                                    _uiState.value.copy(
                                        loading =
                                            false,

                                        listing =
                                            response.listing,

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
                                                ?: "加载单品失败",
                                    )
                            },
                        )
                    }
            }
    }


    /*
     * ------------------------------------------------
     * Quantity
     * ------------------------------------------------
     */

    fun decreaseQuantity() {

        val state =
            _uiState.value

        if (
            state.paying ||
            state.confirming
        ) {
            return
        }

        if (
            state.quantity <=
            1.0
        ) {
            return
        }


        _uiState.value =
            state.copy(
                quantity =
                    state.quantity -
                            1.0,

                paymentError =
                    null,
            )
    }


    fun increaseQuantity() {

        val state =
            _uiState.value

        if (
            state.paying ||
            state.confirming
        ) {
            return
        }


        val listing =
            state.listing
                ?: return


        if (
            !listing.allowMultiQuantity
        ) {
            return
        }


        if (
            listing.stockMode !=
            "unlimited"
        ) {

            val stockCount =
                listing.stockCount

            if (
                stockCount !=
                null &&
                state.quantity >=
                stockCount
            ) {
                return
            }
        }


        _uiState.value =
            state.copy(
                quantity =
                    state.quantity +
                            1.0,

                paymentError =
                    null,
            )
    }


    /*
     * ------------------------------------------------
     * Shipping form
     * ------------------------------------------------
     */

    fun selectRegion(
        region:
        MarketRegionSelection,
    ) {

        _uiState.value =
            _uiState.value.copy(
                selectedRegion =
                    region,

                paymentError =
                    null,
            )
    }


    fun updateReceiverName(
        value:
        String,
    ) {

        _uiState.value =
            _uiState.value.copy(
                receiverName =
                    value,

                paymentError =
                    null,
            )
    }


    fun updateReceiverPhone(
        value:
        String,
    ) {

        _uiState.value =
            _uiState.value.copy(
                receiverPhone =
                    value,

                paymentError =
                    null,
            )
    }


    fun updateAddressLine1(
        value:
        String,
    ) {

        _uiState.value =
            _uiState.value.copy(
                addressLine1 =
                    value,

                paymentError =
                    null,
            )
    }


    fun updatePostalCode(
        value:
        String,
    ) {

        _uiState.value =
            _uiState.value.copy(
                postalCode =
                    value,

                paymentError =
                    null,
            )
    }


    fun updateBuyerNote(
        value:
        String,
    ) {

        _uiState.value =
            _uiState.value.copy(
                buyerNote =
                    value,

                paymentError =
                    null,
            )
    }


    /*
     * ------------------------------------------------
     * Purchase
     * ------------------------------------------------
     */

    fun purchase() {

        val state =
            _uiState.value


        if (
            state.paying ||
            state.confirming
        ) {
            return
        }


        val listing =
            state.listing
                ?: return


        /*
         * ------------------------------------------------
         * Quantity validation
         * ------------------------------------------------
         */

        if (
            state.quantity <
            1.0
        ) {

            _uiState.value =
                state.copy(
                    paymentError =
                        "INVALID_MARKET_QUANTITY",
                )

            return
        }


        if (
            listing.stockMode !=
            "unlimited"
        ) {

            val stockCount =
                listing.stockCount
                    ?: 0.0

            if (
                stockCount <=
                0.0 ||
                state.quantity >
                stockCount
            ) {

                _uiState.value =
                    state.copy(
                        paymentError =
                            "MARKET_STOCK_NOT_ENOUGH",
                    )

                return
            }
        }


        /*
         * ------------------------------------------------
         * Shipping validation
         * ------------------------------------------------
         */

        val shippingInfo =
            if (
                listing.needsShippingAddress
            ) {

                val region =
                    state.selectedRegion


                if (
                    state.receiverName
                        .isBlank() ||
                    state.receiverPhone
                        .isBlank() ||
                    region ==
                    null ||
                    state.addressLine1
                        .isBlank()
                ) {

                    _uiState.value =
                        state.copy(
                            paymentError =
                                "SHIPPING_ADDRESS_REQUIRED",
                        )

                    return
                }


                MarketShippingInfo(
                    receiverName =
                        state.receiverName
                            .trim(),

                    receiverPhone =
                        state.receiverPhone
                            .trim(),

                    regionText =
                        region.regionText,

                    addressLine1 =
                        state.addressLine1
                            .trim(),

                    postalCode =
                        state.postalCode
                            .trim()
                            .takeIf {
                                it.isNotEmpty()
                            },
                )

            } else {

                null
            }


        /*
         * ------------------------------------------------
         * Quantity request value
         * ------------------------------------------------
         *
         * 单数量商品可以不显式传 quantity，
         * 继续由后端使用默认 1。
         */

        val requestQuantity =
            if (
                listing.allowMultiQuantity
            ) {
                state.quantity
            } else {
                null
            }


        val buyerNote =
            state.buyerNote
                .trim()
                .takeIf {
                    it.isNotEmpty()
                }


        /*
         * ------------------------------------------------
         * Create order
         * ------------------------------------------------
         */

        viewModelScope.launch {

            _uiState.value =
                _uiState.value.copy(
                    paying =
                        true,

                    confirming =
                        false,

                    paymentError =
                        null,

                    paymentSuccess =
                        false,

                    purchasedOrderId =
                        null,
                )


            val result =
                repository.createPurchase(
                    request =
                        CreateMarketPurchaseRequest(
                            listingId =
                                listingId,

                            quantity =
                                requestQuantity,

                            shippingInfo =
                                shippingInfo,

                            buyerNote =
                                buyerNote,
                        ),
                )


            result.fold(
                onSuccess = {
                        response ->

                    val purchase =
                        response.purchase

                    val payment =
                        response.payment


                    if (
                        !response.ok ||
                        purchase ==
                        null ||
                        payment ==
                        null
                    ) {

                        pendingOrderId =
                            null

                        pendingOutTradeNo =
                            null


                        _uiState.value =
                            _uiState.value.copy(
                                paying =
                                    false,

                                paymentError =
                                    response.code
                                        ?: response.message
                                        ?: "CREATE_MARKET_PURCHASE_FAILED",
                            )

                        return@fold
                    }


                    /*
                     * 订单已经真实存在。
                     */

                    pendingOrderId =
                        purchase.orderId

                    pendingOutTradeNo =
                        purchase.outTradeNo


                    val launchResult =
                        wechatPayLauncher.launch(
                            payment
                        )


                    launchResult
                        .exceptionOrNull()
                        ?.let { error ->

                            /*
                             * 微信未成功拉起，
                             * 但 pending order 仍保留在服务端。
                             */

                            pendingOrderId =
                                null

                            pendingOutTradeNo =
                                null


                            _uiState.value =
                                _uiState.value.copy(
                                    paying =
                                        false,

                                    confirming =
                                        false,

                                    paymentError =
                                        error.message
                                            ?: "WECHAT_PAY_LAUNCH_FAILED",
                                )
                        }
                },


                onFailure = {
                        error ->

                    pendingOrderId =
                        null

                    pendingOutTradeNo =
                        null


                    _uiState.value =
                        _uiState.value.copy(
                            paying =
                                false,

                            confirming =
                                false,

                            paymentError =
                                error.message
                                    ?: "CREATE_MARKET_PURCHASE_FAILED",
                        )
                },
            )
        }
    }


    /*
     * ------------------------------------------------
     * WeChat callback
     * ------------------------------------------------
     */

    private fun observeWechatPaymentResult() {

        paymentResultJob
            ?.cancel()


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
                             * 微信 SDK 正常返回。
                             *
                             * 这里不能认为已经支付成功，
                             * 必须确认真实订单状态。
                             */

                            0 -> {

                                _uiState.value =
                                    _uiState.value.copy(
                                        paying =
                                            false,

                                        confirming =
                                            true,

                                        paymentError =
                                            null,
                                    )


                                confirmPurchase(
                                    outTradeNo =
                                        outTradeNo,
                                )
                            }


                            /*
                             * 用户取消。
                             *
                             * pending order 保留，
                             * 后续可从订单详情继续支付。
                             */

                            -2 -> {

                                pendingOrderId =
                                    null

                                pendingOutTradeNo =
                                    null


                                _uiState.value =
                                    _uiState.value.copy(
                                        paying =
                                            false,

                                        confirming =
                                            false,

                                        paymentError =
                                            null,
                                    )
                            }


                            else -> {

                                pendingOrderId =
                                    null

                                pendingOutTradeNo =
                                    null


                                _uiState.value =
                                    _uiState.value.copy(
                                        paying =
                                            false,

                                        confirming =
                                            false,

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


    /*
     * ------------------------------------------------
     * Confirm business order
     * ------------------------------------------------
     */

    private suspend fun confirmPurchase(
        outTradeNo:
        String,
    ) {

        repeat(
            STATUS_POLL_MAX_ATTEMPTS
        ) { attempt ->

            if (
                attempt >
                0
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


            val response =
                result.getOrNull()
                    ?: return@repeat


            if (
                !response.ok ||
                !response.found
            ) {
                return@repeat
            }


            when (
                response.status
            ) {

                /*
                 * 真实 marketOrders.status。
                 */

                "paid" -> {

                    val orderId =
                        response.orderId
                            .takeIf {
                                it.isNotBlank()
                            }
                            ?: pendingOrderId


                    pendingOrderId =
                        null

                    pendingOutTradeNo =
                        null


                    _uiState.value =
                        _uiState.value.copy(
                            paying =
                                false,

                            confirming =
                                false,

                            paymentError =
                                null,

                            paymentSuccess =
                                true,

                            purchasedOrderId =
                                orderId,
                        )


                    return
                }


                "cancelled",
                "refunded" -> {

                    pendingOrderId =
                        null

                    pendingOutTradeNo =
                        null


                    _uiState.value =
                        _uiState.value.copy(
                            paying =
                                false,

                            confirming =
                                false,

                            paymentError =
                                when (
                                    response.status
                                ) {

                                    "cancelled" ->
                                        "MARKET_ORDER_CANCELLED"

                                    else ->
                                        "MARKET_ORDER_REFUNDED"
                                },
                        )


                    return
                }


                /*
                 * notify / settle 尚未完成，
                 * 继续下一轮。
                 */

                "pending" ->
                    Unit
            }
        }


        /*
         * 微信已经返回成功，
         * 但短轮询期间仍未变成 paid。
         *
         * 这里不是业务失败。
         */

        pendingOrderId =
            null

        pendingOutTradeNo =
            null


        _uiState.value =
            _uiState.value.copy(
                paying =
                    false,

                confirming =
                    false,

                paymentError =
                    "MARKET_PAYMENT_CONFIRMING",
            )
    }


    /*
     * ------------------------------------------------
     * UI events
     * ------------------------------------------------
     */

    fun consumePaymentSuccess() {

        _uiState.value =
            _uiState.value.copy(
                paymentSuccess =
                    false,

                purchasedOrderId =
                    null,
            )
    }


    fun clearPaymentError() {

        _uiState.value =
            _uiState.value.copy(
                paymentError =
                    null,
            )
    }
}