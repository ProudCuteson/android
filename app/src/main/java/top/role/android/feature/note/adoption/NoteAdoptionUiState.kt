package top.role.android.feature.note.adoption

import top.role.android.core.auth.AuthState
import top.role.android.core.database.note.LocalNoteEntity

data class NoteAdoptionUiState(
    val authState: AuthState = AuthState.Loading,
    val notes: List<LocalNoteEntity> = emptyList(),
    val loading: Boolean = true,
    val isClaiming: Boolean = false,
    val errorMessage: String? = null,
    val claimedCount: Int? = null,
) {
    val unclaimedCount: Int
        get() = notes.size

    val canClaim: Boolean
        get() = notes.isNotEmpty() && !isClaiming
}