package top.role.android.feature.account

import top.role.android.core.model.account.MobileAccountHome

data class AccountUiState(
    val loading: Boolean = true,
    val account: MobileAccountHome? = null,
    val errorMessage: String? = null,
)