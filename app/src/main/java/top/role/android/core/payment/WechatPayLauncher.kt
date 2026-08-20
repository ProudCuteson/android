package top.role.android.core.payment

import android.content.Context
import com.tencent.mm.opensdk.modelpay.PayReq
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import top.role.android.core.model.payment.WechatAppPayParams

class WechatPayLauncher(
    context: Context,
) {

    private val appContext =
        context.applicationContext

    private var wxApi: IWXAPI? =
        null

    fun launch(
        params: WechatAppPayParams,
    ): Result<Unit> {
        return runCatching {
            require(
                params.appId.isNotBlank()
            ) {
                "WECHAT_APP_ID_MISSING"
            }

            require(
                params.partnerId.isNotBlank()
            ) {
                "WECHAT_PARTNER_ID_MISSING"
            }

            require(
                params.prepayId.isNotBlank()
            ) {
                "WECHAT_PREPAY_ID_MISSING"
            }

            val api =
                ensureApi(
                    params.appId
                )

            if (
                !api.isWXAppInstalled
            ) {
                error(
                    "WECHAT_NOT_INSTALLED"
                )
            }

            val request =
                PayReq().apply {
                    appId =
                        params.appId

                    partnerId =
                        params.partnerId

                    prepayId =
                        params.prepayId

                    packageValue =
                        params.packageValue

                    nonceStr =
                        params.nonceStr

                    timeStamp =
                        params.timeStamp

                    sign =
                        params.sign
                }

            val accepted =
                api.sendReq(
                    request
                )

            if (!accepted) {
                error(
                    "WECHAT_PAY_REQUEST_REJECTED"
                )
            }
        }
    }

    private fun ensureApi(
        appId: String,
    ): IWXAPI {
        val existing =
            wxApi

        if (existing != null) {
            return existing
        }

        return WXAPIFactory
            .createWXAPI(
                appContext,
                appId,
                true,
            )
            .also { api ->
                api.registerApp(
                    appId
                )

                wxApi = api
            }
    }
}