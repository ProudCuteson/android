package top.role.android.core.model.region

import kotlinx.serialization.Serializable


@Serializable
data class ChinaRegionNode(
    val label: String = "",
    val value: String = "",
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
        get() =
            listOf(
                provinceName,
                cityName,
                districtName,
            )
                .filter {
                    it.isNotBlank()
                }
                .joinToString(" ")
}