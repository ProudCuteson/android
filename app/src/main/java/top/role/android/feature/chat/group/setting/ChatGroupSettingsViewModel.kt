package top.role.android.feature.chat.group.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import top.role.android.feature.chat.friends.ChatFriendsRepository
import top.role.android.feature.chat.group.ChatGroupRepository

class ChatGroupSettingsViewModel(
    private val conversationId:
    String,

    currentUserId:
    String?,

    private val groupRepository:
    ChatGroupRepository,

    private val friendsRepository:
    ChatFriendsRepository,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            ChatGroupSettingsUiState(
                currentUserId =
                    currentUserId,
            )
        )

    val uiState:
            StateFlow<ChatGroupSettingsUiState> =
        _uiState.asStateFlow()

    private var conversationJob:
            Job? = null

    private var friendsJob:
            Job? = null

    init {
        observeConversation()
        observeFriends()
    }

    private fun observeConversation() {
        conversationJob?.cancel()

        conversationJob =
            viewModelScope.launch {

                groupRepository
                    .observeConversation(
                        conversationId =
                            conversationId,
                    )
                    .collect { result ->

                        result
                            .onSuccess { conversation ->

                                _uiState.value =
                                    _uiState.value.copy(
                                        loading =
                                            false,

                                        conversation =
                                            conversation,

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
                                            "无法加载群聊信息。",
                                    )
                            }
                    }
            }
    }

    private fun observeFriends() {
        friendsJob?.cancel()

        friendsJob =
            viewModelScope.launch {

                friendsRepository
                    .observeFriends()
                    .collect { result ->

                        result.onSuccess { friends ->

                            _uiState.value =
                                _uiState.value.copy(
                                    friends =
                                        friends,
                                )
                        }
                    }
            }
    }

    fun updateTitle(
        title: String,
    ) {
        val value =
            title.trim()

        if (
            value.isEmpty() ||
            _uiState.value.processing
        ) {
            return
        }

        viewModelScope.launch {

            setProcessing(
                true
            )

            runCatching {
                groupRepository
                    .updateGroupTitle(
                        conversationId =
                            conversationId,

                        title =
                            value,
                    )
            }
                .onSuccess {
                    setProcessing(
                        false
                    )
                }
                .onFailure {
                    fail(
                        "修改群名称失败。"
                    )
                }
        }
    }

    fun addMembers(
        userIds: List<String>,
    ) {
        if (
            userIds.isEmpty() ||
            _uiState.value.processing
        ) {
            return
        }

        viewModelScope.launch {

            setProcessing(
                true
            )

            runCatching {
                groupRepository.addMembers(
                    conversationId =
                        conversationId,

                    userIds =
                        userIds,
                )
            }
                .onSuccess {
                    setProcessing(
                        false
                    )
                }
                .onFailure {
                    fail(
                        "添加成员失败。"
                    )
                }
        }
    }

    fun removeMember(
        userId: String,
    ) {
        if (
            _uiState.value.processing
        ) {
            return
        }

        viewModelScope.launch {

            setProcessing(
                true
            )

            runCatching {
                groupRepository
                    .removeMember(
                        conversationId =
                            conversationId,

                        userId =
                            userId,
                    )
            }
                .onSuccess {
                    setProcessing(
                        false
                    )
                }
                .onFailure {
                    fail(
                        "移除成员失败。"
                    )
                }
        }
    }

    fun transferOwner(
        userId: String,
    ) {
        if (
            _uiState.value.processing
        ) {
            return
        }

        viewModelScope.launch {

            setProcessing(
                true
            )

            runCatching {
                groupRepository
                    .transferOwner(
                        conversationId =
                            conversationId,

                        targetUserId =
                            userId,
                    )
            }
                .onSuccess {
                    setProcessing(
                        false
                    )
                }
                .onFailure {
                    fail(
                        "转让群主失败。"
                    )
                }
        }
    }

    fun leaveGroup(
        onSuccess: () -> Unit,
    ) {
        if (
            _uiState.value.processing
        ) {
            return
        }

        viewModelScope.launch {

            setProcessing(
                true
            )

            runCatching {
                groupRepository
                    .leaveGroup(
                        conversationId =
                            conversationId,
                    )
            }
                .onSuccess {

                    setProcessing(
                        false
                    )

                    onSuccess()
                }
                .onFailure {
                    fail(
                        if (
                            _uiState.value.isOwner
                        ) {
                            "请先转让群主后再退出群聊。"
                        } else {
                            "退出群聊失败。"
                        }
                    )
                }
        }
    }

    private fun setProcessing(
        value: Boolean,
    ) {
        _uiState.value =
            _uiState.value.copy(
                processing =
                    value,

                errorMessage =
                    null,
            )
    }

    private fun fail(
        message: String,
    ) {
        _uiState.value =
            _uiState.value.copy(
                processing =
                    false,

                errorMessage =
                    message,
            )
    }

    override fun onCleared() {
        conversationJob?.cancel()
        friendsJob?.cancel()

        super.onCleared()
    }
}