package top.role.android.feature.chat.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatFriendsViewModel(
    private val repository:
    ChatFriendsRepository,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            ChatFriendsUiState()
        )

    val uiState:
            StateFlow<ChatFriendsUiState> =
        _uiState.asStateFlow()

    private var friendsJob:
            Job? = null

    private var invitesJob:
            Job? = null

    init {
        start()
    }

    private fun start() {
        observeFriends()
        observeInvites()
    }

    private fun observeFriends() {
        friendsJob?.cancel()

        friendsJob =
            viewModelScope.launch {

                repository
                    .observeFriends()
                    .collect { result ->

                        result
                            .onSuccess { friends ->

                                _uiState.value =
                                    _uiState.value.copy(
                                        loading =
                                            false,

                                        friends =
                                            friends,

                                        errorMessage =
                                            null,
                                    )
                            }
                            .onFailure {

                                _uiState.value =
                                    _uiState.value.copy(
                                        loading =
                                            false,

                                        errorMessage =
                                            "暂时无法加载好友。",
                                    )
                            }
                    }
            }
    }

    private fun observeInvites() {
        invitesJob?.cancel()

        invitesJob =
            viewModelScope.launch {

                repository
                    .observeInvites()
                    .collect { result ->

                        result
                            .onSuccess { invites ->

                                _uiState.value =
                                    _uiState.value.copy(
                                        invites =
                                            invites,
                                    )
                            }
                            .onFailure {

                                _uiState.value =
                                    _uiState.value.copy(
                                        errorMessage =
                                            "暂时无法加载好友申请。",
                                    )
                            }
                    }
            }
    }

    fun acceptInvite(
        inviteId: String,
    ) {
        if (
            _uiState.value
                .processingInviteId != null
        ) {
            return
        }

        viewModelScope.launch {

            _uiState.value =
                _uiState.value.copy(
                    processingInviteId =
                        inviteId,

                    errorMessage =
                        null,
                )

            runCatching {
                repository.acceptInvite(
                    inviteId =
                        inviteId,
                )
            }
                .onSuccess {

                    _uiState.value =
                        _uiState.value.copy(
                            processingInviteId =
                                null,
                        )

                    /*
                     * 不手工修改 invites / friends。
                     *
                     * Convex subscription 会自动刷新。
                     */
                }
                .onFailure {

                    _uiState.value =
                        _uiState.value.copy(
                            processingInviteId =
                                null,

                            errorMessage =
                                "接受好友申请失败。",
                        )
                }
        }
    }

    fun rejectInvite(
        inviteId: String,
    ) {
        if (
            _uiState.value
                .processingInviteId != null
        ) {
            return
        }

        viewModelScope.launch {

            _uiState.value =
                _uiState.value.copy(
                    processingInviteId =
                        inviteId,

                    errorMessage =
                        null,
                )

            runCatching {
                repository.rejectInvite(
                    inviteId =
                        inviteId,
                )
            }
                .onSuccess {

                    _uiState.value =
                        _uiState.value.copy(
                            processingInviteId =
                                null,
                        )
                }
                .onFailure {

                    _uiState.value =
                        _uiState.value.copy(
                            processingInviteId =
                                null,

                            errorMessage =
                                "拒绝好友申请失败。",
                        )
                }
        }
    }

    fun openConversation(
        userId: String,
        onSuccess: (String) -> Unit,
    ) {
        if (
            _uiState.value
                .openingUserId != null
        ) {
            return
        }

        viewModelScope.launch {

            _uiState.value =
                _uiState.value.copy(
                    openingUserId =
                        userId,

                    errorMessage =
                        null,
                )

            runCatching {
                repository
                    .openDirectConversation(
                        userId =
                            userId,
                    )
            }
                .onSuccess { result ->

                    val conversationId =
                        result.conversationId

                    if (
                        result.ok &&
                        !conversationId.isNullOrBlank()
                    ) {
                        _uiState.value =
                            _uiState.value.copy(
                                openingUserId =
                                    null,
                            )

                        onSuccess(
                            conversationId
                        )

                    } else {

                        _uiState.value =
                            _uiState.value.copy(
                                openingUserId =
                                    null,

                                errorMessage =
                                    when (
                                        result.code
                                    ) {
                                        "CANNOT_MESSAGE_SELF" ->
                                            "不能和自己聊天。"

                                        "USER_NOT_FOUND" ->
                                            "用户不存在。"

                                        "NOT_FRIENDS" ->
                                            "你们还不是好友。"

                                        else ->
                                            "暂时无法打开聊天。"
                                    },
                            )
                    }
                }
                .onFailure {

                    _uiState.value =
                        _uiState.value.copy(
                            openingUserId =
                                null,

                            errorMessage =
                                "暂时无法打开聊天。",
                        )
                }
        }
    }

    fun retry() {
        _uiState.value =
            ChatFriendsUiState()

        start()
    }

    override fun onCleared() {
        friendsJob?.cancel()
        invitesJob?.cancel()

        super.onCleared()
    }
}