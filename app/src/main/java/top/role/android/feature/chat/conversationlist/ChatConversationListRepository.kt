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
            name = "mobile/ios/chat:listMobileConversations",
            args = emptyMap(),
        )
    }
}