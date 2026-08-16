package top.role.android.core.model.slide

import kotlinx.serialization.Serializable

@Serializable
data class MobileSlideQuestionItem(
    val id: String,
    val slideId: String,
    val text: String,
    val status: String,
    val cardIndex: Double? = null,
    val createdAt: Double,
    val answeredAt: Double? = null,
)

@Serializable
data class MobileSlideQuestionsResponse(
    val ok: Boolean,
    val code: String? = null,
    val message: String? = null,
    val data: List<MobileSlideQuestionItem>? = null,
)

@Serializable
data class MobileSlideQuestionSubmitResponse(
    val ok: Boolean,
    val code: String? = null,
    val message: String? = null,
    val id: String? = null,
)

@Serializable
data class MobileSlideQuestionActionResponse(
    val ok: Boolean,
    val code: String? = null,
    val message: String? = null,
    val id: String? = null,
    val status: String? = null,
    val answeredAt: Double? = null,
    val hiddenAt: Double? = null,
)