package top.role.android.feature.market.detail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

import top.role.android.core.convex.ConvexMobileClient
import top.role.android.core.model.market.CreateMarketPurchaseRequest
import top.role.android.core.model.market.CreateMarketPurchaseResponse
import top.role.android.core.model.market.MobileMarketDetailResponse
import top.role.android.core.model.market.MobileMarketPurchaseStatusResponse
import top.role.android.core.network.AppHttpClient


class MarketDetailRepository(
    private val convexClient: ConvexMobileClient,
    private val appHttpClient: AppHttpClient,
) {

    private val json =
        Json {
            ignoreUnknownKeys =
                true

            explicitNulls =
                false
        }


    fun observeDetail(
        listingId:
        String,
    ): Flow<
            Result<MobileMarketDetailResponse>
            > {
        return convexClient.subscribe(
            name =
                "mobile/native/market/index:getMobileMarketListingDetail",

            args =
                mapOf(
                    "listingId" to
                            listingId,
                ),
        )
    }


    suspend fun createPurchase(
        request:
        CreateMarketPurchaseRequest,
    ): Result<CreateMarketPurchaseResponse> {

        return runCatching {

            val jsonBody =
                json.encodeToString(
                    request
                )

            val response =
                appHttpClient.postJson(
                    path =
                        "/api/pay/wechat/market/app/create-order",

                    jsonBody =
                        jsonBody,
                )

            /*
             * 即使 HTTP 是 4xx / 5xx，
             * 如果后端返回标准 JSON，
             * 仍然优先尝试解析 code/message。
             */
            val decoded =
                json.decodeFromString<
                        CreateMarketPurchaseResponse
                        >(
                    response.body
                )

            if (
                response.statusCode
                !in 200..299
            ) {
                /*
                 * 不在这里直接 throw，
                 * 因为 ViewModel 需要读取：
                 *
                 * decoded.ok
                 * decoded.code
                 * decoded.message
                 */
                return@runCatching decoded
            }

            decoded
        }
    }


    suspend fun getPurchaseStatus(
        outTradeNo:
        String,
    ): Result<
            MobileMarketPurchaseStatusResponse
            > {

        return runCatching {

            convexClient
                .subscribe<
                        MobileMarketPurchaseStatusResponse
                        >(
                    name =
                        "mobile/native/purchase/market:getAppMarketOrderStatus",

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
}