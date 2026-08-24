package top.role.android.feature.day.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

import top.role.android.core.database.note.LocalNoteEntity


@Composable
fun DayNoteRow(
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
                    12.dp
                ),

            verticalArrangement =
                Arrangement.spacedBy(
                    4.dp
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
fun RecentNoteRow(
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
                    14.dp
                ),

            verticalArrangement =
                Arrangement.spacedBy(
                    4.dp
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
                                80
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
                ?.let { dayKey ->

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
