package top.role.android.core.model.note

import kotlinx.serialization.Serializable

@Serializable
data class NotePullCheckpoint(
    val kind: String = "updatedAt",
    val value: Double,
)