package top.role.android.feature.membership

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import top.role.android.core.convex.ConvexMobileClient
import top.role.android.core.model.membership.CreateMembershipPurchaseRequest
import top.role.android.core.model.membership.CreateMembershipPurchaseResponse
import top.role.android.core.model.membership.MobileMembershipPageResponse
import top.role.android.core.model.membership.MobileMembershipPurchaseStatusResponse
import top.role.android.core.network.AppHttpClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds

class MembershipRepository(
    private val convexClient: ConvexMobileClient,
    private val appHttpClient: AppHttpClient,
) {

    private val json =
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }

    fun observePage(
        spaceId: String,
    ): Flow<Result<MobileMembershipPageResponse>> {
        return convexClient.subscribe(
            name =
                "mobile/native/purchase/membership:getAppSpaceMembershipPage",
            args =
                mapOf(
                    "spaceId" to spaceId,
                ),
        )
    }

    fun observePurchaseStatus(
        outTradeNo: String,
    ): Flow<Result<MobileMembershipPurchaseStatusResponse>> {
        return convexClient.subscribe(
            name =
                "mobile/native/purchase/membership:getAppMembershipPurchaseStatus",
            args =
                mapOf(
                    "outTradeNo" to outTradeNo,
                ),
        )
    }

    suspend fun createPurchase(
        spaceId: String,
        planKey: String,
    ): Result<CreateMembershipPurchaseResponse> {
        return runCatching {
            val requestBody =
                CreateMembershipPurchaseRequest(
                    spaceId = spaceId,
                    planKey = planKey,
                )

            val response =
                appHttpClient.postJson(
                    path =
                        "/api/pay/wechat/membership/app/create-order",
                    jsonBody =
                        json.encodeToString(
                            requestBody
                        ),
                )

            val parsed =
                json.decodeFromString<CreateMembershipPurchaseResponse>(
                    response.body
                )

            if (
                response.statusCode !in 200..299
            ) {
                throw MembershipPurchaseException(
                    code =
                        parsed.code
                            ?: "HTTP_${response.statusCode}",
                    message =
                        parsed.message,
                )
            }

            if (
                !parsed.ok ||
                parsed.purchase == null ||
                parsed.payment == null
            ) {
                throw MembershipPurchaseException(
                    code =
                        parsed.code
                            ?: "INVALID_PURCHASE_RESPONSE",
                    message =
                        parsed.message,
                )
            }

            parsed
        }
    }

    suspend fun getPurchaseStatus(
        outTradeNo: String,
    ): Result<MobileMembershipPurchaseStatusResponse> {
        return withTimeout(
            5.seconds
        ) {
            convexClient
                .subscribe<MobileMembershipPurchaseStatusResponse>(
                    name =
                        "mobile/native/purchase/membership:getAppMembershipPurchaseStatus",

                    args =
                        mapOf(
                            "outTradeNo" to
                                    outTradeNo,
                        ),
                )
                .first()
        }
    }

}

class MembershipPurchaseException(
    val code: String,
    override val message: String? = null,
) : Exception(
    message ?: code
)