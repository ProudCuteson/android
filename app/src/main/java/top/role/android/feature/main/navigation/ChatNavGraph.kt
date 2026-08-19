package top.role.android.feature.main.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

import top.role.android.app.navigation.ChatConversationRoute
import top.role.android.app.navigation.MainDestination

import top.role.android.feature.chat.ChatScreen

import top.role.android.feature.chat.conversation.ChatConversationScreen
import top.role.android.feature.chat.conversation.ChatConversationViewModel
import top.role.android.feature.chat.conversation.ChatConversationViewModelFactory

import top.role.android.feature.chat.conversationlist.ChatConversationListViewModel
import top.role.android.feature.chat.conversationlist.ChatConversationListViewModelFactory

import top.role.android.app.navigation.ChatFriendsRoute
import top.role.android.app.navigation.ChatFriendLookupRoute
import top.role.android.app.navigation.ChatGroupCreateRoute
import top.role.android.app.navigation.ChatGroupSettingsRoute

import top.role.android.feature.chat.friends.ChatFriendsScreen
import top.role.android.feature.chat.friends.ChatFriendsViewModel
import top.role.android.feature.chat.friends.ChatFriendsViewModelFactory
import top.role.android.feature.chat.friendlookup.ChatFriendLookupScreen
import top.role.android.feature.chat.friendlookup.ChatFriendLookupViewModel
import top.role.android.feature.chat.friendlookup.ChatFriendLookupViewModelFactory

import top.role.android.feature.chat.group.ChatGroupCreateViewModel
import top.role.android.feature.chat.group.ChatGroupCreateViewModelFactory
import top.role.android.feature.chat.group.ChatGroupCreateScreen

import top.role.android.feature.chat.group.setting.ChatGroupSettingsViewModel
import top.role.android.feature.chat.group.setting.ChatGroupSettingsScreen
import top.role.android.feature.chat.group.setting.ChatGroupSettingsViewModelFactory

