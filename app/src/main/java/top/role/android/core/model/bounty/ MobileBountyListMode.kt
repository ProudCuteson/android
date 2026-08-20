package top.role.android.core.model.bounty

enum class MobileBountyListMode(
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
        ): MobileBountyListMode {
            return entries
                .firstOrNull {
                    it.value == value
                }
                ?: Featured
        }
    }
}