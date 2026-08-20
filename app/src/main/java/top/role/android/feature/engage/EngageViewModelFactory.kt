package top.role.android.feature.engage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


class EngageViewModelFactory(
    private val repository:
    EngageRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass:
        Class<T>,
    ): T {
        return EngageViewModel(
            repository =
                repository,
        ) as T
    }
}