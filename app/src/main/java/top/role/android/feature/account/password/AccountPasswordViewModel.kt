package top.role.android.feature.account.password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import top.role.android.feature.account.AccountRepository


class AccountPasswordViewModel(
    private val accountRepository:
    AccountRepository,

    private val passwordRepository:
    AccountPasswordRepository,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            AccountPasswordUiState()
        )

    val uiState:
            StateFlow<AccountPasswordUiState> =
        _uiState.asStateFlow()


    private var accountJob:
            Job? = null


    init {
        observeAccount()
    }


    private fun observeAccount() {

        accountJob?.cancel()

        accountJob =
            viewModelScope.launch {

                accountRepository
                    .observeAccountHome()
                    .collectLatest { result ->

                        result.fold(
                            onSuccess = { account ->

                                if (
                                    !account.ok
                                ) {
                                    _uiState.update {
                                        it.copy(
                                            loading =
                                                false,

                                            errorMessage =
                                                "暂时无法读取账户安全状态。",
                                        )
                                    }

                                    return@fold
                                }


                                _uiState.update {
                                    it.copy(
                                        loading =
                                            false,

                                        hasPassword =
                                            account
                                                .accountProfile
                                                .hasPassword,

                                        errorMessage =
                                            null,
                                    )
                                }
                            },

                            onFailure = {

                                _uiState.update {
                                    it.copy(
                                        loading =
                                            false,

                                        errorMessage =
                                            "暂时无法读取账户安全状态。",
                                    )
                                }
                            },
                        )
                    }
            }
    }


    fun updateCurrentPassword(
        value:
        String,
    ) {
        if (
            _uiState.value.submitting
        ) {
            return
        }

        _uiState.update {
            it.copy(
                currentPassword =
                    value,

                errorMessage =
                    null,
            )
        }
    }


    fun updateNextPassword(
        value:
        String,
    ) {
        if (
            _uiState.value.submitting
        ) {
            return
        }

        _uiState.update {
            it.copy(
                nextPassword =
                    value,

                errorMessage =
                    null,
            )
        }
    }


    fun updateConfirmPassword(
        value:
        String,
    ) {
        if (
            _uiState.value.submitting
        ) {
            return
        }

        _uiState.update {
            it.copy(
                confirmPassword =
                    value,

                errorMessage =
                    null,
            )
        }
    }


    fun submit() {

        val state =
            _uiState.value


        if (
            state.loading ||
            state.submitting
        ) {
            return
        }


        val nextPassword =
            state.nextPassword

        val confirmPassword =
            state.confirmPassword


        if (
            nextPassword.isBlank()
        ) {
            setError(
                "请输入新密码。"
            )

            return
        }


        if (
            confirmPassword.isBlank()
        ) {
            setError(
                "请再次输入新密码。"
            )

            return
        }


        if (
            nextPassword !=
            confirmPassword
        ) {
            setError(
                "两次输入的新密码不一致。"
            )

            return
        }


        if (
            state.hasPassword &&
            state.currentPassword
                .isBlank()
        ) {
            setError(
                "请输入当前密码。"
            )

            return
        }


        if (
            state.hasPassword
        ) {
            changePassword(
                currentPassword =
                    state.currentPassword,

                nextPassword =
                    nextPassword,
            )
        } else {
            setupPassword(
                password =
                    nextPassword,
            )
        }
    }


    private fun setupPassword(
        password:
        String,
    ) {

        viewModelScope.launch {

            beginSubmit()


            val result =
                passwordRepository
                    .setupPassword(
                        password =
                            password,
                    )


            result.fold(
                onSuccess = { response ->

                    if (
                        response.ok
                    ) {
                        completeSuccess(
                            shouldLogout =
                                response.shouldLogout,
                        )

                        return@fold
                    }


                    completeFailure(
                        message =
                            passwordErrorMessage(
                                code =
                                    response.code,

                                fallback =
                                    response.message,
                            ),
                    )
                },

                onFailure = { error ->

                    completeFailure(
                        message =
                            error.message
                                ?.takeIf {
                                    it.isNotBlank()
                                }
                                ?: "暂时无法设置密码。",
                    )
                },
            )
        }
    }


    private fun changePassword(
        currentPassword:
        String,

        nextPassword:
        String,
    ) {

        viewModelScope.launch {

            beginSubmit()


            val result =
                passwordRepository
                    .changePassword(
                        currentPassword =
                            currentPassword,

                        nextPassword =
                            nextPassword,
                    )


            result.fold(
                onSuccess = { response ->

                    if (
                        response.ok
                    ) {
                        completeSuccess(
                            shouldLogout =
                                response.shouldLogout,
                        )

                        return@fold
                    }


                    completeFailure(
                        message =
                            passwordErrorMessage(
                                code =
                                    response.code,

                                fallback =
                                    response.message,
                            ),
                    )
                },

                onFailure = { error ->

                    completeFailure(
                        message =
                            error.message
                                ?.takeIf {
                                    it.isNotBlank()
                                }
                                ?: "暂时无法修改密码。",
                    )
                },
            )
        }
    }


    private fun beginSubmit() {

        _uiState.update {
            it.copy(
                submitting =
                    true,

                success =
                    false,

                shouldLogout =
                    false,

                errorMessage =
                    null,
            )
        }
    }


    private fun completeSuccess(
        shouldLogout:
        Boolean,
    ) {

        _uiState.update {
            it.copy(
                submitting =
                    false,

                /*
                 * 修改成功后本地直接更新，
                 * 不需要等 Account Home
                 * subscription 再回来。
                 */
                hasPassword =
                    true,

                currentPassword =
                    "",

                nextPassword =
                    "",

                confirmPassword =
                    "",

                success =
                    true,

                shouldLogout =
                    shouldLogout,

                errorMessage =
                    null,
            )
        }
    }


    private fun completeFailure(
        message:
        String,
    ) {

        _uiState.update {
            it.copy(
                submitting =
                    false,

                success =
                    false,

                shouldLogout =
                    false,

                errorMessage =
                    message,
            )
        }
    }


    private fun setError(
        message:
        String,
    ) {

        _uiState.update {
            it.copy(
                errorMessage =
                    message,
            )
        }
    }


    fun consumeSuccess() {

        _uiState.update {
            it.copy(
                success =
                    false,
            )
        }
    }


    fun consumeShouldLogout() {

        _uiState.update {
            it.copy(
                shouldLogout =
                    false,
            )
        }
    }


    fun clearError() {

        _uiState.update {
            it.copy(
                errorMessage =
                    null,
            )
        }
    }


    private fun passwordErrorMessage(
        code:
        String?,

        fallback:
        String?,
    ): String {

        return when (
            code
        ) {

            "CURRENT_PASSWORD_REQUIRED" ->
                "请输入当前密码。"

            "CURRENT_PASSWORD_INCORRECT" ->
                "当前密码不正确。"

            "PASSWORD_NOT_SET" ->
                "当前账户尚未设置密码。"

            "NEXT_PASSWORD_TOO_SHORT",
            "PASSWORD_TOO_SHORT" ->
                "新密码长度不足，请使用更长的密码。"

            "PASSWORD_HAS_EDGE_SPACE" ->
                "密码开头和结尾不能包含空格。"

            "PHONE_REQUIRED" ->
                "当前账户尚未绑定手机号。"

            "PASSWORD_ALREADY_SET" ->
                "当前账户已经设置密码。"

            "PHONE_ALREADY_BOUND" ->
                "该手机号已绑定其他密码账户。"

            "HASH_FAILED" ->
                "暂时无法生成新密码，请稍后再试。"

            "INTERNAL_ERROR" ->
                "暂时无法处理密码设置，请稍后再试。"

            else ->
                fallback
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?: "暂时无法处理密码操作。"
        }
    }


    companion object {

        fun factory(
            accountRepository:
            AccountRepository,

            passwordRepository:
            AccountPasswordRepository,
        ): ViewModelProvider.Factory {

            return object :
                ViewModelProvider.Factory {

                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass:
                    Class<T>,
                ): T {

                    if (
                        modelClass.isAssignableFrom(
                            AccountPasswordViewModel::class.java
                        )
                    ) {
                        return AccountPasswordViewModel(
                            accountRepository =
                                accountRepository,

                            passwordRepository =
                                passwordRepository,
                        ) as T
                    }


                    throw IllegalArgumentException(
                        "Unknown ViewModel class: ${modelClass.name}"
                    )
                }
            }
        }
    }
}