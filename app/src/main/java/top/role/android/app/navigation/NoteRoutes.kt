package top.role.android.app.navigation

import android.net.Uri

object NoteDayRoute {
    const val dayKeyArg = "dayKey"

    const val route = "note/day/{$dayKeyArg}"

    fun createRoute(
        dayKey: String,
    ): String {
        return "note/day/${Uri.encode(dayKey)}"
    }
}

object NoteEditorRoute {

    const val localKeyArg = "localKey"

    const val route = "note/editor/{$localKeyArg}"

    fun createRoute(
        localKey: String,
    ): String {
        return "note/editor/${Uri.encode(localKey)}"
    }
}

object NoteLibraryRoute {

    const val route = "note/library"
}

object NoteNewRoute {

    const val route = "note/new"
}

object NoteNewDatedRoute {

    const val dayKeyArg = "dayKey"

    const val route = "note/new/dated/{$dayKeyArg}"

    fun createRoute(
        dayKey: String,
    ): String {
        return "note/new/dated/${Uri.encode(dayKey)}"
    }
}