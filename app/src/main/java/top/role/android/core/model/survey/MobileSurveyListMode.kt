package top.role.android.core.model.survey

enum class MobileSurveyListMode(
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
        ): MobileSurveyListMode {
            return entries
                .firstOrNull {
                    it.value == value
                }
                ?: Featured
        }
    }
}