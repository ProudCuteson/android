package top.role.android.core.model.market

enum class MobileMarketListMode(
    val value: String,
) {
    Featured(
        value = "featured",
    ),

    Latest(
        value = "latest",
    );

    companion object {

        fun fromValue(
            value: String?,
        ): MobileMarketListMode {
            return entries
                .firstOrNull {
                    it.value == value
                }
                ?: Featured
        }
    }
}