package top.role.android.feature.chat.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import top.role.android.feature.chat.friends.ChatFriendsRepository

class ChatGroupCreateViewModel(
    private val friendsRepository:
    ChatFriendsRepository,

    private val repository:
    ChatGroupRepository,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            ChatGroupCreateUiState()
        )

    val uiState:
            StateFlow<ChatGroupCreateUiState> =
        _uiState.asStateFlow()

    private var friendsJob:
            Job? = null

    init {
        observeFriends()
    }

    private fun observeFriends() {
        friendsJob?.cancel()

        friendsJob =
            viewModelScope.launch {

                friendsRepository
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

    fun onTitleChange(
        value: String,
    ) {
        _uiState.value =
            _uiState.value.copy(
                title =
                    value,

                errorMessage =
                    null,
            )
    }

    fun toggleMember(
        userId: String,
    ) {
        val current =
            _uiState.value
                .selectedUserIds

        val next =
            if (
                userId in current
            ) {
                current - userId
            } else {
                current + userId
            }

        _uiState.value =
            _uiState.value.copy(
                selectedUserIds =
                    next,
            )
    }

    fun createGroup(
        onSuccess: (String) -> Unit,
    ) {
        val current =
            _uiState.value

        val title =
            current
                .title
                .trim()

        if (
            title.isEmpty() ||
            current.selectedUserIds.isEmpty() ||
            current.creating
        ) {
            return
        }

        viewModelScope.launch {

            _uiState.value =
                current.copy(
                    creating =
                        true,

                    errorMessage =
                        null,
                )

            runCatching {

                repository.createGroup(
                    title =
                        title,

                    memberUserIds =
                        current
                            .selectedUserIds
                            .toList(),
                )
            }
                .onSuccess { result ->

                    _uiState.value =
                        _uiState.value.copy(
                            creating =
                                false,
                        )

                    onSuccess(
                        result.conversationId
                    )
                }
                .onFailure {

                    _uiState.value =
                        _uiState.value.copy(
                            creating =
                                false,

                            errorMessage =
                                "创建群聊失败。",
                        )
                }
        }
    }

    fun retry() {
        _uiState.value =
            _uiState.value.copy(
                loading =
                    true,

                errorMessage =
                    null,
            )

        observeFriends()
    }

    override fun onCleared() {
        friendsJob?.cancel()

        super.onCleared()
    }
}