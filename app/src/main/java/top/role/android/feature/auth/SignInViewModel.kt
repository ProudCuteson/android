package top.role.android.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import top.role.android.core.auth.AuthRepository
import top.role.android.core.auth.AuthException
import top.role.android.core.storage.SessionStorage
import top.role.android.core.auth.AppSession

data class SignInUiState(
    val phone: String = "",
    val code: String = "",

    val otpSent: Boolean = false,
    val maskedPhone: String? = null,

    val resendAfter: Int = 0,

    val isSendingOtp: Boolean = false,
    val isSigningIn: Boolean = false,

    val errorMessage: String? = null,
)

class SignInViewModel(
    private val authRepository: AuthRepository,
    private val sessionStorage: SessionStorage,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignInUiState())

    val uiState: StateFlow<SignInUiState> =
        _uiState.asStateFlow()

    fun updatePhone(value: String) {
        val normalized = value
            .filter { it.isDigit() }
            .take(11)

        _uiState.update {
            it.copy(
                phone = normalized,
                errorMessage = null,
            )
        }
    }

    fun updateCode(value: String) {
        val normalized = value
            .filter { it.isDigit() }
            .take(8)

        _uiState.update {
            it.copy(
                code = normalized,
                errorMessage = null,
            )
        }
    }

    fun sendOtp() {
        val phone = _uiState.value.phone

        if (!isValidMainlandPhone(phone)) {
            _uiState.update {
                it.copy(
                    errorMessage = "请输入有效的中国大陆手机号",
                )
            }
            return
        }

        if (_uiState.value.isSendingOtp) {
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSendingOtp = true,
                    errorMessage = null,
                )
            }

            authRepository
                .sendOtp(phone)
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            otpSent = true,
                            maskedPhone = result.maskedPhone,
                            resendAfter = result.resendAfter,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            errorMessage = error.toUserMessage(),
                        )
                    }
                }

            _uiState.update {
                it.copy(
                    isSendingOtp = false,
                )
            }
        }
    }

    fun verifyAndSignIn() {
        val state = _uiState.value

        if (!isValidMainlandPhone(state.phone)) {
            _uiState.update {
                it.copy(
                    errorMessage = "请输入有效的中国大陆手机号",
                )
            }
            return
        }

        if (state.code.length !in 4..8) {
            _uiState.update {
                it.copy(
                    errorMessage = "请输入有效验证码",
                )
            }
            return
        }

        if (state.isSigningIn) {
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSigningIn = true,
                    errorMessage = null,
                )
            }

            val verifyResult = authRepository.verifyOtp(
                phone = state.phone,
                code = state.code,
            )

            verifyResult
                .onSuccess { verified ->
                    authRepository
                        .exchangeOtp(
                            ticket = verified.ticket,
                        )
                        .onSuccess { login ->
                            sessionStorage.saveSession(
                                AppSession(
                                    accessToken = login.accessToken,
                                    expiresAt = login.expiresAt,
                                    userId = login.user.id,
                                    phone = login.user.phone,
                                )
                            )
                        }
                        .onFailure { error ->
                            _uiState.update {
                                it.copy(
                                    errorMessage = error.toUserMessage(),
                                )
                            }
                        }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            errorMessage = error.toUserMessage(),
                        )
                    }
                }

            _uiState.update {
                it.copy(
                    isSigningIn = false,
                )
            }
        }
    }

    fun backToPhone() {
        _uiState.update {
            it.copy(
                otpSent = false,
                code = "",
                errorMessage = null,
            )
        }
    }

    private fun isValidMainlandPhone(
        phone: String,
    ): Boolean {
        return phone.length == 11 &&
                phone.startsWith("1")
    }
}

private fun Throwable.toUserMessage(): String {
    return when (this) {
        is AuthException -> message
        else -> "网络连接失败，请稍后重试"
    }
}

class SignInViewModelFactory(
    private val authRepository: AuthRepository,
    private val sessionStorage: SessionStorage,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
    ): T {
        if (
            modelClass.isAssignableFrom(
                SignInViewModel::class.java
            )
        ) {
            return SignInViewModel(
                authRepository = authRepository,
                sessionStorage = sessionStorage,
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}