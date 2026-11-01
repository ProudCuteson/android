package top.role.android.feature.note.adoption

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import top.role.android.core.auth.AuthState
import top.role.android.core.data.note.NoteAdoptionLocalRepository

class NoteAdoptionViewModelFactory(
    private val guestLocalOwnerId: String,
    private val repository: NoteAdoptionLocalRepository,
    private val initialAuthState: AuthState,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
    ): T {
        if (
            modelClass.isAssignableFrom(
                NoteAdoptionViewModel::class.java,
            )
        ) {
            return NoteAdoptionViewModel(
                guestLocalOwnerId = guestLocalOwnerId,
                repository = repository,
                initialAuthState = initialAuthState,
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}