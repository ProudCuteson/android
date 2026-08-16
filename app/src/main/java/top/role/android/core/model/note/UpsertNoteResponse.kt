package top.role.android.core.model.note

import kotlinx.serialization.Serializable

@Serializable
data class UpsertNoteResponse(
    val outcome: String,
    val note: RemoteNoteSnapshot,
)