package top.role.android.core.convex

import android.content.Context
import dev.convex.android.AuthProvider
import kotlinx.coroutines.flow.first
import top.role.android.core.storage.SessionStorage

data class BridgeJwtAuthResult(
    val idToken: String,
)

class BridgeJwtAuthProvider(
    private val sessionStorage: SessionStorage,
) : AuthProvider<BridgeJwtAuthResult> {

    override suspend fun login(
        context: Context,
        onIdToken: (String?) -> Unit,
    ): Result<BridgeJwtAuthResult> {
        return loadAuthResultFromStore()
    }

    override suspend fun loginFromCache(
        onIdToken: (String?) -> Unit,
    ): Result<BridgeJwtAuthResult> {
        return loadAuthResultFromStore()
    }

    // 不再使用 clearSession()，避免清除 USER_ID。
    override suspend fun logout(
        context: Context,
    ): Result<Void?> {
        return runCatching {
            sessionStorage.clearRemoteSession()
            null
        }
    }

    override fun extractIdToken(
        authResult: BridgeJwtAuthResult,
    ): String {
        return authResult.idToken
    }

    private suspend fun loadAuthResultFromStore():
            Result<BridgeJwtAuthResult> {

        return runCatching {
            val session = sessionStorage.session.first()
                ?: throw IllegalStateException("No cached session")

            if (session.isExpired) {
                throw IllegalStateException("Cached session expired")
            }

            BridgeJwtAuthResult(
                idToken = session.accessToken,
            )
        }
    }
}