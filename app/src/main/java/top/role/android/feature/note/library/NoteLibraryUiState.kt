package top.role.android.feature.note.library

import top.role.android.core.database.note.LocalNoteEntity

data class NoteLibraryUiState(
    val selectedSection: NoteLibrarySection =
        NoteLibrarySection.Recent,

    val notes: List<LocalNoteEntity> =
        emptyList(),

    val searchQuery: String =
        "",

    val loading: Boolean =
        false,

    val errorMessage: String? =
        null,
)