package top.role.android.feature.chat.group

import kotlinx.coroutines.flow.Flow

import top.role.android.core.convex.ConvexMobileClient
import top.role.android.core.model.chat.MobileConversationDetail
import top.role.android.core.model.chat.MobileCreateGroupResult
import top.role.android.core.model.chat.MobileGroupActionResult
import top.role.android.core.model.chat.MobileTransferGroupOwnerResult

class ChatGroupRepository(
    private val convexClient:
    ConvexMobileClient,
) {

    fun observeConversation(
        conversationId: String,
    ): Flow<Result<MobileConversationDetail?>> {

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

    suspend fun createGroup(
        title: String,
        memberUserIds: List<String>,
    ): MobileCreateGroupResult {

        return convexClient.mutation(
            name =
                "mobile/native/chat/group:createGroup",

            args =
                mapOf(
                    "title" to title,
                    "memberUserIds" to memberUserIds,
                ),
        )
    }

    suspend fun updateGroupTitle(
        conversationId: String,
        title: String,
    ): MobileGroupActionResult {

        return convexClient.mutation(
            name =
                "mobile/native/chat/group:updateGroup",

            args =
                mapOf(
                    "conversationId" to
                            conversationId,

                    "title" to
                            title,
                ),
        )
    }

    suspend fun addMembers(
        conversationId: String,
        userIds: List<String>,
    ): MobileGroupActionResult {

        return convexClient.mutation(
            name =
                "mobile/native/chat/group:addGroupMembers",

            args =
                mapOf(
                    "conversationId" to
                            conversationId,

                    "userIds" to
                            userIds,
                ),
        )
    }

    suspend fun removeMember(
        conversationId: String,
        userId: String,
    ): MobileGroupActionResult {

        return convexClient.mutation(
            name =
                "mobile/native/chat/group:removeGroupMember",

            args =
                mapOf(
                    "conversationId" to
                            conversationId,

                    "userId" to
                            userId,
                ),
        )
    }

    suspend fun leaveGroup(
        conversationId: String,
    ): MobileGroupActionResult {

        return convexClient.mutation(
            name =
                "mobile/native/chat/group:leaveConversation",

            args =
                mapOf(
                    "conversationId" to
                            conversationId,
                ),
        )
    }

    suspend fun transferOwner(
        conversationId: String,
        targetUserId: String,
    ): MobileTransferGroupOwnerResult {

        return convexClient.mutation(
            name =
                "mobile/native/chat/group:transferGroupOwner",

            args =
                mapOf(
                    "conversationId" to
                            conversationId,

                    "targetUserId" to
                            targetUserId,
                ),
        )
    }
}