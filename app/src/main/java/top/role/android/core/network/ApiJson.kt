package top.role.android.core.network

import kotlinx.serialization.json.Json

val ApiJson = Json {
    ignoreUnknownKeys = true
    isLenient = false
}