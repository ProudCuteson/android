package top.role.android.feature.bounty.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import top.role.android.core.model.bounty.MobileBountyDetail

data class BountyDetailUiState(
    val loading: Boolean = false,

    val signedOut: Boolean = false,

    val bounty:
    MobileBountyDetail? = null,

    val taking: Boolean = false,

    val takeMessage:
    String? = null,

    val errorMessage:
    String? = null,
)

class BountyDetailViewModel(
    private val bountyId: String,
    private val repository:
    BountyDetailRepository,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            BountyDetailUiState()
        )

    val uiState:
            StateFlow<BountyDetailUiState> =
        _uiState.asStateFlow()

    private var observeJob:
            Job? = null

    fun start() {
        if (
            observeJob?.isActive ==
            true
        ) {
            return
        }

        observeDetail()
    }

    fun showSignedOut() {
        observeJob?.cancel()
        observeJob = null

        _uiState.value =
            BountyDetailUiState(
                signedOut = true,
            )
    }

    fun retry() {
        observeJob?.cancel()
        observeJob = null

        observeDetail()
    }

    fun take() {
        val state =
            _uiState.value

        val bounty =
            state.bounty
                ?: return

        if (
            state.taking ||
            bounty.ownedByMe ||
            bounty.taken ||
            bounty.expired ||
            bounty.status != "open"
        ) {
            return
        }

        _uiState.value =
            state.copy(
                taking = true,
                errorMessage = null,
                takeMessage = null,
            )

        viewModelScope.launch {

            runCatching {
                repository.take(
                    bountyId =
                        bountyId,
                )
            }
                .onSuccess { result ->

                    if (
                        result.ok
                    ) {
                        _uiState.value =
                            _uiState.value.copy(
                                taking =
                                    false,

                                bounty =
                                    _uiState.value
                                        .bounty
                                        ?.copy(
                                            taken =
                                                true,

                                            takeStatus =
                                                result.status
                                                    ?: "taken",
                                        ),

                                takeMessage =
                                    result.message
                                        ?: "领取成功",
                            )
                    } else {
                        _uiState.value =
                            _uiState.value.copy(
                                taking =
                                    false,

                                errorMessage =
                                    when (
                                        result.code
                                    ) {
                                        "OWNER_CANNOT_TAKE" ->
                                            "不能领取自己发布的任务"

                                        "BOUNTY_NOT_OPEN" ->
                                            "任务当前不可领取"

                                        "BOUNTY_EXPIRED" ->
                                            "任务已截止"

                                        else ->
                                            result.message
                                                ?: result.code
                                                ?: "领取失败"
                                    },
                            )
                    }
                }
                .onFailure { error ->

                    _uiState.value =
                        _uiState.value.copy(
                            taking =
                                false,

                            errorMessage =
                                error.message
                                    ?: "领取失败",
                        )
                }
        }
    }

    private fun observeDetail() {
        _uiState.value =
            _uiState.value.copy(
                loading = true,
                signedOut = false,
                errorMessage = null,
            )

        observeJob =
            viewModelScope.launch {

                repository
                    .observeDetail(
                        bountyId =
                            bountyId,
                    )
                    .collect { result ->

                        result.fold(
                            onSuccess = {
                                    response ->

                                if (
                                    !response.ok ||
                                    response.bounty == null
                                ) {
                                    _uiState.value =
                                        _uiState.value.copy(
                                            loading = false,

                                            errorMessage =
                                                response.code
                                                    ?: "任务不可用",
                                        )

                                    return@fold
                                }

                                _uiState.value =
                                    _uiState.value.copy(
                                        loading = false,
                                        bounty =
                                            response.bounty,
                                        errorMessage = null,
                                    )
                            },

                            onFailure = {
                                    error ->

                                _uiState.value =
                                    _uiState.value.copy(
                                        loading = false,

                                        errorMessage =
                                            error.message
                                                ?: "加载任务失败",
                                    )
                            },
                        )
                    }
            }
    }
}