package top.role.android.core.data.note.ai

import kotlinx.serialization.Serializable

@Serializable
data class NoteAiGenerateRequest(
    val input: String,
    val mode: String,
)

@Serializable
data class NoteAiGenerateResponse(
    val ok: Boolean,
    val output: String? = null,
    val mode: String? = null,
    val error: String? = null,
    val maxLength: Int? = null,
)