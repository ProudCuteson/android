package top.role.android.feature.day

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import top.role.android.core.data.note.NoteLocalRepository
import top.role.android.core.database.note.LocalNoteEntity
import top.role.android.core.model.note.DayKey
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun DayScreen(
    ownerUserId: String,
    noteLocalRepository: NoteLocalRepository,
    onOpenDayNote: (String) -> Unit,
    onOpenNote: (String) -> Unit,
    onCreateDatedNote: (String) -> Unit,
) {
    val viewModel: DayHomeViewModel =
        viewModel(
            factory =
                DayHomeViewModelFactory(
                    ownerUserId =
                        ownerUserId,
                    noteLocalRepository =
                        noteLocalRepository,
                )
        )

    val state by
    viewModel.uiState
        .collectAsStateWithLifecycle()

    val selectedDate =
        LocalDate.parse(
            state.selectedDayKey,
        )

    val selectedMonth =
        YearMonth.from(
            selectedDate,
        )

    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 16.dp,
                ),

        verticalArrangement =
            Arrangement.spacedBy(
                16.dp,
            ),
    ) {

        item {
            MonthHeader(
                yearMonth =
                    selectedMonth,

                onPreviousMonth = {
                    val previousMonth =
                        selectedMonth
                            .minusMonths(1)

                    viewModel.selectDay(
                        previousMonth
                            .atDay(1)
                            .toString()
                    )
                },

                onNextMonth = {
                    val nextMonth =
                        selectedMonth
                            .plusMonths(1)

                    viewModel.selectDay(
                        nextMonth
                            .atDay(1)
                            .toString()
                    )
                },
            )
        }

        item {
            CalendarGrid(
                yearMonth =
                    selectedMonth,

                selectedDayKey =
                    state.selectedDayKey,

                dottedDayKeys =
                    state.dottedDayKeys,

                todayDayKey =
                    DayKey.today(),

                onSelectDay = {
                        dayKey ->

                    viewModel.selectDay(
                        dayKey,
                    )
                },
            )
        }

        item {
            SelectedDaySection(
                selectedDayKey =
                    state.selectedDayKey,

                diary =
                    state.selectedDayDiary,

                datedNotes =
                    state.selectedDayNotes,

                onOpenDayNote = {
                    onOpenDayNote(
                        state.selectedDayKey,
                    )
                },

                onCreateDatedNote = {
                    onCreateDatedNote(
                        state.selectedDayKey,
                    )
                },

                onOpenNote =
                    onOpenNote,
            )
        }

        if (
            state.recentNotes
                .isNotEmpty()
        ) {
            item {
                Text(
                    text = "本月近期",
                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,
                    fontWeight =
                        FontWeight.SemiBold,
                )
            }

            items(
                count =
                    state.recentNotes.size,

                key = {
                        index ->

                    state.recentNotes[
                        index
                    ].localKey
                },
            ) { index ->

                RecentNoteRow(
                    note =
                        state.recentNotes[
                            index
                        ],

                    onClick = {
                        onOpenNote(
                            state.recentNotes[
                                index
                            ].localKey
                        )
                    },
                )
            }
        }

        item {
            Spacer(
                modifier =
                    Modifier.height(
                        24.dp,
                    )
            )
        }
    }
}

