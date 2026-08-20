package top.role.android.core.model.engage

import kotlinx.serialization.Serializable


@Serializable
data class MobileEngageHome(
    val surveyItems:
    List<MobileSurveyCard> =
        emptyList(),

    val bountyItems:
    List<MobileBountyCard> =
        emptyList(),

    val marketItems:
    List<MobileMarketCard> =
        emptyList(),
)