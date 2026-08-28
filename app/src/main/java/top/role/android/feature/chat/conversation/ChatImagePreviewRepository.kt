package top.role.android.feature.chat.conversation

import kotlinx.serialization.Serializable

import top.role.android.core.convex.ConvexMobileClient

@Serializable
data class MobileChatImagePreviewResult(
    val imageId: String,
    val kind: String,
    val url: String? = null,
    val expiresSec: Double,
)

class ChatImagePreviewRepository(
    private val convexClient: ConvexMobileClient,
) {

    private data class CacheEntry(
        val url: String,
        val expiresAt: Long,
    )

    private val cache =
        mutableMapOf<String, CacheEntry>()

    suspend fun getPreviewUrl(
        imageId: String,
        kind: String,
        expiresSec: Double = 1800.0,
    ): MobileChatImagePreviewResult {
        require(
            imageId.isNotBlank()
        ) {
            "IMAGE_ID_REQUIRED"
        }

        val key =
            cacheKey(
                imageId = imageId,
                kind = kind,
            )

        val now =
            System.currentTimeMillis()

        val cached =
            cache[key]

        if (
            cached != null &&
            cached.expiresAt >
            now + CACHE_SAFETY_WINDOW_MS
        ) {
            return MobileChatImagePreviewResult(
                imageId = imageId,
                kind = kind,
                url = cached.url,
                expiresSec =
                    (
                            cached.expiresAt -
                                    now
                            ).coerceAtLeast(0L) /
                            1000.0,
            )
        }

        val result:
                MobileChatImagePreviewResult =
            convexClient.action(
                name =
                    "mobile/native/chat/image:getImagePreviewUrl",

                args =
                    mapOf(
                        "imageId" to
                                imageId,

                        "kind" to
                                kind,

                        "expiresSec" to
                                expiresSec,
                    ),
            )

        val url =
            result.url
                ?.takeIf {
                    it.isNotBlank()
                }

        if (url != null) {
            cache[key] =
                CacheEntry(
                    url = url,

                    expiresAt =
                        now +
                                (
                                        result.expiresSec *
                                                1000.0
                                        ).toLong(),
                )
        } else {
            cache.remove(
                key
            )
        }

        return result
    }

    fun clear(
        imageId: String,
        kind: String? = null,
    ) {
        if (kind != null) {
            cache.remove(
                cacheKey(
                    imageId = imageId,
                    kind = kind,
                )
            )

            return
        }

        cache.keys
            .filter {
                it.startsWith(
                    "$imageId|"
                )
            }
            .forEach {
                cache.remove(
                    it
                )
            }
    }

    fun clearAll() {
        cache.clear()
    }

    private fun cacheKey(
        imageId: String,
        kind: String,
    ): String {
        return "$imageId|$kind"
    }

    private companion object {
        const val CACHE_SAFETY_WINDOW_MS =
            30_000L
    }
}