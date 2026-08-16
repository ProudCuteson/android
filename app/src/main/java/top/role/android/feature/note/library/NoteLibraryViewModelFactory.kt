package top.role.android.feature.note.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import top.role.android.core.data.note.NoteLocalRepository

class NoteLibraryViewModelFactory(
    private val ownerUserId: String,
    private val noteLocalRepository:
    NoteLocalRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
    ): T {
        if (
            modelClass.isAssignableFrom(
                NoteLibraryViewModel::class.java,
            )
        ) {
            return NoteLibraryViewModel(
                ownerUserId =
                    ownerUserId,

                noteLocalRepository =
                    noteLocalRepository,
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}