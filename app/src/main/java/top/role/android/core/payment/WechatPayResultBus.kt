package top.role.android.core.payment

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class WechatPaySdkResult(
    val errCode: Int,
    val errStr: String? = null,
)

object WechatPayResultBus {

    private val _results =
        MutableSharedFlow<WechatPaySdkResult>(
            replay = 0,
            extraBufferCapacity = 1,
            onBufferOverflow =
                BufferOverflow.DROP_OLDEST,
        )

    val results:
            SharedFlow<WechatPaySdkResult> =
        _results.asSharedFlow()

    fun publish(
        errCode: Int,
        errStr: String?,
    ) {
        _results.tryEmit(
            WechatPaySdkResult(
                errCode = errCode,
                errStr = errStr,
            )
        )
    }
}