package top.role.android.feature.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job

class AccountViewModel(
    private val repository: AccountRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()
    private var accountJob: Job? = null

    init { observeAccountHome() }
    private fun observeAccountHome() {
        accountJob?.cancel()

        accountJob = viewModelScope.launch {

            _uiState.update {
                it.copy(
                    loading = it.account == null,
                    errorMessage = null,
                )
            }

            repository
                .observeAccountHome()
                .collect { result ->

                    result
                        .onSuccess { account ->

                            _uiState.value =
                                AccountUiState(
                                    loading = false,
                                    account = account,
                                    errorMessage = null,
                                )
                        }
                        .onFailure { error ->
                            android.util.Log.e("AccountHome", "Account subscription failed", error)

                            _uiState.update {
                                it.copy(
                                    loading = false,
                                    errorMessage = "暂时无法加载账户信息，请稍后再试。",
                                )
                            }
                        }
                }
        }
    }

    fun retry() {
        observeAccountHome()
    }

    companion object {

        fun factory(
            repository:
            AccountRepository,
        ): ViewModelProvider.Factory {

            return object :
                ViewModelProvider.Factory {

                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass:
                    Class<T>,
                ): T {

                    return AccountViewModel(
                        repository =
                            repository,
                    ) as T
                }
            }
        }
    }
}