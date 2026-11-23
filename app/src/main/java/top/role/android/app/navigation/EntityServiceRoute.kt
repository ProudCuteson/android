package top.role.android.app.navigation

import android.net.Uri

object EntityServiceRoute {

    const val spaceIdArg = "spaceId"
    const val modeArg = "mode"

    const val route =
        "entity-service/{$spaceIdArg}/{$modeArg}"

    const val MODE_MINE = "mine"
    const val MODE_MANAGED = "managed"

    fun create(
        spaceId: String,
        isManager: Boolean,
    ): String {
        val mode = if (isManager) {
            MODE_MANAGED
        } else {
            MODE_MINE
        }

        return "entity-service/${Uri.encode(spaceId)}/$mode"
    }
}