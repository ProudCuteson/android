package top.role.android.feature.day

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import top.role.android.core.data.note.NoteLocalRepository

class DayHomeViewModelFactory(
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
                DayHomeViewModel::class.java,
            )
        ) {
            return DayHomeViewModel(
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