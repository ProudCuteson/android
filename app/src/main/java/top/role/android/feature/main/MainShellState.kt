package top.role.android.feature.main

import top.role.android.app.navigation.AccountRoute
import top.role.android.app.navigation.AccountPasswordRoute
import top.role.android.app.navigation.CreditsRoute
import top.role.android.app.navigation.BountyDetailRoute
import top.role.android.app.navigation.BountyListRoute
import top.role.android.app.navigation.ChatConversationRoute
import top.role.android.app.navigation.ChatFriendLookupRoute
import top.role.android.app.navigation.ChatFriendsRoute
import top.role.android.app.navigation.ChatGroupCreateRoute
import top.role.android.app.navigation.ChatGroupSettingsRoute
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
import top.role.android.app.navigation.SurveyDetailRoute
import top.role.android.app.navigation.SurveyListRoute
import top.role.android.app.navigation.TopicDetailRoute
import top.role.android.app.navigation.TopicListRoute
import top.role.android.app.navigation.MembershipRoute
import top.role.android.core.model.space.SpaceListMode
import top.role.android.app.navigation.MarketDetailRoute
import top.role.android.app.navigation.MarketListRoute
import top.role.android.app.navigation.MarketOrderListRoute
import top.role.android.app.navigation.MarketOrderDetailRoute

data class MainShellState(
    val title: String,
    val showTopBar: Boolean = true,
    val showBack: Boolean = false,
    val showLibrary: Boolean = false,
    val showAccount: Boolean = false,
    val showFriends: Boolean = false,
    val showAddFriend: Boolean = false,
    val showCreateGroup: Boolean = false,
    val showMarketOrders: Boolean = false,
    val showBottomBar: Boolean = false,
)

fun resolveMainShellState(
    route:
    String?,

    spaceListMode:
    SpaceListMode?,
): MainShellState {

    if (
        route == null
    ) {
        return pageOwnedShell()
    }

    return when (
        route
    ) {

        /*
         * ------------------------------------------------
         * Main tabs
         * ------------------------------------------------
         */

        MainDestination.Day.route ->
            MainShellState(
                title =
                    "今日",

                showTopBar =
                    true,

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

                showFriends =
                    true,

                showCreateGroup =
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

        /*
         * ------------------------------------------------
         * Chat
         * ------------------------------------------------
         */

        ChatFriendsRoute.route ->
            MainShellState(
                title =
                    "好友",

                showTopBar =
                    true,

                showBack =
                    true,

                showAddFriend =
                    true,

                showBottomBar =
                    false,
            )

        ChatFriendLookupRoute.route ->
            MainShellState(
                title =
                    "添加好友",

                showTopBar =
                    true,

                showBack =
                    true,

                showBottomBar =
                    false,
            )

        ChatGroupCreateRoute.route ->
            MainShellState(
                title =
                    "创建群聊",

                showTopBar =
                    true,

                showBack =
                    true,

                showBottomBar =
                    false,
            )

        /*
         * Conversation 自己拥有 TopAppBar：
         *
         * - 显示真实 conversation title
         * - direct / group 自己决定工具
         * - group settings 入口也在页面内部
         */
        ChatConversationRoute.route ->
            pageOwnedShell()

        ChatGroupSettingsRoute.route ->
            MainShellState(
                title =
                    "群聊设置",

                showTopBar =
                    true,

                showBack =
                    true,

                showBottomBar =
                    false,
            )

        /*
         * ------------------------------------------------
         * Account
         * ------------------------------------------------
         */

        AccountRoute.route ->
            detailShell(
                title =
                    "我的",
            )

        CreditsRoute.route ->
            detailShell(
                title =
                    "购买 Credits",
            )

        AccountPasswordRoute.route ->
            detailShell(
                title =
                    "账户安全",
            )

        /*
         * ------------------------------------------------
         * Discover
         * ------------------------------------------------
         */

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

        MembershipRoute.route ->
            detailShell(
                title =
                    "会员",
            )

        /*
         * ------------------------------------------------
         * Engage / Survey
         * ------------------------------------------------
         */

        SurveyListRoute.route ->
            detailShell(
                title =
                    "调查与测评",
            )

        SurveyDetailRoute.route ->
            detailShell(
                title =
                    "调查与测评",
            )

        /*
         * ------------------------------------------------
         * Engage / Bounty
         * ------------------------------------------------
         */

        BountyListRoute.route ->
            detailShell(
                title =
                    "赏金任务",
            )

        BountyDetailRoute.route ->
            detailShell(
                title =
                    "赏金任务",
            )

        /*
         * ------------------------------------------------
         * Engage / Market
         * ------------------------------------------------
         */

        MarketListRoute.route ->
            MainShellState(
                title =
                    "极致单品",

                showTopBar =
                    true,

                showBack =
                    true,

                showMarketOrders =
                    true,

                showBottomBar =
                    false,
            )

        MarketDetailRoute.route ->
            detailShell(
                title =
                    "单品详情",
            )

        MarketOrderListRoute.route,
        MarketOrderDetailRoute.route ->
            pageOwnedShell()

        /*
         * ------------------------------------------------
         * Note
         * ------------------------------------------------
         *
         * Note 页面拥有自己的 Scaffold / TopAppBar。
         */

        NoteDayRoute.route,
        NoteEditorRoute.route,
        NoteLibraryRoute.route,
        NoteNewRoute.route,
        NoteNewDatedRoute.route ->
            pageOwnedShell()

        /*
         * ------------------------------------------------
         * Fallback
         * ------------------------------------------------
         */

        else ->
            MainShellState(
                title =
                    "TopRole",

                showTopBar =
                    false,

                showBack =
                    false,

                showBottomBar =
                    false,
            )
    }
}


private fun detailShell(
    title:
    String,
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

        showFriends =
            false,

        showAddFriend =
            false,

        showCreateGroup =
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

        showFriends =
            false,

        showAddFriend =
            false,

        showCreateGroup =
            false,

        showBottomBar =
            false,
    )
}