package top.role.android.core.model.survey

import kotlinx.serialization.Serializable


@Serializable
data class MobileSurveyAnswer(
    val fieldId: String,
    val value: String,
)


@Serializable
data class MobileSurveySubmitResult(
    val ok: Boolean,

    val code: String? = null,

    val responseId: String? = null,

    val responseKind: String? = null,

    val message: String? = null,

    val missingFieldIds:
    List<String> =
        emptyList(),
)