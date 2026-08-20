package top.role.android.feature.survey.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SurveyDetailViewModelFactory(
    private val surveyId:
    String,

    private val repository:
    SurveyDetailRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass:
        Class<T>,
    ): T {

        return SurveyDetailViewModel(
            surveyId =
                surveyId,

            repository =
                repository,
        ) as T
    }
}