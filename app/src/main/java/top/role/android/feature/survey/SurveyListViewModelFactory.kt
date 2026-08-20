package top.role.android.feature.survey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

import top.role.android.core.model.survey.MobileSurveyListMode

class SurveyListViewModelFactory(
    private val mode:
    MobileSurveyListMode,

    private val repository:
    SurveyListRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass:
        Class<T>,
    ): T {

        return SurveyListViewModel(
            mode =
                mode,

            repository =
                repository,
        ) as T
    }
}