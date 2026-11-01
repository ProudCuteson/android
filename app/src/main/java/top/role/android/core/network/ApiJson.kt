//全网络层共享的 JSON 序列化配置，不仅是Auth
package top.role.android.core.network

import kotlinx.serialization.json.Json

val ApiJson = Json {
    ignoreUnknownKeys = true
    isLenient = false
}