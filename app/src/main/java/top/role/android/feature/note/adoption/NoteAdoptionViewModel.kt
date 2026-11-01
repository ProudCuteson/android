
package top.role.android.feature.note.adoption

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import top.role.android.core.auth.AuthState
import top.role.android.core.data.note.NoteAdoptionLocalRepository

class NoteAdoptionViewModel(
    private val guestLocalOwnerId: String,
    private val repository: NoteAdoptionLocalRepository,
    initialAuthState: AuthState,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            NoteAdoptionUiState(
                authState = initialAuthState,
            )
        )

    val uiState: StateFlow<NoteAdoptionUiState> =
        _uiState.asStateFlow()

    private var observeJob: Job? = null

    private var currentAccountUserId: String? = null

    init {
        setAuthState(initialAuthState)
    }

    // MARK: - Auth

    fun setAuthState(authState: AuthState) {
        _uiState.value = _uiState.value.copy(
            authState = authState,
        )

        val userId = (authState as? AuthState.SignedIn)?.userId

        if (userId == currentAccountUserId) return

        currentAccountUserId = userId

        observeJob?.cancel()
        observeJob = null

        if (userId == null) {
            _uiState.value = _uiState.value.copy(
                notes = emptyList(),
                loading = false,
                isClaiming = false,
                claimedCount = null,
            )
            return
        }

        observeUnclaimedNotes()
    }

    // MARK: - Observe

    private fun observeUnclaimedNotes() {
        observeJob?.cancel()

        _uiState.value = _uiState.value.copy(
            loading = true,
            errorMessage = null,
        )

        observeJob = viewModelScope.launch {
            repository
                .observeUnclaimedNotes(
                    guestLocalOwnerId = guestLocalOwnerId,
                )
                .collect { notes ->
                    _uiState.value = _uiState.value.copy(
                        notes = notes,
                        loading = false,
                    )
                }
        }
    }

    // MARK: - Claim

    fun claimAll() {
        val accountUserId = currentAccountUserId
            ?: return

        val current = _uiState.value

        if (!current.canClaim) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isClaiming = true,
                errorMessage = null,
                claimedCount = null,
            )

            try {
                val count = repository.claimGuestNotes(
                    guestLocalOwnerId = guestLocalOwnerId,
                    accountUserId = accountUserId,
                )

                _uiState.value = _uiState.value.copy(
                    isClaiming = false,
                    claimedCount = count,
                )

            } catch (error: CancellationException) {
                throw error

            } catch (error: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isClaiming = false,
                    errorMessage = error.message
                        ?: "认领记录失败，请稍后重试",
                )
            }
        }
    }
}