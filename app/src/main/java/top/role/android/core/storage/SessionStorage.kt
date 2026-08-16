package top.role.android.core.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
        val USER_ID = stringPreferencesKey("user_id")
        val PHONE = stringPreferencesKey("phone")
    }

    val session: Flow<AppSession?> =
        context.sessionDataStore.data.map { preferences ->
            val accessToken = preferences[ACCESS_TOKEN]
            val expiresAt = preferences[EXPIRES_AT]
            val userId = preferences[USER_ID]
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

    val localOwnerUserId: Flow<String?> =
        context.sessionDataStore.data.map { preferences ->
            preferences[USER_ID]
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
        }

    suspend fun clearRemoteSession() {
        context.sessionDataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN)
            preferences.remove(EXPIRES_AT)
        }
    }

    suspend fun saveSession(
        session: AppSession,
    ) {
        context.sessionDataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = session.accessToken
            preferences[EXPIRES_AT] = session.expiresAt
            preferences[USER_ID] = session.userId
            preferences[PHONE] = session.phone
        }
    }

    suspend fun clearSession() {
        context.sessionDataStore.edit { preferences ->
            preferences.clear()
        }
    }
}