package top.role.android.feature.main

import top.role.android.app.navigation.ContentListRoute
import top.role.android.app.navigation.DocDetailRoute
import top.role.android.app.navigation.EntityDetailRoute
import top.role.android.app.navigation.MainDestination
import top.role.android.app.navigation.NoteDayRoute
import top.role.android.app.navigation.NoteEditorRoute
import top.role.android.app.navigation.NoteLibraryRoute
import top.role.android.app.navigation.NoteNewDatedRoute
import top.role.android.app.navigation.NoteNewRoute
import top.role.android.app.navigation.SlideDetailRoute
import top.role.android.app.navigation.SpaceListRoute
import top.role.android.app.navigation.TopicDetailRoute
import top.role.android.app.navigation.TopicListRoute
import top.role.android.core.model.space.SpaceListMode

const val ACCOUNT_ROUTE =
    "account"

data class MainShellState(
    val title: String,

    val showTopBar: Boolean =
        true,

    val showBack: Boolean =
        false,

    val showLibrary: Boolean =
        false,

    val showAccount: Boolean =
        false,

    val showBottomBar: Boolean =
        false,
)

fun resolveMainShellState(
    route: String?,
    spaceListMode: SpaceListMode?,
): MainShellState {

    return when (
        route
    ) {

        MainDestination.Day.route ->
            MainShellState(
                title =
                    "今日",

                showTopBar =
                    true,

                showBack =
                    false,

                showLibrary =
                    true,

                showAccount =
                    true,

                showBottomBar =
                    true,
            )

        MainDestination.Discover.route ->
            MainShellState(
                title =
                    "发现",

                showTopBar =
                    true,

                showBottomBar =
                    true,
            )

        MainDestination.Chat.route ->
            MainShellState(
                title =
                    "聊天",

                showTopBar =
                    true,

                showBottomBar =
                    true,
            )

        MainDestination.Engage.route ->
            MainShellState(
                title =
                    "参与",

                showTopBar =
                    true,

                showBottomBar =
                    true,
            )

        ACCOUNT_ROUTE ->
            MainShellState(
                title =
                    "我的",

                showTopBar =
                    true,

                showBack =
                    true,

                showBottomBar =
                    false,
            )

        SpaceListRoute.route ->
            MainShellState(
                title =
                    when (
                        spaceListMode
                    ) {
                        SpaceListMode.Featured ->
                            "推荐空间"

                        SpaceListMode.Following ->
                            "我关注的"

                        SpaceListMode.Latest ->
                            "最新空间"

                        null ->
                            "空间"
                    },

                showTopBar =
                    true,

                showBack =
                    true,

                showBottomBar =
                    false,
            )

        TopicListRoute.route ->
            detailShell(
                title =
                    "话题",
            )

        ContentListRoute.route ->
            detailShell(
                title =
                    "内容",
            )

        EntityDetailRoute.route ->
            detailShell(
                title =
                    "空间",
            )

        TopicDetailRoute.route ->
            detailShell(
                title =
                    "话题",
            )

        DocDetailRoute.route ->
            detailShell(
                title =
                    "阅读",
            )

        SlideDetailRoute.route ->
            detailShell(
                title =
                    "演示",
            )

        /*
         * ------------------------------------------------
         * Note module
         * ------------------------------------------------
         *
         * Note 页面自己拥有 Scaffold / TopAppBar。
         *
         * Main shell 不再重复提供顶部栏，
         * 避免双抬头。
         */
        NoteDayRoute.route,
        NoteEditorRoute.route,
        NoteLibraryRoute.route,
        NoteNewRoute.route,
        NoteNewDatedRoute.route ->
            pageOwnedShell()

        else ->
            MainShellState(
                title =
                    "TopRole",

                showTopBar =
                    true,

                showBack =
                    true,

                showBottomBar =
                    false,
            )
    }
}

private fun detailShell(
    title: String,
): MainShellState {
    return MainShellState(
        title =
            title,

        showTopBar =
            true,

        showBack =
            true,

        showLibrary =
            false,

        showAccount =
            false,

        showBottomBar =
            false,
    )
}

private fun pageOwnedShell():
        MainShellState {

    return MainShellState(
        title =
            "",

        showTopBar =
            false,

        showBack =
            false,

        showLibrary =
            false,

        showAccount =
            false,

        showBottomBar =
            false,
    )
}