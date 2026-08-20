package top.role.android.core.model.survey

import kotlinx.serialization.Serializable

import top.role.android.core.model.engage.MobileSurveyCard

@Serializable
data class MobileSurveyPage(
    val page:
    List<MobileSurveyCard> =
        emptyList(),

    val isDone:
    Boolean = true,

    val continueCursor:
    String = "",
)