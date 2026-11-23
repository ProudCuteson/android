package top.role.android.feature.entity.service

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement


@Serializable
data class EntityServiceRequester(
    val displayName: String? = null,
    val avatarUrl: String? = null,
)

/**
 * Space Service 类型
 */
object EntityServiceTypes {
    const val ADVICE = "advice"
    const val FEEDBACK = "feedback"
    const val SUPPORT = "support"
    const val COLLABORATION = "collaboration"
    const val OTHER = "other"

    fun label(value: String): String = when (value) {
        ADVICE -> "咨询建议"
        FEEDBACK -> "反馈"
        SUPPORT -> "支持"
        COLLABORATION -> "协作"
        OTHER -> "其他"
        else -> value
    }
}

/**
 * Space Service 状态
 */
object EntityServiceStatuses {
    const val OPEN = "open"
    const val IN_REVIEW = "in_review"
    const val ACCEPTED = "accepted"
    const val DECLINED = "declined"
    const val CLOSED = "closed"

    fun label(value: String): String = when (value) {
        OPEN -> "待处理"
        IN_REVIEW -> "处理中"
        ACCEPTED -> "已受理"
        DECLINED -> "已拒绝"
        CLOSED -> "已关闭"
        else -> value
    }

    fun canWithdraw(value: String): Boolean =
        value == OPEN || value == IN_REVIEW

    fun canReview(value: String): Boolean =
        value == OPEN || value == IN_REVIEW

    fun canAccept(value: String): Boolean =
        value == OPEN || value == IN_REVIEW

    fun canDecline(value: String): Boolean =
        value != DECLINED && value != CLOSED

    fun canClose(value: String): Boolean =
        value != DECLINED && value != CLOSED
}

/**
 * 关联内容
 *
 * 目前 Android 只负责展示和阅读。
 */
@Serializable
data class EntityServiceReference(
    val targetType: String,
    val targetId: String? = null,
    val url: String? = null,
    val label: String? = null,
    val purpose: String? = null,
    val createdAt: Long? = null,
)

/**
 * 服务请求
 *
 * 同时用于会员列表、管理列表和详情。
 */
@Serializable
data class EntityServiceItem(
    @SerialName("_id")
    val id: String,

    @SerialName("_creationTime")
    val creationTime: Double? = null,

    val spaceId: String,
    val requesterId: String,

    val requestType: String,
    val title: String,
    val contextSummary: String,
    val coreQuestion: String,
    val expectedHelp: String,

    val status: String,

    val references: List<EntityServiceReference>? = null,

    val incentiveC: Double? = null,
    val incentiveIntentId: String? = null,

    val assigneeId: String? = null,

    val acceptedAt: Double? = null,
    val acceptedBy: String? = null,
    val targetReplyAt: Double? = null,

    val responseNote: String? = null,
    val resultLink: String? = null,
    val resultLinkLabel: String? = null,
    val resultRef: EntityServiceReference? = null,

    val resolvedAt: Double? = null,
    val resolvedBy: String? = null,

    val createdAt: Double,
    val updatedAt: Double,

    val isRequester: Boolean? = null,

    // 详情和管理列表包含扩展信息。
    // 具体 Profile / Billing 类型后续再对齐。
    val requester: EntityServiceRequester? = null,
    val incentiveIntent: JsonElement? = null,
)

/**
 * 列表查询返回
 */
@Serializable
data class EntityServiceListResponse(
    val ok: Boolean,
    val code: String? = null,
    val services: List<EntityServiceItem> = emptyList(),
)

/**
 * 详情查询返回
 */
@Serializable
data class EntityServiceDetailResponse(
    val ok: Boolean,
    val code: String? = null,
    val service: EntityServiceItem? = null,
)

/**
 * 通用操作结果
 */
@Serializable
data class EntityServiceActionResponse(
    val ok: Boolean,
    val code: String? = null,
    val serviceId: String? = null,
)
