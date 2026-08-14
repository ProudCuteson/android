package top.role.android.core.convex

import android.content.Context
import dev.convex.android.ConvexClientWithAuth
import kotlinx.coroutines.flow.Flow
import top.role.android.core.storage.SessionStorage

class ConvexMobileClient(
    deploymentUrl: String,
    sessionStorage: SessionStorage,
) {

    private val authProvider =
        BridgeJwtAuthProvider(
            sessionStorage = sessionStorage,
        )

    val rawClient =
        ConvexClientWithAuth(
            deploymentUrl = deploymentUrl,
            authProvider = authProvider,
        )

    suspend fun loginFromStoredSession():
            Result<BridgeJwtAuthResult> {

        return rawClient.loginFromCache()
    }

    suspend fun logout(
        context: Context,
    ): Result<Void?> {
        return rawClient.logout(context)
    }

    inline fun <reified T> subscribe(
        name: String,
        args: Map<String, Any?> = emptyMap(),
    ): Flow<Result<T>> {
        return rawClient.subscribe(
            name = name,
            args = args,
        )
    }

    suspend inline fun <reified T> mutation(
        name: String,
        args: Map<String, Any?> = emptyMap(),
    ): T {
        return rawClient.mutation<T>(
            name = name,
            args = args,
        )
    }

    suspend inline fun <reified T> action(
        name: String,
        args: Map<String, Any?> = emptyMap(),
    ): T {
        return rawClient.action<T>(
            name = name,
            args = args,
        )
    }
}