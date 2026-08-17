package top.role.android.feature.note.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import top.role.android.core.data.image.NoteImageService
import top.role.android.core.data.note.NoteLocalRepository

class NoteEditorViewModelFactory(
    private val ownerUserId: String,

    private val noteLocalRepository:
    NoteLocalRepository,

    private val noteImageService:
    NoteImageService,

    private val initialLocalKey: String? = null,
    private val initialDayKey: String? = null,
    private val openAsDayOnly: Boolean = false,
    private val createAsNormal: Boolean = false,
    private val createAsDated: Boolean = false,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
    ): T {
        if (
            modelClass.isAssignableFrom(
                NoteEditorViewModel::class.java,
            )
        ) {
            return NoteEditorViewModel(
                ownerUserId =
                    ownerUserId,

                noteLocalRepository =
                    noteLocalRepository,

                noteImageService =
                    noteImageService,

                initialLocalKey =
                    initialLocalKey,

                initialDayKey =
                    initialDayKey,

                openAsDayOnly =
                    openAsDayOnly,

                createAsNormal =
                    createAsNormal,

                createAsDated =
                    createAsDated,
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}