fun NavGraphBuilder.chatNavGraph(
    navController: NavHostController,
    dependencies: MainNavigationDependencies,
) {

    composable(
        route = MainDestination.Chat.route,
    ) {
        val factory =
            remember(
                dependencies
                    .chatConversationListRepository,
            ) {
                ChatConversationListViewModelFactory(
                    repository =
                        dependencies
                            .chatConversationListRepository,
                )
            }

        val chatViewModel:
                ChatConversationListViewModel =
            viewModel(
                key = "chat-conversation-list",
                factory = factory,
            )

        LaunchedEffect(
            dependencies.remoteAuthenticated,
        ) {
            if (
                dependencies.remoteAuthenticated
            ) {
                chatViewModel.start()
            } else {
                chatViewModel.showSignedOut()
            }
        }

        val state by
        chatViewModel
            .uiState
            .collectAsStateWithLifecycle()

        ChatScreen(
            state = state,
            onRetry =
                chatViewModel::retry,
            onConversationClick = { conversationId ->
                navController.navigate(
                    ChatConversationRoute.create(
                        conversationId = conversationId,
                    )
                )
            },
        )
    }

    composable(
        route =
            ChatConversationRoute.route,

        arguments =
            listOf(
                navArgument(
                    ChatConversationRoute
                        .conversationIdArg
                ) {
                    type =
                        NavType.StringType
                }
            ),
    ) { backStackEntry ->

        val conversationId =
            backStackEntry
                .arguments
                ?.getString(
                    ChatConversationRoute
                        .conversationIdArg
                )
                .orEmpty()

        val factory =
            remember(
                conversationId,
                dependencies
                    .chatConversationRepository,
            ) {
                ChatConversationViewModelFactory(
                    conversationId =
                        conversationId,

                    repository =
                        dependencies
                            .chatConversationRepository,

                    imageRepository =
                        dependencies
                            .imageRepository,
                )
            }

        val conversationViewModel:
                ChatConversationViewModel =
            viewModel(
                key =
                    "chat-conversation:$conversationId",

                factory =
                    factory,
            )

        val state by
        conversationViewModel
            .uiState
            .collectAsStateWithLifecycle()

        ChatConversationScreen(
            state =
                state,

            onBack = {
                navController.navigate(
                    MainDestination.Chat.route
                ) {
                    popUpTo(
                        MainDestination.Chat.route
                    ) {
                        inclusive =
                            false
                    }

                    launchSingleTop =
                        true
                }
            },

            onInputTextChange =
                conversationViewModel::
                onInputTextChange,

            onSendText =
                conversationViewModel::
                sendText,

            onImageSelected =
                conversationViewModel::
                onImageSelected,

            onCancelImageDraft =
                conversationViewModel::
                cancelImageDraft,

            onSendImage =
                conversationViewModel::
                sendImage,

            onStartReply =
                conversationViewModel::
                startReply,

            onCancelReply =
                conversationViewModel::
                cancelReply,

            onRecallMessage =
                conversationViewModel::
                recallMessage,

            onDeleteMessageForMe =
                conversationViewModel::
                deleteMessageForMe,

            onLoadMore =
                conversationViewModel::
                loadMoreMessages,

            onRetry =
                conversationViewModel::
                retry,

            onGroupSettingsClick = {
                    conversationId ->

                navController.navigate(
                    ChatGroupSettingsRoute.create(
                        conversationId =
                            conversationId,
                    )
                )
            },
        )

    }

    composable(
        route =
            ChatFriendsRoute.route,
    ) {

        val factory =
            remember(
                dependencies
                    .chatFriendsRepository,
            ) {
                ChatFriendsViewModelFactory(
                    repository =
                        dependencies
                            .chatFriendsRepository,
                )
            }

        val friendsViewModel:
                ChatFriendsViewModel =
            viewModel(
                key =
                    "chat-friends",

                factory =
                    factory,
            )

        val state by
        friendsViewModel
            .uiState
            .collectAsStateWithLifecycle()

        ChatFriendsScreen(
            state =
                state,

            onAcceptInvite =
                friendsViewModel::
                acceptInvite,

            onRejectInvite =
                friendsViewModel::
                rejectInvite,

            onFriendClick = {
                    userId ->

                friendsViewModel
                    .openConversation(
                        userId =
                            userId,

                        onSuccess = {
                                conversationId ->

                            navController.navigate(
                                ChatConversationRoute.create(
                                    conversationId =
                                        conversationId,
                                )
                            )
                        },
                    )
            },

            onRetry =
                friendsViewModel::
                retry,
        )
    }

    composable(
        route =
            ChatFriendLookupRoute.route,
    ) {

        val factory =
            remember(
                dependencies
                    .chatFriendsRepository,
            ) {
                ChatFriendLookupViewModelFactory(
                    repository =
                        dependencies
                            .chatFriendsRepository,
                )
            }

        val lookupViewModel:
                ChatFriendLookupViewModel =
            viewModel(
                key =
                    "chat-friend-lookup",

                factory =
                    factory,
            )

        val state by
        lookupViewModel
            .uiState
            .collectAsStateWithLifecycle()

        ChatFriendLookupScreen(
            state =
                state,

            onQueryChange =
                lookupViewModel::
                onQueryChange,

            onClear =
                lookupViewModel::
                clearQuery,

            onSearch =
                lookupViewModel::
                search,

            onAdd =
                lookupViewModel::
                sendInvite,

            onOpenChat = {
                    userId ->

                lookupViewModel
                    .openConversation(
                        userId =
                            userId,

                        onSuccess = {
                                conversationId ->

                            navController.navigate(
                                ChatConversationRoute.create(
                                    conversationId =
                                        conversationId,
                                )
                            )
                        },
                    )
            },
        )
    }

    composable(
        route =
            ChatGroupCreateRoute.route,
    ) {

        val factory =
            remember(
                dependencies.chatFriendsRepository,
                dependencies.chatGroupRepository,
            ) {
                ChatGroupCreateViewModelFactory(
                    friendsRepository =
                        dependencies.chatFriendsRepository,

                    repository =
                        dependencies.chatGroupRepository,
                )
            }

        val groupViewModel:
                ChatGroupCreateViewModel =
            viewModel(
                key =
                    "chat-group-create",

                factory =
                    factory,
            )

        val state by
        groupViewModel
            .uiState
            .collectAsStateWithLifecycle()

        ChatGroupCreateScreen(
            state =
                state,

            onTitleChange =
                groupViewModel::
                onTitleChange,

            onToggleMember =
                groupViewModel::
                toggleMember,

            onCreate = {

                groupViewModel.createGroup(
                    onSuccess = {
                            conversationId ->

                        navController.navigate(
                            ChatConversationRoute.create(
                                conversationId =
                                    conversationId,
                            )
                        ) {
                            /*
                             * 创建完成后不希望返回键
                             * 再回到创建表单。
                             */
                            popUpTo(
                                ChatGroupCreateRoute.route
                            ) {
                                inclusive =
                                    true
                            }
                        }
                    },
                )
            },

            onRetry =
                groupViewModel::
                retry,
        )
    }

    composable(
        route =
            ChatGroupSettingsRoute.route,
    ) { backStackEntry ->

        val conversationId =
            backStackEntry
                .arguments
                ?.getString(
                    ChatGroupSettingsRoute
                        .conversationIdArg
                )
                ?: return@composable

        val factory =
            remember(
                conversationId,
                dependencies.localOwnerUserId,
                dependencies.chatGroupRepository,
                dependencies.chatFriendsRepository,
            ) {

                ChatGroupSettingsViewModelFactory(
                    conversationId =
                        conversationId,

                    currentUserId =
                        dependencies.localOwnerUserId,

                    groupRepository =
                        dependencies.chatGroupRepository,

                    friendsRepository =
                        dependencies.chatFriendsRepository,
                )
            }

        val viewModel:
                ChatGroupSettingsViewModel =
            viewModel(
                key =
                    "chat-group-$conversationId",

                factory =
                    factory,
            )

        val state by
        viewModel
            .uiState
            .collectAsStateWithLifecycle()

        ChatGroupSettingsScreen(
            state =
                state,

            onUpdateTitle =
                viewModel::updateTitle,

            onAddMembers =
                viewModel::addMembers,

            onRemoveMember =
                viewModel::removeMember,

            onTransferOwner =
                viewModel::transferOwner,

            onLeave = {

                viewModel.leaveGroup {

                    /*
                     * 离开后不能再回 Conversation。
                     */
                    navController.navigate(
                        MainDestination.Chat.route
                    ) {

                        popUpTo(
                            MainDestination.Chat.route
                        ) {
                            inclusive =
                                false
                        }

                        launchSingleTop =
                            true
                    }
                }
            },
        )
    }

}