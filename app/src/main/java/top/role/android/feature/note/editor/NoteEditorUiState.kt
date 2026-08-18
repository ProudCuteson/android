package top.role.android.feature.note.editor

import top.role.android.core.database.note.LocalNoteEntity
import top.role.android.core.database.note.effectiveRemoteStatus
import top.role.android.core.model.note.NoteEditorPlacement
import top.role.android.core.model.note.NoteRemoteStatus
import top.role.android.core.model.note.NoteSyncState
import top.role.android.core.data.note.ai.NoteAiMode

data class NoteEditorUiState(
    val loading: Boolean = true,
    val localKey: String? = null,
    val title: String = "",
    val text: String = "",
    val dayKey: String? = null,
    val onlyDay: Boolean = false,
    val pinned: Boolean = false,

    val aiRunning: Boolean =
        false,

    val aiSuggestion: String? =
        null,

    val aiSuggestionMode: NoteAiMode? =
        null,

    val aiErrorMessage: String? =
        null,

    val images: List<NoteEditorImageUi> =
        emptyList(),

    val remoteStatus: NoteRemoteStatus =
        NoteRemoteStatus.Active,

    val mode: NoteEditorMode =
        NoteEditorMode.Reading,

    val syncState: NoteSyncState =
        NoteSyncState.Pending,

    val conflict: NoteConflictUiModel? =
        null,

    val errorMessage: String? = null,
) {

    val placement: NoteEditorPlacement
        get() =
            when {
                onlyDay ->
                    NoteEditorPlacement.DayOnly

                dayKey != null ->
                    NoteEditorPlacement.Dated

                else ->
                    NoteEditorPlacement.Normal
            }

    val isArchived: Boolean
        get() =
            remoteStatus ==
                    NoteRemoteStatus.Archived

    val isDeleted: Boolean
        get() =
            remoteStatus ==
                    NoteRemoteStatus.Deleted

    val canEdit: Boolean
        get() =
            !loading &&
                    localKey != null &&
                    remoteStatus ==
                    NoteRemoteStatus.Active

    val isEditing: Boolean
        get() =
            canEdit &&
                    mode ==
                    NoteEditorMode.Editing

    val hasConflict: Boolean
        get() =
            syncState ==
                    NoteSyncState.Conflict &&
                    conflict != null

    val hasImages: Boolean
        get() =
            images.isNotEmpty()

    val isContentEmpty: Boolean
        get() =
            title.isBlank() &&
                    text.isBlank() &&
                    images.isEmpty()

    companion object {

        fun fromNote(
            note: LocalNoteEntity,
        ): NoteEditorUiState {
            return NoteEditorUiState(
                loading =
                    false,

                localKey =
                    note.localKey,

                title =
                    note.title,

                text =
                    note.text,

                dayKey =
                    note.dayKey,

                onlyDay =
                    note.onlyDay,

                pinned =
                    note.pinned,

                images =
                    emptyList(),

                remoteStatus =
                    note.effectiveRemoteStatus,

                syncState =
                    note.syncState,

                conflict =
                    null,

                errorMessage =
                    null,
            )
        }
    }
}