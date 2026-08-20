package top.role.android.feature.survey

import kotlinx.coroutines.flow.Flow

import top.role.android.core.convex.ConvexMobileClient
import top.role.android.core.model.survey.MobileSurveyListMode
import top.role.android.core.model.survey.MobileSurveyPage

class SurveyListRepository(
    private val convexClient:
    ConvexMobileClient,
) {

    fun observePage(
        mode:
        MobileSurveyListMode,

        cursor:
        String? = null,

        numItems:
        Double = 12.0,
    ): Flow<Result<MobileSurveyPage>> {

        return convexClient.subscribe(
            name =
                "mobile/native/survey/index:listMobileSurveys",

            args =
                mapOf(
                    "mode" to
                            mode.value,

                    "paginationOpts" to
                            mapOf(
                                "numItems" to
                                        numItems,

                                "cursor" to
                                        cursor,
                            ),
                ),
        )
    }
}