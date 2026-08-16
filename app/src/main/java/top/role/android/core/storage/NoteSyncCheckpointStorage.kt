package top.role.android.core.storage

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import top.role.android.core.model.note.NotePullCheckpoint

private val Context.noteSyncDataStore by
preferencesDataStore(
    name = "toprole_note_sync",
)

class NoteSyncCheckpointStorage(
    private val context: Context,
) {

    suspend fun getPullCheckpoint(
        ownerUserId: String,
    ): NotePullCheckpoint? {
        val key =
            pullUpdatedAtKey(
                ownerUserId,
            )

        val preferences =
            context.noteSyncDataStore
                .data
                .first()

        val value =
            preferences[key]
                ?: return null

        return NotePullCheckpoint(
            kind =
                "updatedAt",
            value =
                value,
        )
    }

    suspend fun savePullCheckpoint(
        ownerUserId: String,
        checkpoint: NotePullCheckpoint,
    ) {
        when (
            checkpoint.kind
        ) {
            "updatedAt" -> {
                val key =
                    pullUpdatedAtKey(
                        ownerUserId,
                    )

                context.noteSyncDataStore
                    .edit { preferences ->
                        preferences[key] =
                            checkpoint.value
                    }
            }

            else -> {
                throw IllegalArgumentException(
                    "Unsupported note pull checkpoint kind: ${checkpoint.kind}"
                )
            }
        }
    }

    suspend fun clearPullCheckpoint(
        ownerUserId: String,
    ) {
        val key =
            pullUpdatedAtKey(
                ownerUserId,
            )

        context.noteSyncDataStore
            .edit { preferences ->
                preferences.remove(
                    key,
                )
            }
    }

    private fun pullUpdatedAtKey(
        ownerUserId: String,
    ) =
        doublePreferencesKey(
            "note_pull_updated_at_$ownerUserId"
        )
}