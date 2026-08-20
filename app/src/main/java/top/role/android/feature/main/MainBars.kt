package top.role.android.feature.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AddComment
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.Today

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar

import androidx.compose.runtime.Composable

import androidx.compose.ui.graphics.vector.ImageVector

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

import top.role.android.app.navigation.MainDestination


private data class MainNavigationItem(
    val destination:
    MainDestination,

    val icon:
    ImageVector,
)


private val mainNavigationItems =
    listOf(
        MainNavigationItem(
            destination =
                MainDestination.Day,

            icon =
                Icons
                    .Outlined
                    .Today,
        ),

        MainNavigationItem(
            destination =
                MainDestination.Discover,

            icon =
                Icons
                    .Outlined
                    .Explore,
        ),

        MainNavigationItem(
            destination =
                MainDestination.Chat,

            icon =
                Icons
                    .Outlined
                    .ChatBubbleOutline,
        ),

        MainNavigationItem(
            destination =
                MainDestination.Engage,

            icon =
                Icons
                    .Outlined
                    .TaskAlt,
        ),
    )


@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun MainTopBar(
    state:
    MainShellState,

    onBack:
        () -> Unit,

    onLibrary:
        () -> Unit,

    onAccount:
        () -> Unit,

    onFriends:
        () -> Unit,

    onAddFriend:
        () -> Unit,

    onCreateGroup:
        () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text =
                    state.title,
            )
        },

        navigationIcon = {
            if (
                state.showBack
            ) {
                IconButton(
                    onClick =
                        onBack,
                ) {
                    Icon(
                        imageVector =
                            Icons
                                .AutoMirrored
                                .Filled
                                .ArrowBack,

                        contentDescription =
                            "返回",
                    )
                }
            }
        },

        actions = {

            /*
             * Day
             * ------------------------------------------------
             */

            if (
                state.showLibrary
            ) {
                IconButton(
                    onClick =
                        onLibrary,
                ) {
                    Icon(
                        imageVector =
                            Icons
                                .AutoMirrored
                                .Outlined
                                .LibraryBooks,

                        contentDescription =
                            "记录",
                    )
                }
            }

            if (
                state.showAccount
            ) {
                IconButton(
                    onClick =
                        onAccount,
                ) {
                    Icon(
                        imageVector =
                            Icons
                                .Outlined
                                .AccountCircle,

                        contentDescription =
                            "我的",
                    )
                }
            }

            /*
             * Chat
             * ------------------------------------------------
             */

            if (
                state.showFriends
            ) {
                IconButton(
                    onClick =
                        onFriends,
                ) {
                    Icon(
                        imageVector =
                            Icons
                                .Outlined
                                .Group,

                        contentDescription =
                            "好友",
                    )
                }
            }

            if (
                state.showCreateGroup
            ) {
                IconButton(
                    onClick =
                        onCreateGroup,
                ) {
                    Icon(
                        imageVector =
                            Icons
                                .Outlined
                                .AddComment,

                        contentDescription =
                            "创建群聊",
                    )
                }
            }

            /*
             * Friends
             * ------------------------------------------------
             */

            if (
                state.showAddFriend
            ) {
                IconButton(
                    onClick =
                        onAddFriend,
                ) {
                    Icon(
                        imageVector =
                            Icons
                                .Outlined
                                .PersonAdd,

                        contentDescription =
                            "添加好友",
                    )
                }
            }
        },
    )
}


@Composable
fun MainBottomBar(
    navController:
    NavHostController,

    currentRoute:
    String?,
) {
    NavigationBar {
        mainNavigationItems
            .forEach { item ->

                val destination =
                    item.destination

                val selected =
                    currentRoute ==
                            destination.route

                NavigationBarItem(
                    selected =
                        selected,

                    onClick = {
                        navController.navigate(
                            destination.route
                        ) {
                            popUpTo(
                                navController
                                    .graph
                                    .findStartDestination()
                                    .id
                            ) {
                                saveState =
                                    true
                            }

                            launchSingleTop =
                                true

                            restoreState =
                                true
                        }
                    },

                    icon = {
                        Icon(
                            imageVector =
                                item.icon,

                            contentDescription =
                                destination.label,
                        )
                    },

                    label = {
                        Text(
                            text =
                                destination.label,
                        )
                    },
                )
            }
    }
}