package top.role.android.core.payment

fun wechatPayDisplayMessage(
    message: String?,
): String {
    return when (message?.trim()) {
        "WECHAT_NOT_INSTALLED" ->
            "请先安装微信，再进行支付。"

        "WECHAT_APP_ID_MISSING",
        "WECHAT_PARTNER_ID_MISSING",
        "WECHAT_PREPAY_ID_MISSING" ->
            "暂时无法获取微信支付信息，请稍后再试。"

        "WECHAT_PAY_REQUEST_REJECTED",
        "WECHAT_PAY_LAUNCH_FAILED" ->
            "无法唤起微信支付，请稍后再试。"

        "WECHAT_PAY_FAILED" ->
            "微信支付未完成，请稍后查看订单状态。"

        "CREATE_APP_PAY_ORDER_FAILED",
        "CREATE_CREDIT_PURCHASE_FAILED" ->
            "暂时无法创建支付订单，请稍后再试。"

        "CONTINUE_CREDIT_PURCHASE_FAILED" ->
            "暂时无法继续支付，请稍后再试。"

        "CREDIT_PAYMENT_NOT_SETTLED" ->
            "暂时无法确认充值结果，请查看购买记录。"

        "CREDIT_PAYMENT_CONFIRMING" ->
            "支付结果正在确认，请稍后查看购买记录。"

        "APP_NOT_AUTHENTICATED",
        "APP_SESSION_INVALID",
        "APP_SESSION_NOT_FOUND",
        "APP_SESSION_REVOKED",
        "APP_SESSION_EXPIRED",
        "UNAUTHENTICATED" ->
            "登录状态已失效，请重新登录。"

        "PAYMENT_CHANNEL_MISMATCH" ->
            "该订单不支持当前支付方式。"

        "BILLING_INTENT_EXPIRED" ->
            "该订单已过期，请重新购买。"

        else -> {
            val text = message.orEmpty().trim()

            if (text.any { it in '\u4e00'..'\u9fff' }) {
                text
            } else {
                "暂时无法完成操作，请稍后查看购买记录或重试。"
            }
        }
    }
}