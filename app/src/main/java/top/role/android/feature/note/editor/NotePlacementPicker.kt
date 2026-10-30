package top.role.android.feature.note.editor

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotePlacementPicker(
    dayKey: String?,
    onChange: (String?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    val label = remember(dayKey) {
        dayKey?.let {
            runCatching {
                LocalDate.parse(it).format(
                    DateTimeFormatter.ofPattern("M月d日")
                )
            }.getOrNull()
        } ?: "关联日期"
    }

    TextButton(
        onClick = { expanded = true },
        contentPadding = PaddingValues(horizontal = 8.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.Today,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Text(label)
    }

    if (expanded) {
        val initialMillis = remember(dayKey) {
            val date = dayKey?.let {
                runCatching { LocalDate.parse(it) }.getOrNull()
            } ?: LocalDate.now()

            date.atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
        }

        val picker = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis,
        )

        DatePickerDialog(
            onDismissRequest = { expanded = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        picker.selectedDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                            onChange(date.toString())
                        }
                        expanded = false
                    },
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                Row {
                    if (dayKey != null) {
                        TextButton(
                            onClick = {
                                onChange(null)
                                expanded = false
                            },
                        ) {
                            Text("清除关联")
                        }
                    }

                    TextButton(onClick = { expanded = false }) {
                        Text("取消")
                    }
                }
            },
        ) {
            DatePicker(state = picker)
        }
    }
}