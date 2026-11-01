package top.role.android.feature.main.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

import top.role.android.app.navigation.ChatConversationRoute
import top.role.android.app.navigation.ChatFriendsRoute
import top.role.android.app.navigation.ChatFriendLookupRoute
import top.role.android.app.navigation.ChatGroupCreateRoute
import top.role.android.app.navigation.ChatGroupSettingsRoute
import top.role.android.app.navigation.MainDestination
import top.role.android.core.auth.AuthState
import top.role.android.feature.chat.ChatScreen
import top.role.android.feature.chat.conversation.ChatConversationScreen
import top.role.android.feature.chat.conversation.ChatConversationViewModel
import top.role.android.feature.chat.conversation.ChatConversationViewModelFactory
import top.role.android.feature.chat.conversationlist.ChatConversationListViewModel
import top.role.android.feature.chat.conversationlist.ChatConversationListViewModelFactory
import top.role.android.feature.chat.friends.ChatFriendsScreen
import top.role.android.feature.chat.friends.ChatFriendsViewModel
import top.role.android.feature.chat.friends.ChatFriendsViewModelFactory
import top.role.android.feature.chat.friendlookup.ChatFriendLookupScreen
import top.role.android.feature.chat.friendlookup.ChatFriendLookupViewModel
import top.role.android.feature.chat.friendlookup.ChatFriendLookupViewModelFactory
import top.role.android.feature.chat.group.ChatGroupCreateScreen
import top.role.android.feature.chat.group.ChatGroupCreateViewModel
import top.role.android.feature.chat.group.ChatGroupCreateViewModelFactory
import top.role.android.feature.chat.group.setting.ChatGroupSettingsScreen
import top.role.android.feature.chat.group.setting.ChatGroupSettingsViewModel
import top.role.android.feature.chat.group.setting.ChatGroupSettingsViewModelFactory
import top.role.android.ui.auth.AuthStatusContent

