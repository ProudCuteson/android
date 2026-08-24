package top.role.android.core.preferences

import android.content.Context

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


private val Context.appPreferencesDataStore
        by preferencesDataStore(
            name = "app_preferences"
        )


class AppPreferencesRepository(
    private val context: Context,
) {

    val appearance:
            Flow<AppAppearance> =
        context
            .appPreferencesDataStore
            .data
            .map { preferences ->

                AppAppearance.fromStorage(
                    preferences[
                        APPEARANCE_KEY
                    ]
                )
            }


    val weekStart:
            Flow<WeekStart> =
        context
            .appPreferencesDataStore
            .data
            .map { preferences ->

                WeekStart.fromStorage(
                    preferences[
                        WEEK_START_KEY
                    ]
                )
            }


    /*
     * ------------------------------------------------
     * Auth Agreement
     * ------------------------------------------------
     */

    val acceptedAuthAgreementVersion:
            Flow<Int> =
        context
            .appPreferencesDataStore
            .data
            .map { preferences ->

                preferences[
                    AUTH_AGREEMENT_VERSION_KEY
                ] ?: 0
            }


    suspend fun setAppearance(
        appearance: AppAppearance,
    ) {

        context
            .appPreferencesDataStore
            .edit { preferences ->

                preferences[
                    APPEARANCE_KEY
                ] =
                    appearance.storageValue
            }
    }


    suspend fun setWeekStart(
        weekStart: WeekStart,
    ) {

        context
            .appPreferencesDataStore
            .edit { preferences ->

                preferences[
                    WEEK_START_KEY
                ] =
                    weekStart.storageValue
            }
    }


    /*
     * 用户明确同意当前版本的
     * 用户协议 / 隐私政策后调用。
     */
    suspend fun acceptAuthAgreement() {

        context
            .appPreferencesDataStore
            .edit { preferences ->

                preferences[
                    AUTH_AGREEMENT_VERSION_KEY
                ] =
                    CURRENT_AUTH_AGREEMENT_VERSION
            }
    }


    private companion object {

        val APPEARANCE_KEY =
            stringPreferencesKey(
                "appearance"
            )


        val WEEK_START_KEY =
            stringPreferencesKey(
                "week_start"
            )


        val AUTH_AGREEMENT_VERSION_KEY =
            intPreferencesKey(
                "auth_agreement_version"
            )
    }
}