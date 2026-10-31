package top.role.android.feature.day

import java.time.LocalDate
import java.time.YearMonth
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import top.role.android.core.data.image.NoteImageService
import top.role.android.core.data.note.NoteLocalRepository
import top.role.android.core.model.note.DayKey
import top.role.android.core.preferences.WeekStart
import top.role.android.feature.day.components.DayCalendar
import top.role.android.feature.day.components.RecentNoteRow
import top.role.android.feature.day.components.DayDiaryOverviewSection

@Composable
fun DayScreen(
    ownerUserId: String,
    noteLocalRepository: NoteLocalRepository,
    noteImageService: NoteImageService,
    weekStart: WeekStart,
    onOpenDayNote: (String) -> Unit,
    onOpenNote: (String) -> Unit,
    onCreateDatedNote: (String) -> Unit,
    onCreateMaterial: () -> Unit,
    onOpenLibrary: () -> Unit,
) {
    val viewModel: DayHomeViewModel = viewModel(
        key = "day-home:$ownerUserId",
        factory = DayHomeViewModelFactory(
            ownerUserId = ownerUserId,
            noteLocalRepository = noteLocalRepository,
            noteImageService = noteImageService,
        ),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showMonthView by rememberSaveable { mutableStateOf(false) }

    val todayKey = DayKey.today()
    val selectedDate = LocalDate.parse(state.selectedDayKey)
    val selectedMonth = YearMonth.from(selectedDate)

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 20.dp),
    ) {
        item {
            Text(
                text = "快速创建",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 20.dp, bottom = 10.dp),
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                DayActionTile(
                    title = "日记",
                    subtitle = "天天记录",
                    icon = Icons.Outlined.Edit,
                    modifier = Modifier.weight(1f),
                    onClick = { onOpenDayNote(todayKey) },
                )
                DayActionTile(
                    title = "素材",
                    subtitle = "随时创作",
                    icon = Icons.Outlined.Image,
                    modifier = Modifier.weight(1f),
                    onClick = onCreateMaterial,
                )
            }
        }

        item {
            DayDiaryOverviewSection(
                selectedDayKey = state.selectedDayKey,
                hasDiary = state.selectedDayDiary != null,
                expanded = showMonthView,
                onToggleCalendar = {
                    showMonthView = !showMonthView
                },
                onRead = {
                    state.selectedDayDiary?.let { diary ->
                        onOpenNote(diary.localKey)
                    }
                },
                onCreate = {
                    onOpenDayNote(state.selectedDayKey)
                },
            )
        }

        if (showMonthView) {
            item {
                DayCalendar(
                    yearMonth = selectedMonth,
                    selectedDayKey = state.selectedDayKey,
                    dottedDayKeys = state.dottedDayKeys,
                    todayDayKey = todayKey,
                    weekStart = weekStart,
                    onPreviousMonth = {
                        viewModel.selectDay(
                            selectedMonth
                                .minusMonths(1)
                                .atDay(1)
                                .toString(),
                        )
                    },
                    onNextMonth = {
                        viewModel.selectDay(
                            selectedMonth
                                .plusMonths(1)
                                .atDay(1)
                                .toString(),
                        )
                    },
                    onSelectDay = viewModel::selectDay,
                )
            }

        }

        item {
            Text(
                text = "本月记录",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(
                    top = 20.dp,
                    bottom = 10.dp,
                ),
            )
        }

        if (state.recentNotes.isEmpty()) {
            item {
                Text(
                    text = "本月还没有记录",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
        } else {
            items(
                count = state.recentNotes.size,
                key = { index -> state.recentNotes[index].localKey },
            ) { index ->
                val note = state.recentNotes[index]

                RecentNoteRow(
                    note = note,
                    imageCount = state.imageCounts[note.localKey] ?: 0,
                    onClick = { onOpenNote(note.localKey) },
                )
            }
        }

        item {
            OutlinedButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 20.dp),
                onClick = onOpenLibrary,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Icon(
                    imageVector = Icons.Outlined.FolderOpen,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.size(8.dp))
                Text("前往 记录库")
            }
        }
    }
}

@Composable
private fun DayActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 76.dp).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}