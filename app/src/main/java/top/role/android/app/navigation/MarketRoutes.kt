package top.role.android.app.navigation

import top.role.android.core.model.market.MobileMarketListMode


object MarketListRoute {

    const val modeArg =
        "mode"

    const val route =
        "market/list/{$modeArg}"

    fun create(
        mode:
        MobileMarketListMode,
    ): String {
        return "market/list/${mode.value}"
    }
}


object MarketDetailRoute {

    const val listingIdArg =
        "listingId"

    const val route =
        "market/detail/{$listingIdArg}"

    fun create(
        listingId:
        String,
    ): String {
        return "market/detail/$listingId"
    }
}