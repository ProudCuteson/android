package top.role.android.feature.chat.conversationlist

import kotlinx.coroutines.flow.Flow

import top.role.android.core.convex.ConvexMobileClient
import top.role.android.core.model.chat.MobileConversationListItem

class ChatConversationListRepository(
    private val convexClient: ConvexMobileClient,
) {

    fun observeConversations():
            Flow<Result<List<MobileConversationListItem>>> {

        return convexClient.subscribe(
            name =
                "mobile/native/chat/conversation:listConversations",

            args =
                emptyMap(),
        )
    }

    suspend fun hideConversationForMe(
        conversationId: String,
    ) {
        require(conversationId.isNotBlank()) {
            "CONVERSATION_ID_REQUIRED"
        }

        convexClient.mutation<Map<String, Any?>>(
            name =
                "mobile/native/chat/conversation:hideConversationForMe",

            args =
                mapOf(
                    "conversationId" to
                            conversationId,
                ),
        )
    }
}