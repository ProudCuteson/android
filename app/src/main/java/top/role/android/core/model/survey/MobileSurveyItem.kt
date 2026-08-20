package top.role.android.core.model.survey

import kotlinx.serialization.Serializable


@Serializable
data class MobileSurveyItem(
    val id: String,

    val kind: String,
    val type: String,

    /*
     * content
     */
    val content: String? = null,
    val headingLevel: Double? = null,

    /*
     * question / common
     */
    val fieldId: String? = null,
    val title: String? = null,
    val description: String? = null,
    val required: Boolean = false,

    /*
     * choice
     */
    val choiceVariant: String? = null,

    val options:
    List<MobileSurveyOption> =
        emptyList(),

    /*
     * text
     */
    val placeholder: String? = null,

    /*
     * rating
     */
    val ratingScale: Double? = null,
    val rating:
    MobileSurveyRating? = null,

    /*
     * boolean
     */
    val booleanVariant: String? = null,
    val booleanLabels:
    MobileSurveyBooleanLabels? = null,

    /*
     * number
     */
    val min: Double? = null,
    val max: Double? = null,
    val step: Double? = null,

    /*
     * unsupported
     */
    val reason: String? = null,
)


@Serializable
data class MobileSurveyOption(
    val id: String,

    val label: String,
    val value: String,

    val analysisKey: String? = null,
    val score: Double? = null,
)


@Serializable
data class MobileSurveyRating(
    val preset: Double? = null,

    val min: Double? = null,
    val max: Double? = null,
    val step: Double? = null,

    val minLabel: String? = null,
    val maxLabel: String? = null,

    val reverseScore: Boolean? = null,
)


@Serializable
data class MobileSurveyBooleanLabels(
    val trueLabel: String? = null,
    val falseLabel: String? = null,
)