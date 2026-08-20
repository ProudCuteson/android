package top.role.android.feature.survey.detail

import kotlinx.coroutines.flow.Flow

import top.role.android.core.convex.ConvexMobileClient
import top.role.android.core.model.survey.MobileSurveyDetailResponse
import top.role.android.core.model.survey.MobileSurveySubmitResult


class SurveyDetailRepository(
    private val convexClient:
    ConvexMobileClient,
) {

    fun observeDetail(
        surveyId: String,
    ): Flow<
            Result<MobileSurveyDetailResponse>
            > {
        return convexClient.subscribe(
            name =
                "mobile/native/survey/index:getMobileSurveyDetail",

            args =
                mapOf(
                    "surveyId" to
                            surveyId,
                ),
        )
    }


    suspend fun submit(
        surveyId: String,

        answers:
        Map<String, Any?>,

        durationMs:
        Double? = null,

        sessionId:
        String? = null,
    ): MobileSurveySubmitResult {

        val payload =
            answers.map {
                    (
                        fieldId,
                        value
                    ) ->

                mapOf(
                    "fieldId" to
                            fieldId,

                    "value" to
                            value,
                )
            }

        val args =
            mutableMapOf<String, Any?>(
                "surveyId" to
                        surveyId,

                "payload" to
                        payload,
            )

        durationMs?.let {
            args["durationMs"] =
                it
        }

        sessionId
            ?.takeIf {
                it.isNotBlank()
            }
            ?.let {
                args["sessionId"] =
                    it
            }

        return convexClient.mutation(
            name =
                "mobile/native/survey/index:submitMobileSurvey",

            args =
                args,
        )
    }
}