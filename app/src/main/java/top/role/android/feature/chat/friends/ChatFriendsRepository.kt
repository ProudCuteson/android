package top.role.android.feature.chat.friends

import kotlinx.coroutines.flow.Flow

import top.role.android.core.convex.ConvexMobileClient
import top.role.android.core.model.chat.MobileDirectConversationResult
import top.role.android.core.model.chat.MobileFriendInviteItem
import top.role.android.core.model.chat.MobileFriendItem
import top.role.android.core.model.chat.MobileFriendLookupItem
import top.role.android.core.model.chat.MobileFriendActionResult

class ChatFriendsRepository(
    private val convexClient:
    ConvexMobileClient,
) {

    fun observeFriends():
            Flow<Result<List<MobileFriendItem>>> {

        return convexClient.subscribe(
            name =
                "mobile/ios/friend:listMobileFriends",

            args =
                emptyMap(),
        )
    }

    fun observeInvites():
            Flow<Result<List<MobileFriendInviteItem>>> {

        return convexClient.subscribe(
            name =
                "mobile/ios/friend:listMobileFriendInvites",

            args =
                emptyMap(),
        )
    }

    suspend fun acceptInvite(
        inviteId: String,
    ): MobileFriendActionResult {

        return convexClient.mutation(
            name =
                "mobile/ios/friend:acceptMobileFriendInvite",

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
                "mobile/ios/friend:rejectMobileFriendInvite",

            args =
                mapOf(
                    "inviteId" to inviteId,
                ),
        )
    }

    suspend fun openDirectConversation(
        userId: String,
    ): MobileDirectConversationResult {

        return convexClient.mutation(
            name =
                "mobile/ios/chat:getOrCreateMobileDirectConversation",

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
                "mobile/ios/friend:lookupMobileUsers",

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
                "mobile/ios/friend:sendMobileFriendInvite",

            args =
                mapOf(
                    "targetUserId" to targetUserId,
                ),
        )
    }

}