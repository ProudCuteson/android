package top.role.android.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import top.role.android.core.preferences.AppAppearance
import top.role.android.core.preferences.AppPreferencesRepository
import top.role.android.core.preferences.WeekStart


class AppPreferencesViewModel(
    private val repository:
    AppPreferencesRepository,
) : ViewModel() {

    val appearance:
            StateFlow<AppAppearance> =
        repository
            .appearance
            .stateIn(
                scope =
                    viewModelScope,

                started =
                    SharingStarted.WhileSubscribed(
                        stopTimeoutMillis =
                            5_000,
                    ),

                initialValue =
                    AppAppearance.System,
            )


    val weekStart:
            StateFlow<WeekStart> =
        repository
            .weekStart
            .stateIn(
                scope =
                    viewModelScope,

                started =
                    SharingStarted.WhileSubscribed(
                        stopTimeoutMillis =
                            5_000,
                    ),

                initialValue =
                    WeekStart.Monday,
            )


    fun setAppearance(
        appearance:
        AppAppearance,
    ) {
        viewModelScope.launch {

            repository
                .setAppearance(
                    appearance =
                        appearance,
                )
        }
    }


    fun setWeekStart(
        weekStart:
        WeekStart,
    ) {
        viewModelScope.launch {

            repository
                .setWeekStart(
                    weekStart =
                        weekStart,
                )
        }
    }


    companion object {

        fun factory(
            repository:
            AppPreferencesRepository,
        ): ViewModelProvider.Factory {

            return object :
                ViewModelProvider.Factory {

                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass:
                    Class<T>,
                ): T {

                    if (
                        modelClass.isAssignableFrom(
                            AppPreferencesViewModel::class.java
                        )
                    ) {
                        return AppPreferencesViewModel(
                            repository =
                                repository,
                        ) as T
                    }


                    throw IllegalArgumentException(
                        "Unknown ViewModel class: ${modelClass.name}"
                    )
                }
            }
        }
    }
}