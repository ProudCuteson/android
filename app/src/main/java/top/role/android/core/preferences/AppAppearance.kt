package top.role.android.core.preferences


enum class AppAppearance(
    val storageValue: String,
) {
    System(
        storageValue = "system",
    ),

    Light(
        storageValue = "light",
    ),

    Dark(
        storageValue = "dark",
    );


    companion object {

        fun fromStorage(
            value: String?,
        ): AppAppearance {

            return entries
                .firstOrNull {
                    it.storageValue == value
                }
                ?: System
        }
    }
}


fun AppAppearance.displayLabel(): String {

    return when (
        this
    ) {
        AppAppearance.System ->
            "跟随系统"

        AppAppearance.Light ->
            "浅色"

        AppAppearance.Dark ->
            "深色"
    }
}