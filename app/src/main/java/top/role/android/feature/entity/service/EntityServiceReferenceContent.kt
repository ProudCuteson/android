package top.role.android.feature.entity.service

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class EntityServiceReferenceContentResponse(
    val ok: Boolean,
    val code: String? = null,
    val content: JsonObject? = null,
)

enum class EntityServiceReferenceSource(
    val value: String,
) {
    REFERENCE("reference"),
    RESULT("result"),
}

enum class EntityServiceReferenceType(
    val value: String,
) {
    NOTE("note"),
    DOC("doc"),
    SLIDE("slide"),
    FILE_ASSET("fileAsset"),
}

@Serializable
data class AssetDeliveryResult(
    val assetId: String,
    val accessMode: String,
    val url: String,
    val expiresSec: Double? = null,
)