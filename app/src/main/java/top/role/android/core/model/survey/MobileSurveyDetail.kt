package top.role.android.core.model.survey

import kotlinx.serialization.Serializable


@Serializable
data class MobileSurveyDetailResponse(
    val ok: Boolean,

    val code: String? = null,

    val survey:
    MobileSurveyDetail? = null,

    val items:
    List<MobileSurveyItem> =
        emptyList(),

    val groups:
    List<MobileSurveyGroup> =
        emptyList(),

    val submitSchema:
    MobileSurveySubmitSchema? = null,
)


@Serializable
data class MobileSurveyDetail(
    val id: String,

    val title: String,
    val description: String = "",

    val responseKind: String,

    val coverImageUrl: String = "",

    val primaryColor: String = "",
    val backgroundColor: String = "",

    val views: Double = 0.0,
    val responses: Double = 0.0,

    val publishedAt: Double = 0.0,
    val updatedAt: Double = 0.0,
)


@Serializable
data class MobileSurveyGroup(
    val id: String,

    val index: Double = 0.0,

    val title: String = "",

    val questionStart: Double = 0.0,
    val questionEnd: Double = 0.0,
    val questionCount: Double = 0.0,

    val items:
    List<MobileSurveyItem> =
        emptyList(),
)


@Serializable
data class MobileSurveySubmitSchema(
    val questionCount:
    Double = 0.0,

    val requiredFieldIds:
    List<String> =
        emptyList(),

    val fieldIds:
    List<String> =
        emptyList(),

    val supportedQuestionTypes:
    List<String> =
        emptyList(),
)