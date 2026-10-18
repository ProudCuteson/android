package top.role.android.feature.day

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

import top.role.android.core.data.note.NoteLocalRepository
import top.role.android.core.model.note.DayKey
import top.role.android.core.preferences.WeekStart
import top.role.android.feature.day.components.DayCalendar
import top.role.android.feature.day.components.RecentNoteRow
import top.role.android.feature.day.components.SelectedDaySection

import java.time.LocalDate
import java.time.YearMonth


@Composable
fun DayScreen(
    ownerUserId: String,
    noteLocalRepository: NoteLocalRepository,
    weekStart: WeekStart,
    onOpenDayNote: (String) -> Unit,
    onOpenNote: (String) -> Unit,
    onCreateDatedNote: (String) -> Unit,
) {
    val viewModel: DayHomeViewModel =
        viewModel(
            factory = DayHomeViewModelFactory(
                ownerUserId = ownerUserId,
                noteLocalRepository = noteLocalRepository,
            ),
        )

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedDate = LocalDate.parse(state.selectedDayKey)
    val selectedMonth = YearMonth.from(selectedDate)

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(
            horizontal = 16.dp,
            ),

        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {

        item {
            DayCalendar(
                yearMonth = selectedMonth,
                selectedDayKey = state.selectedDayKey,
                dottedDayKeys = state.dottedDayKeys,
                todayDayKey = DayKey.today(),
                weekStart = weekStart,
                onPreviousMonth = {
                    val previousMonth = selectedMonth.minusMonths(1)
                    viewModel.selectDay(
                        previousMonth.atDay(1).toString()
                    )
                },
                onNextMonth = {
                    val nextMonth = selectedMonth.plusMonths(1)
                    viewModel.selectDay(nextMonth.atDay(1) .toString()
                    )
                },
                onSelectDay = viewModel::selectDay,
            )
        }

        item {
            SelectedDaySection(
                selectedDayKey = state.selectedDayKey,
                diary = state.selectedDayDiary,
                datedNotes = state.selectedDayNotes,
                onOpenDayNote = {onOpenDayNote(state.selectedDayKey)},
                onCreateDatedNote = {onCreateDatedNote(state.selectedDayKey )},
                onOpenNote = onOpenNote,
            )
        }

        if (
            state.recentNotes.isNotEmpty()
        ) {
            item {
                Text(
                    text = "本月近期",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            items(
                count = state.recentNotes.size,
                key = { index -> state.recentNotes[index].localKey },
            ) { index ->
                val note = state.recentNotes[index]
                RecentNoteRow(
                    note = note,
                    onClick = {onOpenNote(note.localKey)},
                )
            }
        }

        item {
            Spacer(
                modifier = Modifier.height(24.dp)
            )
        }
    }
}