@Composable
private fun MonthHeader(
    yearMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
) {
    Row(
        modifier =
            Modifier.fillMaxWidth(),

        verticalAlignment =
            Alignment.CenterVertically,

        horizontalArrangement =
            Arrangement.SpaceBetween,
    ) {
        IconButton(
            onClick =
                onPreviousMonth,
        ) {
            Icon(
                imageVector =
                    Icons.AutoMirrored.Filled
                        .KeyboardArrowLeft,

                contentDescription =
                    "上个月",
            )
        }

        Text(
            text =
                "${yearMonth.year}年${yearMonth.monthValue}月",

            style =
                MaterialTheme
                    .typography
                    .titleMedium,

            fontWeight =
                FontWeight.Medium,
        )

        IconButton(
            onClick =
                onNextMonth,
        ) {
            Icon(
                imageVector =
                    Icons.AutoMirrored.Filled
                        .KeyboardArrowRight,

                contentDescription =
                    "下个月",
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    yearMonth: YearMonth,
    selectedDayKey: String,
    dottedDayKeys: Set<String>,
    todayDayKey: String,
    onSelectDay: (String) -> Unit,
) {
    Column(
        verticalArrangement =
            Arrangement.spacedBy(
                6.dp,
            ),
    ) {
        WeekdayHeader()

        val firstDay =
            yearMonth.atDay(1)

        // java.time:
        // Monday = 1
        // Sunday = 7
        val leadingEmptyCount =
            firstDay
                .dayOfWeek
                .value - 1

        val daysInMonth =
            yearMonth
                .lengthOfMonth()

        val cellCount =
            leadingEmptyCount +
                    daysInMonth

        val rowCount =
            (cellCount + 6) / 7

        for (
        rowIndex in
        0 until rowCount
        ) {
            Row(
                modifier =
                    Modifier.fillMaxWidth(),
            ) {
                for (
                columnIndex in
                0 until 7
                ) {
                    val cellIndex =
                        rowIndex * 7 +
                                columnIndex

                    val dayNumber =
                        cellIndex -
                                leadingEmptyCount +
                                1

                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .height(
                                    48.dp,
                                ),

                        contentAlignment =
                            Alignment.Center,
                    ) {
                        if (
                            dayNumber in
                            1..daysInMonth
                        ) {
                            val date =
                                yearMonth
                                    .atDay(
                                        dayNumber,
                                    )

                            val dayKey =
                                date.toString()

                            CalendarDayCell(
                                dayNumber =
                                    dayNumber,

                                selected =
                                    dayKey ==
                                            selectedDayKey,

                                isToday =
                                    dayKey ==
                                            todayDayKey,

                                hasNote =
                                    dayKey in
                                            dottedDayKeys,

                                onClick = {
                                    onSelectDay(
                                        dayKey,
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekdayHeader() {
    val labels =
        listOf(
            "一",
            "二",
            "三",
            "四",
            "五",
            "六",
            "日",
        )

    Row(
        modifier =
            Modifier.fillMaxWidth(),
    ) {
        labels.forEach {
                label ->

            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .height(
                            28.dp,
                        ),

                contentAlignment =
                    Alignment.Center,
            ) {
                Text(
                    text =
                        label,

                    style =
                        MaterialTheme
                            .typography
                            .labelMedium,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    dayNumber: Int,
    selected: Boolean,
    isToday: Boolean,
    hasNote: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .size(
                    44.dp,
                )
                .clickable(
                    onClick =
                        onClick,
                ),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center,
    ) {
        Surface(
            modifier =
                Modifier.size(
                    32.dp,
                ),

            shape =
                CircleShape,

            color =
                when {
                    selected ->
                        MaterialTheme
                            .colorScheme
                            .primary

                    else ->
                        MaterialTheme
                            .colorScheme
                            .surface
                },
        ) {
            Box(
                contentAlignment =
                    Alignment.Center,
            ) {
                Text(
                    text =
                        dayNumber
                            .toString(),

                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,

                    fontWeight =
                        if (
                            isToday ||
                            selected
                        ) {
                            FontWeight.SemiBold
                        } else {
                            FontWeight.Normal
                        },

                    color =
                        if (selected) {
                            MaterialTheme
                                .colorScheme
                                .onPrimary
                        } else {
                            MaterialTheme
                                .colorScheme
                                .onSurface
                        },
                )
            }
        }

        if (hasNote) {
            Box(
                modifier =
                    Modifier
                        .padding(
                            top = 2.dp,
                        )
                        .size(
                            4.dp,
                        )
                        .background(
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .primary,

                            shape =
                                CircleShape,
                        )
            )
        } else {
            Spacer(
                modifier =
                    Modifier.height(
                        6.dp,
                    )
            )
        }
    }
}

@Composable
private fun SelectedDaySection(
    selectedDayKey: String,
    diary: LocalNoteEntity?,
    datedNotes: List<LocalNoteEntity>,
    onOpenDayNote: () -> Unit,
    onCreateDatedNote: () -> Unit,
    onOpenNote: (String) -> Unit,
) {
    val date =
        LocalDate.parse(
            selectedDayKey,
        )

    val formatter =
        DateTimeFormatter.ofPattern(
            "M月d日",
        )

    Surface(
        modifier =
            Modifier.fillMaxWidth(),

        tonalElevation =
            1.dp,

        shape =
            MaterialTheme
                .shapes
                .large,
    ) {
        Column(
            modifier =
                Modifier.padding(
                    16.dp,
                ),

            verticalArrangement =
                Arrangement.spacedBy(
                    16.dp,
                ),
        ) {
            Text(
                text =
                    date.format(
                        formatter,
                    ),

                style =
                    MaterialTheme
                        .typography
                        .titleMedium,

                fontWeight =
                    FontWeight.SemiBold,
            )

            DayDiarySection(
                diary =
                    diary,

                onOpenDayNote =
                    onOpenDayNote,

                onOpenNote =
                    onOpenNote,
            )

            DayDatedNotesSection(
                datedNotes =
                    datedNotes,

                onCreateDatedNote =
                    onCreateDatedNote,

                onOpenNote =
                    onOpenNote,
            )
        }
    }
}

@Composable
private fun DayDiarySection(
    diary: LocalNoteEntity?,
    onOpenDayNote: () -> Unit,
    onOpenNote: (String) -> Unit,
) {
    Column(
        verticalArrangement =
            Arrangement.spacedBy(
                8.dp,
            ),
    ) {
        Text(
            text = "日记",

            style =
                MaterialTheme
                    .typography
                    .labelLarge,

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant,
        )

        if (diary == null) {
            Text(
                text =
                    "这一天还没有日记",

                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant,
            )

            Button(
                modifier =
                    Modifier.fillMaxWidth(),

                onClick =
                    onOpenDayNote,
            ) {
                Text(
                    text =
                        "开始当天日记",
                )
            }
        } else {
            DayNoteRow(
                note =
                    diary,

                onClick = {
                    onOpenNote(
                        diary.localKey,
                    )
                },
            )
        }
    }
}

@Composable
private fun DayDatedNotesSection(
    datedNotes: List<LocalNoteEntity>,
    onCreateDatedNote: () -> Unit,
    onOpenNote: (String) -> Unit,
) {
    Column(
        verticalArrangement =
            Arrangement.spacedBy(
                8.dp,
            ),
    ) {
        Text(
            text = "记录",

            style =
                MaterialTheme
                    .typography
                    .labelLarge,

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant,
        )

        if (datedNotes.isEmpty()) {
            Text(
                text =
                    "这一天还没有其他记录",

                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant,
            )
        } else {
            datedNotes.forEach { note ->
                DayNoteRow(
                    note =
                        note,

                    onClick = {
                        onOpenNote(
                            note.localKey,
                        )
                    },
                )
            }
        }

        OutlinedButton(
            modifier =
                Modifier.fillMaxWidth(),

            onClick =
                onCreateDatedNote,
        ) {
            Text(
                text =
                    "添加记录",
            )
        }
    }
}

@Composable
private fun DayNoteRow(
    note: LocalNoteEntity,
    onClick: () -> Unit,
) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    onClick =
                        onClick,
                ),

        shape =
            MaterialTheme
                .shapes
                .medium,

        color =
            MaterialTheme
                .colorScheme
                .surfaceVariant,
    ) {
        Column(
            modifier =
                Modifier.padding(
                    12.dp,
                ),

            verticalArrangement =
                Arrangement.spacedBy(
                    4.dp,
                ),
        ) {
            Text(
                text =
                    note.title
                        .takeIf {
                            it.isNotBlank()
                        }
                        ?: "无标题",

                style =
                    MaterialTheme
                        .typography
                        .titleSmall,

                fontWeight =
                    FontWeight.Medium,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis,
            )

            if (
                note.text
                    .isNotBlank()
            ) {
                Text(
                    text =
                        note.text,

                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,

                    maxLines =
                        2,

                    overflow =
                        TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun RecentNoteRow(
    note: LocalNoteEntity,
    onClick: () -> Unit,
) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    onClick =
                        onClick,
                ),

        shape =
            MaterialTheme
                .shapes
                .medium,

        tonalElevation =
            1.dp,
    ) {
        Column(
            modifier =
                Modifier.padding(
                    14.dp,
                ),

            verticalArrangement =
                Arrangement.spacedBy(
                    4.dp,
                ),
        ) {
            Text(
                text =
                    note.title
                        .takeIf {
                            it.isNotBlank()
                        }
                        ?: "无标题",

                style =
                    MaterialTheme
                        .typography
                        .titleSmall,

                fontWeight =
                    FontWeight.Medium,
            )

            if (
                note.text
                    .isNotBlank()
            ) {
                Text(
                    text =
                        note.text
                            .take(
                                80,
                            ),

                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,
                )
            }

            note.dayKey
                ?.let {
                        dayKey ->

                    Text(
                        text =
                            dayKey,

                        style =
                            MaterialTheme
                                .typography
                                .labelSmall,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant,
                    )
                }
        }
    }
}