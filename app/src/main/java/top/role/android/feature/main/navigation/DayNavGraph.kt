package top.role.android.feature.main.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import top.role.android.app.navigation.MainDestination
import top.role.android.app.navigation.NoteDayRoute
import top.role.android.app.navigation.NoteEditorRoute as NoteEditorNavRoute
import top.role.android.feature.day.DayScreen
import top.role.android.feature.note.editor.NoteEditorRoute
import top.role.android.app.navigation.NoteLibraryRoute
import top.role.android.feature.note.library.NoteLibraryScreen
import top.role.android.app.navigation.NoteNewRoute
import top.role.android.app.navigation.NoteNewDatedRoute

fun NavGraphBuilder.dayNavGraph(
    navController: NavHostController,
    dependencies: MainNavigationDependencies,
) {
    composable(
        route =
            MainDestination.Day.route,
    ) {
        DayScreen(
            ownerUserId =
                dependencies.localOwnerUserId,

            noteLocalRepository =
                dependencies.noteLocalRepository,

            onOpenDayNote = { dayKey ->
                navController.navigate(
                    NoteDayRoute.createRoute(
                        dayKey = dayKey,
                    )
                )
            },

            onOpenNote = { localKey ->
                navController.navigate(
                    NoteEditorNavRoute.createRoute(
                        localKey = localKey,
                    )
                )
            },

            onCreateDatedNote = { dayKey ->
                navController.navigate(
                    NoteNewDatedRoute.createRoute(
                        dayKey = dayKey,
                    )
                )
            },
        )
    }

    composable(
        route =
            NoteDayRoute.route,
    ) { backStackEntry ->

        val dayKey =
            backStackEntry
                .arguments
                ?.getString(
                    NoteDayRoute.dayKeyArg,
                )

        if (dayKey != null) {
            NoteEditorRoute(
                ownerUserId =
                    dependencies.localOwnerUserId,

                noteLocalRepository =
                    dependencies.noteLocalRepository,

                noteImageService =
                    dependencies.noteImageService,

                initialDayKey =
                    dayKey,

                openAsDayOnly =
                    true,

                onBack = {
                    navController
                        .popBackStack()
                },
            )
        }
    }

    composable(
        route =
            NoteEditorNavRoute.route,
    ) { backStackEntry ->

        val localKey =
            backStackEntry
                .arguments
                ?.getString(
                    NoteEditorNavRoute.localKeyArg,
                )

        if (localKey != null) {
            NoteEditorRoute(
                ownerUserId =
                    dependencies.localOwnerUserId,

                noteLocalRepository =
                    dependencies.noteLocalRepository,

                noteImageService =
                    dependencies.noteImageService,

                initialLocalKey =
                    localKey,

                initialDayKey =
                    null,

                openAsDayOnly =
                    false,

                onBack = {
                    navController
                        .popBackStack()
                },
            )
        }
    }

    composable(
        route =
            NoteLibraryRoute.route,
    ) {
        NoteLibraryScreen(
            ownerUserId =
                dependencies.localOwnerUserId,

            noteLocalRepository =
                dependencies.noteLocalRepository,

            onBack = {
                navController
                    .popBackStack()
            },

            onOpenNote = { localKey ->
                navController.navigate(
                    NoteEditorNavRoute.createRoute(
                        localKey = localKey,
                    )
                )
            },

            onCreateNote = {
                navController.navigate(
                    NoteNewRoute.route,
                )
            },

        )
    }

    composable(
        route =
            NoteNewDatedRoute.route,
    ) { backStackEntry ->

        val dayKey =
            backStackEntry
                .arguments
                ?.getString(
                    NoteNewDatedRoute.dayKeyArg,
                )

        if (dayKey != null) {
            NoteEditorRoute(
                ownerUserId =
                    dependencies.localOwnerUserId,

                noteLocalRepository =
                    dependencies.noteLocalRepository,

                noteImageService =
                    dependencies.noteImageService,

                initialDayKey =
                    dayKey,

                createAsDated =
                    true,

                onBack = {
                    navController
                        .popBackStack()
                },
            )
        }
    }

    composable(
        route =
            NoteNewRoute.route,
    ) {
        NoteEditorRoute(
            ownerUserId =
                dependencies.localOwnerUserId,

            noteLocalRepository =
                dependencies.noteLocalRepository,

            noteImageService =
                dependencies.noteImageService,

            createAsNormal =
                true,

            onBack = {
                navController
                    .popBackStack()
            },
        )
    }

}