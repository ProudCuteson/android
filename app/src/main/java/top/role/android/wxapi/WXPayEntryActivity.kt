package top.role.android.wxapi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.tencent.mm.opensdk.constants.ConstantsAPI
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import top.role.android.core.payment.WechatPayResultBus

class WXPayEntryActivity :
    Activity(),
    IWXAPIEventHandler {

    private lateinit var wxApi: IWXAPI

    override fun onCreate(
        savedInstanceState: Bundle?,
    ) {
        super.onCreate(savedInstanceState)

        wxApi =
            WXAPIFactory.createWXAPI(
                this,
                null,
            )

        wxApi.handleIntent(
            intent,
            this,
        )
    }

    override fun onNewIntent(
        intent: Intent,
    ) {
        super.onNewIntent(intent)

        setIntent(intent)

        wxApi.handleIntent(
            intent,
            this,
        )
    }

    override fun onReq(
        req: BaseReq?,
    ) {
        // APP Pay 不需要处理微信主动请求。
    }

    override fun onResp(
        resp: BaseResp?,
    ) {
        if (
            resp?.type !=
            ConstantsAPI.COMMAND_PAY_BY_WX
        ) {
            finish()
            return
        }

        WechatPayResultBus.publish(
            errCode =
                resp.errCode,

            errStr =
                resp.errStr,
        )

        finish()
    }
}