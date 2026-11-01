package top.role.android.core.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import top.role.android.core.auth.AppSession

private val Context.sessionDataStore by preferencesDataStore(
    name = "toprole_session"
)

class SessionStorage(
    private val context: Context,
) {

    private companion object {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")

        val EXPIRES_AT = longPreferencesKey("expires_at")

        val PHONE = stringPreferencesKey("phone")

        // 本地身份
        val LOCAL_OWNER_USER_ID =
            stringPreferencesKey("local_owner_user_id")

        // 当前远端身份
        val REMOTE_USER_ID = stringPreferencesKey("remote_user_id")

        // 上一次远端身份
        val LAST_IDENTITY_USER_ID = stringPreferencesKey("last_identity_user_id")

        // 旧版本字段，仅用于迁移
        val LEGACY_USER_ID = stringPreferencesKey("user_id")
    }

    private val dataStore = context.applicationContext.sessionDataStore

    /**
     * 当前远端 Session
     */
    val session: Flow<AppSession?> =
        dataStore.data.map { preferences ->

            val accessToken = preferences[ACCESS_TOKEN]
            val expiresAt = preferences[EXPIRES_AT]
            val userId = preferences[REMOTE_USER_ID]
            val phone = preferences[PHONE]

            if (
                accessToken.isNullOrBlank() ||
                expiresAt == null ||
                userId.isNullOrBlank() ||
                phone.isNullOrBlank()
            ) {
                null
            } else {
                AppSession(
                    accessToken = accessToken,
                    expiresAt = expiresAt,
                    userId = userId,
                    phone = phone,
                )
            }
        }

    /**
     * 当前本地数据归属身份
     */
    val localOwnerUserId: Flow<String?> =
        dataStore.data.map { preferences ->
            preferences[LOCAL_OWNER_USER_ID]
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
        }

    /**
     * 上一次远端账户身份
     */
    val lastIdentityUserId: Flow<String?> =
        dataStore.data.map { preferences ->
            preferences[LAST_IDENTITY_USER_ID]
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
        }

    /**
     * 初始化本地身份。
     *
     * 兼容旧版本 USER_ID：
     * 1. 原 USER_ID 迁移为 LOCAL_OWNER_USER_ID。
     * 2. 如果原来有 Token，同时恢复 REMOTE_USER_ID。
     * 3. LAST_IDENTITY_USER_ID 保留原账户身份。
     * 4. 全新安装生成独立访客身份。
     */
    suspend fun ensureLocalIdentity(): String {

        var resolvedUserId = ""

        dataStore.edit { preferences ->

            val legacyUserId =
                preferences[LEGACY_USER_ID]
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() }

            // 1. 初始化本地身份
            val existingLocalId =
                preferences[LOCAL_OWNER_USER_ID]
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() }

            val localId = existingLocalId
                ?: legacyUserId
                ?: "guest_${UUID.randomUUID()}"

            preferences[LOCAL_OWNER_USER_ID] = localId

            resolvedUserId = localId

            // 2. 迁移远端身份
            if (
                preferences[REMOTE_USER_ID] == null &&
                legacyUserId != null &&
                !preferences[ACCESS_TOKEN].isNullOrBlank()
            ) {
                preferences[REMOTE_USER_ID] = legacyUserId
            }

            // 3. 迁移上一次账户身份
            if (preferences[LAST_IDENTITY_USER_ID] == null) {
                val lastUserId =
                    preferences[REMOTE_USER_ID]
                        ?.trim()
                        ?.takeIf { it.isNotEmpty() }
                        ?: legacyUserId

                if (lastUserId != null) {
                    preferences[LAST_IDENTITY_USER_ID] = lastUserId
                }
            }

            // 4. 删除旧字段
            preferences.remove(LEGACY_USER_ID)
        }

        return resolvedUserId
    }

    /**
     * 清除远端 Session。
     *
     * 保留：
     * - LOCAL_OWNER_USER_ID
     * - LAST_IDENTITY_USER_ID
     */
    suspend fun clearRemoteSession() {
        dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN)
            preferences.remove(EXPIRES_AT)
            preferences.remove(REMOTE_USER_ID)
            preferences.remove(PHONE)
        }
    }

    /**
     * 保存远端登录 Session。
     *
     * 不改变本地数据归属。
     */
    suspend fun saveSession(
        session: AppSession,
    ) {
        dataStore.edit { preferences ->

            preferences[ACCESS_TOKEN] = session.accessToken

            preferences[EXPIRES_AT] = session.expiresAt

            preferences[REMOTE_USER_ID] = session.userId

            preferences[PHONE] = session.phone

            preferences[LAST_IDENTITY_USER_ID] = session.userId
        }
    }

    /**
     * 完整清除所有身份和认证信息。
     *
     * 注意：这是破坏性操作，不用于普通退出登录。
     */
    suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
