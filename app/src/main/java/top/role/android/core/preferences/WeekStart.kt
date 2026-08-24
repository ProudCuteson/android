package top.role.android.core.preferences


enum class WeekStart(
    val storageValue: String,
) {
    Monday(
        storageValue = "monday",
    ),

    Sunday(
        storageValue = "sunday",
    );


    companion object {

        fun fromStorage(
            value: String?,
        ): WeekStart {

            return entries
                .firstOrNull {
                    it.storageValue == value
                }
                ?: Monday
        }
    }
}


fun WeekStart.displayLabel(): String {

    return when (
        this
    ) {
        WeekStart.Monday ->
            "星期一"

        WeekStart.Sunday ->
            "星期日"
    }
}