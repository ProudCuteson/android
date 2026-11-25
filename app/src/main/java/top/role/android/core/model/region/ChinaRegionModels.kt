package top.role.android.core.model.region

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.SerialName

/**
 * 行政区划编码兼容序列化器。
 *
 * pca.json 中的编码可能是数字，也可能是字符串。
 * 统一转换为 Kotlin String。
 */
object RegionCodeSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(
            "RegionCode",
            PrimitiveKind.STRING,
        )

    override fun deserialize(decoder: Decoder): String {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("Expected JSON decoder")

        val element = jsonDecoder.decodeJsonElement()

        if (element !is JsonPrimitive) {
            throw SerializationException("Invalid region code")
        }

        return element.content
    }

    override fun serialize(encoder: Encoder, value: String) {
        encoder.encodeString(value)
    }
}

@Serializable
data class ChinaRegionNode(
    @SerialName("n")
    val label: String = "",

    @SerialName("c")
    @Serializable(with = RegionCodeSerializer::class)
    val value: String = "",

    @SerialName("ch")
    val children: List<ChinaRegionNode> = emptyList(),
)

data class MarketRegionSelection(
    val provinceName: String,
    val provinceCode: String,
    val cityName: String,
    val cityCode: String,
    val districtName: String,
    val districtCode: String,
) {
    val regionText: String
        get() = listOf(
            provinceName,
            cityName,
            districtName,
        ).filter {
            it.isNotBlank()
        }.joinToString(" ")
}
