package top.role.android.core.model.payment

import kotlinx.serialization.Serializable

@Serializable
data class WechatAppPayParams(
    val appId: String = "",
    val partnerId: String = "",
    val prepayId: String = "",
    val packageValue: String = "",
    val nonceStr: String = "",
    val timeStamp: String = "",
    val sign: String = "",
)