package top.role.android.feature.chat.friends

import kotlinx.coroutines.flow.Flow

import top.role.android.core.convex.ConvexMobileClient
import top.role.android.core.model.chat.MobileFriendInviteItem
import top.role.android.core.model.chat.MobileFriendItem
import top.role.android.core.model.chat.MobileFriendLookupItem
import top.role.android.core.model.chat.MobileFriendActionResult
import top.role.android.core.model.chat.MobileOpenFriendConversationResult

class ChatFriendsRepository(
    private val convexClient:
    ConvexMobileClient,
) {

    fun observeFriends():
            Flow<Result<List<MobileFriendItem>>> {

        return convexClient.subscribe(
            name =
                "mobile/native/friend/index:listFriends",

            args =
                emptyMap(),
        )
    }

    fun observeInvites():
            Flow<Result<List<MobileFriendInviteItem>>> {

        return convexClient.subscribe(
            name =
                "mobile/native/friend/index:listFriendInvites",

            args =
                emptyMap(),
        )
    }

    suspend fun acceptInvite(
        inviteId: String,
    ): MobileFriendActionResult {

        return convexClient.mutation(
            name =
                "mobile/native/friend/index:acceptFriendInvite",

            args =
                mapOf(
                    "inviteId" to inviteId,
                ),
        )
    }

    suspend fun rejectInvite(
        inviteId: String,
    ): MobileFriendActionResult {

        return convexClient.mutation(
            name =
                "mobile/native/friend/index:rejectFriendInvite",

            args =
                mapOf(
                    "inviteId" to inviteId,
                ),
        )
    }

    suspend fun openDirectConversation(
        userId: String,
    ): MobileOpenFriendConversationResult {

        return convexClient.mutation(
            name =
                "mobile/native/friend/index:openDirectConversationWithUser",

            args =
                mapOf(
                    "targetUserId" to userId,
                ),
        )
    }

    fun lookupUsers(
        query: String,
        limit: Double = 10.0,
    ): Flow<Result<List<MobileFriendLookupItem>>> {

        return convexClient.subscribe(
            name =
                "mobile/native/friend/index:lookupUsers",

            args =
                mapOf(
                    "query" to query,
                    "limit" to limit,
                ),
        )
    }

    suspend fun sendInvite(
        targetUserId: String,
    ): MobileFriendActionResult {

        return convexClient.mutation(
            name =
                "mobile/native/friend/index:sendFriendInvite",

            args =
                mapOf(
                    "targetUserId" to targetUserId,
                ),
        )
    }

}