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

object EntityServiceReferencePreviewRoute {

    const val serviceIdArg = "serviceId"
    const val targetTypeArg = "targetType"
    const val targetIdArg = "targetId"
    const val sourceArg = "source"

    private const val base =
        "entity-service/reference-preview"

    const val route =
        "$base/{$serviceIdArg}/{$targetTypeArg}/{$targetIdArg}/{$sourceArg}"

    fun create(
        serviceId: String,
        targetType: String,
        targetId: String,
        source: String,
    ): String {
        return "$base/$serviceId/$targetType/$targetId/$source"
    }
}