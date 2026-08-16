package top.role.android.feature.day

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import top.role.android.core.data.note.NoteLocalRepository
import top.role.android.core.model.note.DayKey

class DayHomeViewModel(
    private val ownerUserId: String,
    private val noteLocalRepository:
    NoteLocalRepository,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            DayHomeUiState(),
        )

    val uiState:
            StateFlow<DayHomeUiState> =
        _uiState.asStateFlow()

    private var monthObserveJob:
            Job? = null

    init {
        observeSelectedMonth()
    }

    fun selectDay(
        dayKey: String,
    ) {
        if (!DayKey.isValid(dayKey)) {
            return
        }

        val oldMonth =
            DayKey.monthKey(
                _uiState.value
                    .selectedDayKey,
            )

        val newMonth =
            DayKey.monthKey(
                dayKey,
            )

        _uiState.value =
            _uiState.value.copy(
                selectedDayKey =
                    dayKey,
            )

        if (oldMonth != newMonth) {
            observeSelectedMonth()
        }
    }

    private fun observeSelectedMonth() {
        val selectedDayKey =
            _uiState.value
                .selectedDayKey

        val range =
            DayKey.monthRange(
                selectedDayKey,
            )

        _uiState.value =
            _uiState.value.copy(
                monthStartDayKey =
                    range.start,
                monthEndDayKeyExclusive =
                    range.endExclusive,
            )

        monthObserveJob
            ?.cancel()

        monthObserveJob =
            viewModelScope.launch {
                noteLocalRepository
                    .observeMonthNotesForDayHome(
                        ownerUserId =
                            ownerUserId,
                        startDayKey =
                            range.start,
                        endDayKeyExclusive =
                            range.endExclusive,
                    )
                    .collect {
                            notes ->

                        _uiState.value =
                            _uiState.value.copy(
                                monthNotes =
                                    notes,
                            )
                    }
            }
    }
}