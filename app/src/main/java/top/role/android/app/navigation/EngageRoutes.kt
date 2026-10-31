package top.role.android.app.navigation

import top.role.android.core.model.bounty.MobileBountyListMode

object BountyListRoute {
    const val modeArg = "mode"

    const val route = "bounty/list/{$modeArg}"

    fun create(
        mode:
        MobileBountyListMode,
    ): String {
        return "bounty/list/${mode.value}"
    }
}

object BountyDetailRoute {
    const val bountyIdArg = "bountyId"

    const val route = "bounty/detail/{$bountyIdArg}"

    fun create(
        bountyId:
        String,
    ): String {
        return "bounty/detail/$bountyId"
    }
}