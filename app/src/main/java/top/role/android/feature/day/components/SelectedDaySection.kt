package top.role.android.feature.day.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import top.role.android.core.database.note.LocalNoteEntity

import java.time.LocalDate
import java.time.format.DateTimeFormatter


@Composable
fun SelectedDaySection(
    selectedDayKey: String,
    diary: LocalNoteEntity?,
    datedNotes: List<LocalNoteEntity>,
    onOpenDayNote: () -> Unit,
    onCreateDatedNote: () -> Unit,
    onOpenNote: (String) -> Unit,
) {
    val date =
        LocalDate.parse(
            selectedDayKey
        )

    val formatter =
        DateTimeFormatter.ofPattern(
            "M月d日"
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
                    16.dp
                ),

            verticalArrangement =
                Arrangement.spacedBy(
                    16.dp
                ),
        ) {

            Text(
                text =
                    date.format(
                        formatter
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
                8.dp
            ),
    ) {

        Text(
            text =
                "日记",

            style =
                MaterialTheme
                    .typography
                    .labelLarge,

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant,
        )


        if (
            diary == null
        ) {

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
                        diary.localKey
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
                8.dp
            ),
    ) {

        Text(
            text =
                "记录",

            style =
                MaterialTheme
                    .typography
                    .labelLarge,

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant,
        )


        if (
            datedNotes.isEmpty()
        ) {

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
                            note.localKey
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