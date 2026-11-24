package top.role.android.core.model.content

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

interface MobileNodeIdentity {
    val id: String
    val sourceId: String?
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("kind")
sealed interface MobileRenderBlock : MobileNodeIdentity

@Serializable
data class MobileRenderDoc(
    val version: Double,
    val blocks: List<MobileRenderBlock>,

    val repairedBlockIds: Double? = null,
    val repairedLeafIds: Double? = null,
)