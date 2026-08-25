package top.role.android.feature.slide

import kotlinx.coroutines.flow.first
import top.role.android.core.convex.ConvexMobileClient
import top.role.android.core.model.slide.MobileSlideDetailResponse
import top.role.android.core.model.slide.MobileSlideQuestionActionResponse
import top.role.android.core.model.slide.MobileSlideQuestionsResponse
import top.role.android.core.model.slide.MobileSlideQuestionSubmitResponse

class SlideDetailRepository(
    private val convexClient: ConvexMobileClient,
) {

    suspend fun loadDetail(
        slideId: String,
    ): Result<MobileSlideDetailResponse?> {
        return try {
            println(
                "SlideDetailRepository loadDetail start: $slideId"
            )

            val result =
                convexClient
                    .subscribe<MobileSlideDetailResponse?>(
                        name =
                            "mobile/native/slide/index:getSlideDetail",

                        args =
                            mapOf(
                                "slideId" to
                                        slideId,
                            ),
                    )
                    .first()

            println(
                "SlideDetailRepository loadDetail result: $result"
            )

            result
        } catch (error: Throwable) {
            error.printStackTrace()

            println(
                "SlideDetailRepository loadDetail exception: " +
                        "${error::class.java.name}: ${error.message}"
            )

            Result.failure(error)
        }
    }

    suspend fun loadQuestions(
        slideId: String,
        cardIndex: Int,
        limit: Int = 20,
    ): Result<MobileSlideQuestionsResponse> {
        return try {
            convexClient
                .subscribe<MobileSlideQuestionsResponse>(
                    name =
                        "mobile/native/slide/index:listSlideQuestions",

                    args =
                        mapOf(
                            "slideId" to
                                    slideId,

                            "cardIndex" to
                                    cardIndex.toDouble(),

                            "scope" to
                                    "card",

                            "limit" to
                                    limit
                                        .coerceIn(
                                            1,
                                            50,
                                        )
                                        .toDouble(),
                        ),
                )
                .first()
        } catch (error: Throwable) {
            Result.failure(
                error
            )
        }
    }

    suspend fun createQuestion(
        slideId: String,
        cardIndex: Int,
        text: String,
    ): Result<MobileSlideQuestionSubmitResponse> {
        return try {
            val trimmed =
                text.trim()

            println(
                "SlideDetailRepository createQuestion " +
                        "slideId=$slideId cardIndex=$cardIndex"
            )

            val result =
                convexClient
                    .mutation<MobileSlideQuestionSubmitResponse>(
                        name =
                            "mobile/native/slide/index:createSlideQuestion",

                        args =
                            mapOf(
                                "slideId" to
                                        slideId,

                                "cardIndex" to
                                        cardIndex.toDouble(),

                                "text" to
                                        trimmed,
                            ),
                    )

            println(
                "SlideDetailRepository createQuestion result: $result"
            )

            Result.success(
                result
            )
        } catch (error: Throwable) {
            error.printStackTrace()

            println(
                "SlideDetailRepository createQuestion exception: " +
                        "${error::class.java.name}: ${error.message}"
            )

            Result.failure(
                error
            )
        }
    }

    suspend fun markQuestionAnswered(
        slideId: String,
        questionId: String,
    ): Result<MobileSlideQuestionActionResponse> {
        return try {
            println(
                "SlideDetailRepository markQuestionAnswered " +
                        "slideId=$slideId questionId=$questionId"
            )

            val result =
                convexClient
                    .mutation<MobileSlideQuestionActionResponse>(
                        name =
                            "mobile/native/slide/index:markSlideQuestionAnswered",

                        args =
                            mapOf(
                                "slideId" to
                                        slideId,

                                "questionId" to
                                        questionId,
                            ),
                    )

            println(
                "SlideDetailRepository markQuestionAnswered result: $result"
            )

            Result.success(
                result
            )
        } catch (error: Throwable) {
            error.printStackTrace()

            println(
                "SlideDetailRepository markQuestionAnswered exception: " +
                        "${error::class.java.name}: ${error.message}"
            )

            Result.failure(
                error
            )
        }
    }

    suspend fun hideQuestion(
        slideId: String,
        questionId: String,
    ): Result<MobileSlideQuestionActionResponse> {
        return try {
            println(
                "SlideDetailRepository hideQuestion " +
                        "slideId=$slideId questionId=$questionId"
            )

            val result =
                convexClient
                    .mutation<MobileSlideQuestionActionResponse>(
                        name =
                            "mobile/native/slide/index:hideSlideQuestion",

                        args =
                            mapOf(
                                "slideId" to
                                        slideId,

                                "questionId" to
                                        questionId,
                            ),
                    )

            println(
                "SlideDetailRepository hideQuestion result: $result"
            )

            Result.success(
                result
            )
        } catch (error: Throwable) {
            error.printStackTrace()

            println(
                "SlideDetailRepository hideQuestion exception: " +
                        "${error::class.java.name}: ${error.message}"
            )

            Result.failure(
                error
            )
        }
    }

}