fun NavGraphBuilder.chatNavGraph(
    navController: NavHostController,
    dependencies: MainNavigationDependencies,
    onLogin: () -> Unit,
    onRetryAuth: () -> Unit,
    onReSignIn: () -> Unit,
) {

    /*
     * Chat Home
     */
    composable(
        route = MainDestination.Chat.route,
    ) {

        val factory = remember(
            dependencies.chatConversationListRepository,
        ) {
            ChatConversationListViewModelFactory(
                repository = dependencies.chatConversationListRepository,
            )
        }

        val chatViewModel: ChatConversationListViewModel =
            viewModel(
                key = "chat-conversation-list",
                factory = factory,
            )

        LaunchedEffect(
            dependencies.authState,
            dependencies.remoteOnline,
        ) {
            if (
                dependencies.authState is AuthState.SignedIn &&
                dependencies.remoteOnline
            ) {
                chatViewModel.start()
            } else {
                chatViewModel.stop()
            }
        }

        ChatScreen(
            viewModel = chatViewModel,
            authState = dependencies.authState,
            remoteOnline = dependencies.remoteOnline,
            onLogin = onLogin,
            onRetryAuth = onRetryAuth,
            onReSignIn = onReSignIn,

            onConversationClick = { conversationId ->
                navController.navigate(
                    ChatConversationRoute.create(
                        conversationId = conversationId,
                    )
                )
            },

            onHideConversation = { conversationId ->
                chatViewModel.hideConversation(
                    conversationId = conversationId,
                )
            },
        )
    }


    /*
     * Conversation Detail
     */
    composable(
        route = ChatConversationRoute.route,
        arguments = listOf(
            navArgument(
                ChatConversationRoute.conversationIdArg
            ) {
                type = NavType.StringType
            }
        ),
    ) { backStackEntry ->

        val conversationId = backStackEntry
            .arguments
            ?.getString(ChatConversationRoute.conversationIdArg)
            .orEmpty()

        val factory = remember(
            conversationId,
            dependencies.chatConversationRepository,
        ) {
            ChatConversationViewModelFactory(
                conversationId = conversationId,
                repository = dependencies.chatConversationRepository,
                imageRepository = dependencies.imageRepository,
            )
        }

        val conversationViewModel: ChatConversationViewModel =
            viewModel(
                key = "chat-conversation:$conversationId",
                factory = factory,
            )

        val state by conversationViewModel
            .uiState
            .collectAsStateWithLifecycle()

        ChatConversationScreen(
            state = state,
            imagePreviewRepository = dependencies.chatImagePreviewRepository,

            onBack = {
                navController.navigate(
                    MainDestination.Chat.route
                ) {
                    popUpTo(MainDestination.Chat.route) {
                        inclusive = false
                    }
                    launchSingleTop = true
                }
            },

            onInputTextChange = conversationViewModel::onInputTextChange,
            onSendText = conversationViewModel::sendText,
            onImageSelected = conversationViewModel::onImageSelected,
            onCancelImageDraft = conversationViewModel::cancelImageDraft,
            onSendImage = conversationViewModel::sendImage,
            onStartReply = conversationViewModel::startReply,
            onCancelReply = conversationViewModel::cancelReply,
            onRecallMessage = conversationViewModel::recallMessage,
            onDeleteMessageForMe = conversationViewModel::deleteMessageForMe,
            onLoadMore = conversationViewModel::loadMoreMessages,
            onRetry = conversationViewModel::retry,

            onGroupSettingsClick = { conversationId ->
                navController.navigate(
                    ChatGroupSettingsRoute.create(
                        conversationId = conversationId,
                    )
                )
            },
        )
    }


    /*
     * Friends
     */
    composable(
        route = ChatFriendsRoute.route,
    ) {

        val factory = remember(
            dependencies.chatFriendsRepository,
        ) {
            ChatFriendsViewModelFactory(
                repository = dependencies.chatFriendsRepository,
            )
        }

        val friendsViewModel: ChatFriendsViewModel = viewModel(
            key = "chat-friends",
            factory = factory,
        )

        val state by friendsViewModel
            .uiState
            .collectAsStateWithLifecycle()

        ChatFriendsScreen(
            state = state,
            onAcceptInvite = friendsViewModel::acceptInvite,
            onRejectInvite = friendsViewModel::rejectInvite,

            onFriendClick = { userId ->
                friendsViewModel.openConversation(
                    userId = userId,
                    onSuccess = { conversationId ->
                        navController.navigate(
                            ChatConversationRoute.create(
                                conversationId = conversationId,
                            )
                        )
                    },
                )
            },

            onRetry = friendsViewModel::retry,
        )
    }


    /*
     * Friend Lookup
     */
    composable(
        route = ChatFriendLookupRoute.route,
    ) {

        val factory = remember(
            dependencies.chatFriendsRepository,
        ) {
            ChatFriendLookupViewModelFactory(
                repository = dependencies.chatFriendsRepository,
            )
        }

        val lookupViewModel: ChatFriendLookupViewModel = viewModel(
            key = "chat-friend-lookup",
            factory = factory,
        )

        val state by lookupViewModel
            .uiState
            .collectAsStateWithLifecycle()

        ChatFriendLookupScreen(
            state = state,
            onQueryChange = lookupViewModel::onQueryChange,
            onClear = lookupViewModel::clearQuery,
            onSearch = lookupViewModel::search,
            onAdd = lookupViewModel::sendInvite,

            onOpenChat = { userId ->
                lookupViewModel.openConversation(
                    userId = userId,
                    onSuccess = { conversationId ->
                        navController.navigate(
                            ChatConversationRoute.create(
                                conversationId = conversationId,
                            )
                        )
                    },
                )
            },
        )
    }


    /*
     * Group Create
     */
    composable(
        route = ChatGroupCreateRoute.route,
    ) {

        val factory = remember(
            dependencies.chatFriendsRepository,
            dependencies.chatGroupRepository,
        ) {
            ChatGroupCreateViewModelFactory(
                friendsRepository = dependencies.chatFriendsRepository,
                repository = dependencies.chatGroupRepository,
            )
        }

        val groupViewModel: ChatGroupCreateViewModel = viewModel(
            key = "chat-group-create",
            factory = factory,
        )

        val state by groupViewModel
            .uiState
            .collectAsStateWithLifecycle()

        ChatGroupCreateScreen(
            state = state,
            onTitleChange = groupViewModel::onTitleChange,
            onToggleMember = groupViewModel::toggleMember,

            onCreate = {
                groupViewModel.createGroup(
                    onSuccess = { conversationId ->
                        navController.navigate(
                            ChatConversationRoute.create(
                                conversationId = conversationId,
                            )
                        ) {
                            popUpTo(
                                ChatGroupCreateRoute.route
                            ) {
                                inclusive = true
                            }
                        }
                    },
                )
            },

            onRetry = groupViewModel::retry,
        )
    }


    /*
     * Group Settings
     */
    composable(
        route = ChatGroupSettingsRoute.route,
    ) { backStackEntry ->

        val conversationId = backStackEntry
            .arguments
            ?.getString(ChatGroupSettingsRoute.conversationIdArg)
            ?: return@composable

        AuthStatusContent(
            authState = dependencies.authState,
            onLogin = onLogin,
            onRetry = onRetryAuth,
            onReSignIn = onReSignIn,
        ) { currentUserId ->

            val factory = remember(
                conversationId,
                currentUserId,
                dependencies.chatGroupRepository,
                dependencies.chatFriendsRepository,
            ) {
                ChatGroupSettingsViewModelFactory(
                    conversationId = conversationId,
                    currentUserId = currentUserId,
                    groupRepository = dependencies.chatGroupRepository,
                    friendsRepository = dependencies.chatFriendsRepository,
                )
            }

            val groupSettingsViewModel: ChatGroupSettingsViewModel =
                viewModel(
                    key = "chat-group-$conversationId:$currentUserId",
                    factory = factory,
                )

            val state by groupSettingsViewModel
                .uiState
                .collectAsStateWithLifecycle()

            ChatGroupSettingsScreen(
                state = state,
                onUpdateTitle = groupSettingsViewModel::updateTitle,
                onAddMembers = groupSettingsViewModel::addMembers,
                onRemoveMember = groupSettingsViewModel::removeMember,
                onTransferOwner = groupSettingsViewModel::transferOwner,

                onLeave = {
                    groupSettingsViewModel.leaveGroup {
                        navController.navigate(
                            MainDestination.Chat.route
                        ) {
                            popUpTo(
                                MainDestination.Chat.route
                            ) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    }
                },
            )
        }
    }
}