package top.role.android.feature.chat.conversation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import top.role.android.core.data.image.ImageRepository
import top.role.android.core.model.image.MobileImageScene
import top.role.android.core.model.chat.MobileChatMessage

class ChatConversationViewModel(
    private val conversationId: String,
    private val repository: ChatConversationRepository,
    private val imageRepository: ImageRepository,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            ChatConversationUiState()
        )

    val uiState:
            StateFlow<ChatConversationUiState> =
        _uiState.asStateFlow()

    private var conversationJob: Job? = null
    private var messagesJob: Job? = null

    init {
        start()
    }

    private fun start() {
        observeConversation()
        observeMessages()
        markOpened()
    }

    private fun observeConversation() {
        conversationJob?.cancel()

        conversationJob =
            viewModelScope.launch {
                repository
                    .observeConversation(
                        conversationId =
                            conversationId,
                    )
                    .collect { result ->

                        result
                            .onSuccess { detail ->
                                if (detail == null) {
                                    _uiState.value =
                                        _uiState.value.copy(
                                            loading = false,
                                            notFound = true,
                                            conversation = null,
                                            errorMessage = null,
                                        )
                                } else {
                                    _uiState.value =
                                        _uiState.value.copy(
                                            loading = false,
                                            notFound = false,
                                            conversation = detail,
                                            errorMessage = null,
                                        )
                                }
                            }
                            .onFailure {
                                _uiState.value =
                                    _uiState.value.copy(
                                        loading = false,
                                        errorMessage =
                                            "暂时无法加载会话。",
                                    )
                            }
                    }
            }
    }

    private fun observeMessages() {
        messagesJob?.cancel()

        messagesJob =
            viewModelScope.launch {
                repository
                    .observeMessages(
                        conversationId =
                            conversationId,
                    )
                    .collect { result ->

                        result
                            .onSuccess { page ->

                                val current =
                                    _uiState.value

                                /*
                                 * 第一页是最新消息。
                                 *
                                 * 如果之前已经加载过历史，
                                 * 不能直接 page.page 覆盖，
                                 * 否则历史消息会消失。
                                 */
                                val latestIds =
                                    page.page
                                        .map {
                                            it.messageId
                                        }
                                        .toSet()

                                val olderMessages =
                                    current.messages
                                        .filter {
                                            it.messageId !in latestIds
                                        }

                                val merged =
                                    (
                                            olderMessages +
                                                    page.page
                                            )
                                        .distinctBy {
                                            it.messageId
                                        }
                                        .sortedBy {
                                            it.createdAt
                                        }

                                _uiState.value =
                                    current.copy(
                                        messages =
                                            merged,

                                        messagesLoading =
                                            false,

                                        messagesDone =
                                            page.isDone,

                                        continueCursor =
                                            page.continueCursor,
                                    )
                            }
                            .onFailure {
                                _uiState.value =
                                    _uiState.value.copy(
                                        messagesLoading =
                                            false,

                                        errorMessage =
                                            "暂时无法加载消息。",
                                    )
                            }
                    }
            }
    }

    fun loadMoreMessages() {
        val current =
            _uiState.value

        if (
            current.loadingMore ||
            current.messagesDone ||
            current.continueCursor.isNullOrBlank()
        ) {
            return
        }

        viewModelScope.launch {
            _uiState.value =
                current.copy(
                    loadingMore = true,
                )

            runCatching {
                repository
                    .observeMessages(
                        conversationId =
                            conversationId,

                        cursor =
                            current.continueCursor,
                    )
                    .first()
                    .getOrThrow()
            }
                .onSuccess { page ->

                    val latest =
                        _uiState.value

                    val merged =
                        (
                                page.page +
                                        latest.messages
                                )
                            .distinctBy {
                                it.messageId
                            }
                            .sortedBy {
                                it.createdAt
                            }

                    _uiState.value =
                        latest.copy(
                            messages =
                                merged,

                            loadingMore =
                                false,

                            messagesDone =
                                page.isDone,

                            continueCursor =
                                page.continueCursor,
                        )
                }
                .onFailure {
                    _uiState.value =
                        _uiState.value.copy(
                            loadingMore = false,
                        )
                }
        }
    }

    private fun markOpened() {
        viewModelScope.launch {
            runCatching {
                repository.markOpened(
                    conversationId =
                        conversationId,
                )
            }
        }
    }

    fun onInputTextChange(
        value: String,
    ) {
        _uiState.value =
            _uiState.value.copy(
                inputText = value,
                sendErrorMessage = null,
            )
    }

    fun sendText() {
        val current =
            _uiState.value

        val text =
            current.inputText.trim()

        if (
            text.isEmpty() ||
            current.sending ||
            current.imageSending
        ) {
            return
        }

        val replyToMessageId =
            current.replyTarget?.messageId

        viewModelScope.launch {
            _uiState.value =
                _uiState.value.copy(
                    sending = true,
                    sendErrorMessage = null,
                )

            runCatching {
                repository.sendText(
                    conversationId =
                        conversationId,

                    text =
                        text,

                    replyToMessageId =
                        replyToMessageId,
                )
            }.onSuccess {

                _uiState.value =
                    _uiState.value.copy(
                        inputText = "",
                        sending = false,
                        replyTarget = null,
                    )
            }.onFailure { error ->
                _uiState.value =
                    _uiState.value.copy(
                        sending = false,

                        sendErrorMessage =
                            mapChatSendError(
                                error
                            ),
                    )
            }
        }
    }

    fun onImageSelected(
        uri: Uri,
    ) {
        val current =
            _uiState.value

        if (
            current.imageSending ||
            current.sending
        ) {
            return
        }

        _uiState.value =
            current.copy(
                imageDraft =
                    ChatImageDraft(
                        sourceUri =
                            uri,
                    ),

                imageSendErrorMessage =
                    null,
            )
    }

    fun cancelImageDraft() {
        val current =
            _uiState.value

        if (
            current.imageSending
        ) {
            return
        }

        _uiState.value =
            current.copy(
                imageDraft = null,
                imageSendErrorMessage = null,
            )
    }

    fun sendImage() {
        val current =
            _uiState.value

        val draft =
            current.imageDraft
                ?: return

        if (
            current.imageSending ||
            current.sending
        ) {
            return
        }

        val replyToMessageId =
            current.replyTarget
                ?.messageId

        viewModelScope.launch {
            _uiState.value =
                _uiState.value.copy(
                    imageSending = true,
                    imageSendErrorMessage = null,

                    imageDraft =
                        draft.copy(
                            uploading =
                                draft.uploadedImageId == null,

                            sending =
                                draft.uploadedImageId != null,

                            errorMessage =
                                null,
                        ),
                )

            runCatching {
                /*
                 * 如果之前已经上传成功，
                 * retry 直接使用已有 imageId。
                 */
                val imageId =
                    draft.uploadedImageId
                        ?: run {
                            val asset =
                                imageRepository
                                    .uploadPrivateImage(
                                        sourceUri =
                                            draft.sourceUri,

                                        scene =
                                            MobileImageScene
                                                .Conversation,
                                    )

                            val uploadedId =
                                asset.id

                            val latest =
                                _uiState.value

                            _uiState.value =
                                latest.copy(
                                    imageDraft =
                                        latest.imageDraft?.copy(
                                            uploadedImageId =
                                                uploadedId,

                                            uploading =
                                                false,

                                            sending =
                                                true,
                                        ),
                                )

                            uploadedId
                        }

                repository.sendImage(
                    conversationId =
                        conversationId,

                    imageId =
                        imageId,

                    /*
                     * Android 当前不提供 caption UI。
                     */
                    caption =
                        null,

                    replyToMessageId =
                        replyToMessageId,
                )
            }.onSuccess {
                _uiState.value =
                    _uiState.value.copy(
                        imageDraft = null,

                        imageSending = false,
                        imageSendErrorMessage = null,

                        replyTarget = null,
                    )
            }.onFailure { error ->
                val latest =
                    _uiState.value

                _uiState.value =
                    latest.copy(
                        imageSending = false,

                        imageSendErrorMessage =
                            mapChatImageSendError(
                                error
                            ),

                        /*
                         * 保留 draft。
                         *
                         * 如果 upload 已经成功，
                         * uploadedImageId 也会继续保留，
                         * 下一次 retry 不再上传。
                         */
                        imageDraft =
                            latest.imageDraft
                                ?.copy(
                                    uploading = false,
                                    sending = false,
                                    errorMessage =
                                        mapChatImageSendError(
                                            error
                                        ),
                                ),
                    )
            }
        }
    }

    fun startReply(
        message: MobileChatMessage,
    ) {
        if (message.deletedAt != null) {
            return
        }

        val senderName =
            message.sender?.displayName
                ?.takeIf { it.isNotBlank() }
                ?: if (message.mine) {
                    "我"
                } else {
                    "用户"
                }

        val image =
            if (message.kind == "image") {
                message.attachments
                    ?.firstOrNull()
            } else {
                null
            }

        _uiState.value =
            _uiState.value.copy(
                replyTarget =
                    ChatReplyTarget(
                        messageId =
                            message.messageId,

                        senderName =
                            senderName,

                        kind =
                            message.kind,

                        text =
                            message.text,

                        imageId =
                            image?.imageId
                                ?.takeIf {
                                    it.isNotBlank()
                                },
                    ),

                sendErrorMessage = null,
                imageSendErrorMessage = null,
            )

    }

    fun cancelReply() {
        _uiState.value =
            _uiState.value.copy(
                replyTarget = null,
            )
    }

    fun recallMessage(
        message: MobileChatMessage,
    ) {
        if (
            !message.mine ||
            message.deletedAt != null
        ) {
            return
        }

        operateMessage(
            messageId =
                message.messageId,
        ) {
            repository.recallMessage(
                message.messageId
            )
        }
    }

    fun deleteMessageForMe(
        message: MobileChatMessage,
    ) {
        operateMessage(
            messageId =
                message.messageId,

            onSuccess = {
                val latest =
                    _uiState.value

                _uiState.value =
                    latest.copy(
                        messages =
                            latest.messages.filterNot {
                                it.messageId ==
                                        message.messageId
                            },

                        replyTarget =
                            latest.replyTarget
                                ?.takeUnless {
                                    it.messageId ==
                                            message.messageId
                                },
                    )
            },
        ) {
            repository.deleteMessageForMe(
                message.messageId
            )
        }
    }

    fun retry() {
        _uiState.value =
            ChatConversationUiState()

        start()
    }

    override fun onCleared() {
        conversationJob?.cancel()
        messagesJob?.cancel()

        super.onCleared()
    }

    private fun operateMessage(
        messageId: String,
        onSuccess: () -> Unit = {},
        operation: suspend () -> Unit,
    ) {
        val current =
            _uiState.value

        if (
            current.operatingMessageId != null
        ) {
            return
        }

        viewModelScope.launch {
            _uiState.value =
                _uiState.value.copy(
                    operatingMessageId =
                        messageId,

                    messageOperationError =
                        null,
                )

            runCatching {
                operation()
            }.onSuccess {
                onSuccess()

                _uiState.value =
                    _uiState.value.copy(
                        operatingMessageId =
                            null,

                        messageOperationError =
                            null,
                    )
            }.onFailure { error ->
                _uiState.value =
                    _uiState.value.copy(
                        operatingMessageId =
                            null,

                        messageOperationError =
                            mapChatMessageOperationError(
                                error
                            ),
                    )
            }
        }
    }

    private fun mapChatSendError(
        error: Throwable,
    ): String {
        val raw =
            error.message.orEmpty()

        return when {
            raw.contains(
                "NOT_FRIEND",
                ignoreCase = true,
            ) ->
                "你们当前不是好友，无法发送消息。"

            raw.contains(
                "USER_BLOCKED",
                ignoreCase = true,
            ) ->
                "你已将对方加入黑名单，无法发送消息。"

            raw.contains(
                "BLOCKED_BY_USER",
                ignoreCase = true,
            ) ->
                "当前无法向对方发送消息。"

            raw.contains(
                "NOT_CONVERSATION_PARTICIPANT",
                ignoreCase = true,
            ) ->
                "你已不在当前会话中。"

            raw.contains(
                "REPLY_MESSAGE_NOT_FOUND",
                ignoreCase = true,
            ) ->
                "回复的消息已不可用，请取消回复后重试。"

            else ->
                "消息发送失败，请稍后再试。"
        }
    }

    private fun mapChatImageSendError(
        error: Throwable,
    ): String {
        val raw =
            error.message.orEmpty()

        return when {
            raw.contains(
                "IMAGE_NOT_FOUND",
                ignoreCase = true,
            ) ->
                "图片已不可用，请重新选择。"

            raw.contains(
                "IMAGE_NOT_OWNED_BY_USER",
                ignoreCase = true,
            ) ->
                "无法使用这张图片，请重新选择。"

            raw.contains(
                "IMAGE_NOT_CONVERSATION_IMAGE",
                ignoreCase = true,
            ) ->
                "图片状态异常，请重新选择。"

            raw.contains(
                "REPLY_MESSAGE_NOT_FOUND",
                ignoreCase = true,
            ) ->
                "回复的消息已不可用，请取消回复后重试。"

            raw.contains(
                "NOT_FRIEND",
                ignoreCase = true,
            ) ->
                "你们当前不是好友，无法发送图片。"

            raw.contains(
                "USER_BLOCKED",
                ignoreCase = true,
            ) ->
                "你已将对方加入黑名单，无法发送图片。"

            raw.contains(
                "BLOCKED_BY_USER",
                ignoreCase = true,
            ) ->
                "当前无法向对方发送图片。"

            else ->
                "图片发送失败，请重试。"
        }
    }

    private fun mapChatMessageOperationError(
        error: Throwable,
    ): String {
        val raw =
            error.message.orEmpty()

        return when {
            raw.contains(
                "ONLY_SENDER_CAN_RECALL_MESSAGE",
                ignoreCase = true,
            ) ->
                "只能撤回自己发送的消息。"

            raw.contains(
                "NOT_CONVERSATION_PARTICIPANT",
                ignoreCase = true,
            ) ->
                "你已不在当前会话中。"

            else ->
                "操作失败，请稍后再试。"
        }
    }

}