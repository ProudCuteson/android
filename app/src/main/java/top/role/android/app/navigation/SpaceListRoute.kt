package top.role.android.app.navigation

import top.role.android.core.model.space.SpaceListMode

object SpaceListRoute {

    const val modeArg = "mode"

    const val route =
        "discover/spaces/{$modeArg}"

    fun create(
        mode: SpaceListMode,
    ): String {
        return "discover/spaces/${mode.value}"
    }
}