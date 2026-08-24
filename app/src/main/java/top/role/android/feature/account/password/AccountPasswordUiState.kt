package top.role.android.feature.account.password


data class AccountPasswordUiState(
    val loading: Boolean = true,

    /*
     * 当前账户是否已经设置
     * phone-password。
     */
    val hasPassword: Boolean = false,

    val currentPassword: String = "",
    val nextPassword: String = "",
    val confirmPassword: String = "",

    val submitting: Boolean = false,

    val errorMessage: String? = null,

    /*
     * 修改 / 设置成功。
     */
    val success: Boolean = false,

    /*
     * 后端明确要求修改认证凭据后
     * 重新登录。
     */
    val shouldLogout: Boolean = false,
)