package top.role.android.feature.chat.friendlookup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

import top.role.android.feature.chat.friends.ChatFriendsRepository

class ChatFriendLookupViewModel(
    private val repository:
    ChatFriendsRepository,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            ChatFriendLookupUiState()
        )

    val uiState:
            StateFlow<ChatFriendLookupUiState> =
        _uiState.asStateFlow()

    private var searchJob:
            Job? = null

    fun onQueryChange(
        value: String,
    ) {
        _uiState.value =
            _uiState.value.copy(
                queryText = value,
                errorMessage = null,
            )
    }

    fun clearQuery() {
        searchJob?.cancel()

        _uiState.value =
            ChatFriendLookupUiState()
    }

    fun search() {
        val query =
            _uiState.value
                .queryText
                .trim()

        if (
            query.isEmpty()
        ) {
            return
        }

        searchJob?.cancel()

        searchJob =
            viewModelScope.launch {

                _uiState.value =
                    _uiState.value.copy(
                        loading = true,
                        results = emptyList(),
                        errorMessage = null,
                    )

                runCatching {
                    repository
                        .lookupUsers(
                            query = query,
                        )
                        .first()
                        .getOrThrow()
                }
                    .onSuccess { results ->

                        _uiState.value =
                            _uiState.value.copy(
                                loading = false,
                                results = results,
                                errorMessage = null,
                            )
                    }
                    .onFailure {

                        _uiState.value =
                            _uiState.value.copy(
                                loading = false,
                                errorMessage =
                                    "暂时无法搜索用户。",
                            )
                    }
            }
    }

    fun sendInvite(
        userId: String,
    ) {
        if (
            _uiState.value
                .sendingUserId != null
        ) {
            return
        }

        viewModelScope.launch {

            _uiState.value =
                _uiState.value.copy(
                    sendingUserId = userId,
                    errorMessage = null,
                )

            runCatching {
                repository.sendInvite(
                    targetUserId = userId,
                )
            }
                .onSuccess {

                    _uiState.value =
                        _uiState.value.copy(
                            sendingUserId = null,
                        )

                    /*
                     * 重新查一次，
                     * 让 relation 从 none
                     * 变为 outgoingPending。
                     */
                    search()
                }
                .onFailure {

                    _uiState.value =
                        _uiState.value.copy(
                            sendingUserId = null,
                            errorMessage =
                                "发送好友申请失败。",
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
                .sendingUserId != null
        ) {
            return
        }

        viewModelScope.launch {

            _uiState.value =
                _uiState.value.copy(
                    sendingUserId = userId,
                    errorMessage = null,
                )

            runCatching {
                repository
                    .openDirectConversation(
                        userId = userId,
                    )
            }
                .onSuccess { result ->

                    _uiState.value =
                        _uiState.value.copy(
                            sendingUserId = null,
                        )

                    onSuccess(
                        result.conversationId
                    )
                }
                .onFailure {

                    _uiState.value =
                        _uiState.value.copy(
                            sendingUserId = null,
                            errorMessage =
                                "暂时无法打开聊天。",
                        )
                }
        }
    }

    override fun onCleared() {
        searchJob?.cancel()

        super.onCleared()
    }
}