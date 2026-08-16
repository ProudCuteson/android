package top.role.android.core.model.note

import kotlinx.serialization.Serializable

@Serializable
data class LifecycleNoteResponse(
    val outcome: String,
    val note: RemoteNoteSnapshot,
)