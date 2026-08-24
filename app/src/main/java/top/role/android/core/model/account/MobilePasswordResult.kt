package top.role.android.core.model.account

import kotlinx.serialization.Serializable


@Serializable
data class MobilePasswordResult(
    val ok: Boolean = false,

    val provider: String? = null,

    val shouldLogout: Boolean = false,

    val code: String? = null,

    val message: String? = null,
)