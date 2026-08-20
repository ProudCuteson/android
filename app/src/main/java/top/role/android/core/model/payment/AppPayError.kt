package top.role.android.core.model.payment

import kotlinx.serialization.Serializable

@Serializable
data class AppPayError(
    val ok: Boolean = false,
    val code: String? = null,
    val message: String? = null,
)