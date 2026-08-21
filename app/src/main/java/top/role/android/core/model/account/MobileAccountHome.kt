package top.role.android.core.model.account

import kotlinx.serialization.Serializable


@Serializable
data class MobileAccountHome(
    val ok: Boolean = false,
    val balance: Double = 0.0,
    val accountProfile: MobileAccountProfile =
        MobileAccountProfile(),
)


@Serializable
data class MobileAccountProfile(
    val name: String = "",
    val avatarUrl: String = "",
    val phone: String = "",
    val email: String = "",
    val hasPassword: Boolean = false,
    val passwordTip: String = "",
)