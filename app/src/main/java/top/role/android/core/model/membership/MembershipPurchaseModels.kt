package top.role.android.core.model.membership

import kotlinx.serialization.Serializable
import top.role.android.core.model.payment.WechatAppPayParams

@Serializable
data class CreateMembershipPurchaseRequest(
    val spaceId: String,
    val planKey: String,
)

@Serializable
data class CreateMembershipPurchaseResponse(
    val ok: Boolean = false,
    val purchase: MembershipPurchaseCreated? = null,
    val payment: WechatAppPayParams? = null,
    val code: String? = null,
    val message: String? = null,
)

@Serializable
data class MembershipPurchaseCreated(
    val billingIntentId: String = "",
    val outTradeNo: String = "",
    val amountMinor: Double = 0.0,
    val currency: String = "",
    val spaceId: String = "",
    val spaceName: String = "",
    val plan: MembershipPurchasePlan? = null,
)

@Serializable
data class MembershipPurchasePlan(
    val key: String = "",
    val label: String = "",
    val months: Double = 0.0,
    val factorBps: Double = 0.0,
    val totalPriceCents: Double = 0.0,
)

@Serializable
data class MobileMembershipPageResponse(
    val ok: Boolean = false,
    val code: String? = null,
    val space: MobileMembershipSpace? = null,
    val membership: MobileMembershipConfig? = null,
    val viewer: MobileMembershipViewer? = null,
)

@Serializable
data class MobileMembershipSpace(
    val id: String = "",
    val number: Double? = null,
    val name: String = "",
    val description: String? = null,
    val avatarUrl: String = "",
)

@Serializable
data class MobileMembershipConfig(
    val enabled: Boolean = false,
    val description: String? = null,
    val monthlyBasePriceCents: Double = 0.0,
    val plans: List<MobileMembershipPlan> = emptyList(),
)

@Serializable
data class MobileMembershipPlan(
    val key: String = "",
    val label: String = "",
    val months: Double = 0.0,
    val factorBps: Double = 0.0,
    val totalPriceCents: Double = 0.0,
    val currency: String = "CNY",
)

@Serializable
data class MobileMembershipViewer(
    val isManager: Boolean = false,
    val isMember: Boolean = false,
    val isPermanentMember: Boolean = false,
    val activeUntil: Double? = null,
)

@Serializable
data class MobileMembershipPurchaseStatusResponse(
    val ok: Boolean = false,
    val code: String? = null,
    val found: Boolean = false,

    val billingIntentId: String = "",
    val outTradeNo: String = "",
    val status: String = "",

    val paidAt: Double? = null,
    val settledAt: Double? = null,
    val failedAt: Double? = null,
    val failureCode: String? = null,
    val dueAt: Double? = null,

    val amountMinor: Double? = null,
    val currency: String? = null,

    val spaceId: String? = null,
    val spaceName: String? = null,

    val planKey: String? = null,
    val planLabel: String? = null,
    val months: Double? = null,

    val paymentProvider: String? = null,
    val paymentChannel: String? = null,
    val prepayId: String? = null,
)