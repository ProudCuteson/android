package top.role.android.app.navigation

import top.role.android.core.model.survey.MobileSurveyListMode


object SurveyListRoute {

    const val modeArg =
        "mode"

    const val route =
        "survey/list/{$modeArg}"

    fun create(
        mode:
        MobileSurveyListMode,
    ): String {
        return "survey/list/${mode.value}"
    }
}


object SurveyDetailRoute {

    const val surveyIdArg =
        "surveyId"

    const val route =
        "survey/detail/{$surveyIdArg}"

    fun create(
        surveyId:
        String,
    ): String {
        return "survey/detail/$surveyId"
    }
}