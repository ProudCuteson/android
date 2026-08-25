package top.role.android.feature.chat.conversationlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatConversationListViewModel(
    private val repository: ChatConversationListRepository,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<ChatConversationListUiState>(
            ChatConversationListUiState.Loading
        )

    val uiState: StateFlow<ChatConversationListUiState> =
        _uiState.asStateFlow()

    private var observeJob: Job? =
        null


    fun start() {
        if (
            observeJob != null
        ) {
            return
        }

        _uiState.value =
            ChatConversationListUiState.Loading

        observeJob =
            viewModelScope.launch {

                repository
                    .observeConversations()
                    .collect { result ->

                        result
                            .onSuccess { conversations ->

                                _uiState.value =
                                    ChatConversationListUiState.Content(
                                        conversations =
                                            conversations,
                                    )
                            }
                            .onFailure {

                                _uiState.value =
                                    ChatConversationListUiState.Error(
                                        message =
                                            "暂时无法加载聊天，请稍后再试。",
                                    )
                            }
                    }
            }
    }


    fun hideConversation(
        conversationId: String,
    ) {
        if (
            conversationId.isBlank()
        ) {
            return
        }

        viewModelScope.launch {

            runCatching {
                repository
                    .hideConversationForMe(
                        conversationId =
                            conversationId,
                    )
            }
                .onFailure {

                    /*
                     * 当前列表继续保持。
                     *
                     * 后续如果需要 Snackbar，
                     * 再单独增加 effect / message state，
                     * 不污染列表主 UiState。
                     */
                }
        }
    }


    fun showOffline() {
        stopSubscription()

        _uiState.value =
            ChatConversationListUiState.Offline
    }


    fun showSignedOut() {
        stopSubscription()

        _uiState.value =
            ChatConversationListUiState.SignedOut
    }


    fun retry() {
        stopSubscription()
        start()
    }


    private fun stopSubscription() {
        observeJob?.cancel()
        observeJob =
            null
    }


    override fun onCleared() {
        stopSubscription()

        super.onCleared()
    }
}