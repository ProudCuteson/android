package top.role.android.feature.chat.conversation

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

import top.role.android.core.convex.ConvexMobileClient
import top.role.android.core.model.chat.MobileChatMessagePage
import top.role.android.core.model.chat.MobileConversationDetail

@Serializable
data class MobileSendImageMessageResult(
    val ok: Boolean,
    val messageId: String,
    val imageId: String,
)

@Serializable
data class MutationOkResult(
    val ok: Boolean,
)

@Serializable
data class SendTextMessageResult(
    val ok: Boolean,
    val messageId: String,
)

class ChatConversationRepository(
    private val convexClient: ConvexMobileClient,
) {

    fun observeConversation(
        conversationId: String,
    ): Flow<Result<MobileConversationDetail?>> {
        require(
            conversationId.isNotBlank()
        ) {
            "CONVERSATION_ID_REQUIRED"
        }

        return convexClient.subscribe(
            name =
                "mobile/native/chat/conversation:getConversation",

            args =
                mapOf(
                    "conversationId" to
                            conversationId,
                ),
        )
    }

    fun observeMessages(
        conversationId: String,
        cursor: String? = null,
        numItems: Double = 30.0,
    ): Flow<Result<MobileChatMessagePage>> {
        require(
            conversationId.isNotBlank()
        ) {
            "CONVERSATION_ID_REQUIRED"
        }

        return convexClient.subscribe(
            name =
                "mobile/native/chat/message:listMessagesPaginated",

            args =
                mapOf(
                    "conversationId" to
                            conversationId,

                    "paginationOpts" to
                            mapOf(
                                "numItems" to
                                        numItems,

                                "cursor" to
                                        cursor,
                            ),
                ),
        )
    }

    suspend fun markOpened(
        conversationId: String,
    ) {
        require(
            conversationId.isNotBlank()
        ) {
            "CONVERSATION_ID_REQUIRED"
        }

        convexClient.mutation<MutationOkResult>(
            name =
                "mobile/native/chat/conversation:markConversationOpened",

            args =
                mapOf(
                    "conversationId" to
                            conversationId,
                ),
        )
    }

    suspend fun sendText(
        conversationId: String,
        text: String,
        replyToMessageId: String? = null,
    ): SendTextMessageResult {
        require(
            conversationId.isNotBlank()
        ) {
            "CONVERSATION_ID_REQUIRED"
        }

        val normalizedText =
            text.trim()

        require(
            normalizedText.isNotEmpty()
        ) {
            "MESSAGE_TEXT_REQUIRED"
        }

        val args =
            mutableMapOf<String, Any?>(
                "conversationId" to
                        conversationId,

                "text" to
                        normalizedText,
            )

        replyToMessageId
            ?.takeIf {
                it.isNotBlank()
            }
            ?.let {
                args["replyToMessageId"] =
                    it
            }

        return convexClient.mutation(
            name =
                "mobile/native/chat/message:sendTextMessage",

            args =
                args,
        )
    }

    suspend fun sendImage(
        conversationId: String,
        imageId: String,
        caption: String? = null,
        replyToMessageId: String? = null,
    ): MobileSendImageMessageResult {
        require(
            conversationId.isNotBlank()
        ) {
            "CONVERSATION_ID_REQUIRED"
        }

        require(
            imageId.isNotBlank()
        ) {
            "IMAGE_ID_REQUIRED"
        }

        val args =
            mutableMapOf<String, Any?>(
                "conversationId" to
                        conversationId,

                "imageId" to
                        imageId,
            )

        caption
            ?.trim()
            ?.takeIf {
                it.isNotEmpty()
            }
            ?.let {
                args["caption"] =
                    it
            }

        replyToMessageId
            ?.takeIf {
                it.isNotBlank()
            }
            ?.let {
                args["replyToMessageId"] =
                    it
            }

        return convexClient.action(
            name =
                "mobile/native/chat/image:sendImageMessage",

            args =
                args,
        )
    }

    suspend fun recallMessage(
        messageId: String,
    ) {
        require(
            messageId.isNotBlank()
        ) {
            "MESSAGE_ID_REQUIRED"
        }

        convexClient.mutation<MutationOkResult>(
            name =
                "mobile/native/chat/message:recallMessage",

            args =
                mapOf(
                    "messageId" to
                            messageId,
                ),
        )
    }

    suspend fun deleteMessageForMe(
        messageId: String,
    ) {
        require(
            messageId.isNotBlank()
        ) {
            "MESSAGE_ID_REQUIRED"
        }

        convexClient.mutation<MutationOkResult>(
            name =
                "mobile/native/chat/message:deleteMessageForMe",

            args =
                mapOf(
                    "messageId" to
                            messageId,
                ),
        )
    }
}