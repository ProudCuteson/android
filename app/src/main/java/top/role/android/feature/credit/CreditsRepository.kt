package top.role.android.feature.credit

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

import top.role.android.core.convex.ConvexMobileClient
import top.role.android.core.model.account.ContinueCreditPurchaseRequest
import top.role.android.core.model.account.CreateCreditPurchaseRequest
import top.role.android.core.model.account.CreateCreditPurchaseResponse
import top.role.android.core.network.AppHttpClient

import top.role.android.core.model.account.MobileCreditPurchaseOptionsResponse
import top.role.android.core.model.account.MobileCreditPurchaseStatusResponse
import top.role.android.core.model.account.MobileRecentCreditPurchasesResponse

class CreditsRepository(
    private val convexClient:
    ConvexMobileClient,
    private val appHttpClient:
    AppHttpClient,
) {

    private val json =
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }

    fun observePurchaseOptions():
            Flow<Result<MobileCreditPurchaseOptionsResponse>> {

        return convexClient.subscribe(
            name =
                "mobile/native/account/get:getMobileCreditPurchaseOptions",

            args =
                emptyMap(),
        )
    }

    fun observeRecentPurchases(
        limit:
        Double = 10.0,
    ): Flow<Result<MobileRecentCreditPurchasesResponse>> {

        return convexClient.subscribe(
            name =
                "mobile/native/purchase/credits:listMyRecentCreditPurchases",

            args =
                mapOf(
                    "limit" to limit,
                ),
        )
    }

    suspend fun createPurchase(
        request: CreateCreditPurchaseRequest,
    ): Result<CreateCreditPurchaseResponse> {

        return runCatching {

            val jsonBody =
                json.encodeToString(
                    request
                )

            val response =
                appHttpClient.postJson(
                    path =
                        CREDIT_CREATE_PATH,

                    jsonBody =
                        jsonBody,
                )

            json.decodeFromString<
                    CreateCreditPurchaseResponse
                    >(
                response.body
            )
        }
    }


    suspend fun getPurchaseStatus(
        outTradeNo:
        String,
    ): Result<MobileCreditPurchaseStatusResponse> {

        return runCatching {

            convexClient
                .subscribe<
                        MobileCreditPurchaseStatusResponse
                        >(
                    name =
                        "mobile/native/purchase/credits:getAppCreditPurchaseStatus",

                    args =
                        mapOf(
                            "outTradeNo" to
                                    outTradeNo,
                        ),
                )
                .first()
                .getOrThrow()
        }
    }

    suspend fun continuePurchase(
        outTradeNo: String,
    ): Result<CreateCreditPurchaseResponse> {

        return runCatching {

            val jsonBody =
                json.encodeToString(
                    ContinueCreditPurchaseRequest(
                        outTradeNo =
                            outTradeNo,
                    )
                )

            val response =
                appHttpClient.postJson(
                    path =
                        "/api/pay/wechat/credit/app/continue-order",

                    jsonBody =
                        jsonBody,
                )

            json.decodeFromString<
                    CreateCreditPurchaseResponse
                    >(
                response.body
            )
        }
    }


    private companion object {
        const val CREDIT_CREATE_PATH =
            "/api/pay/wechat/credit/app/create-order"
    }

}