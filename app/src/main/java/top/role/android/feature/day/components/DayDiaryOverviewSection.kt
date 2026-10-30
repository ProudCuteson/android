package top.role.android.feature.day.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DayDiaryOverviewSection(
    selectedDayKey: String,
    hasDiary: Boolean,
    expanded: Boolean,
    onToggleCalendar: () -> Unit,
    onRead: () -> Unit,
    onCreate: () -> Unit,
) {
    var showCreateDialog by rememberSaveable {
        mutableStateOf(false)
    }

    val dateText = LocalDate.parse(selectedDayKey).format(
        DateTimeFormatter.ofPattern("M月d日")
    )

    val statusColor = if (hasDiary) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 10.dp)
        ,
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.weight(1f)
                    .clickable(onClick = onToggleCalendar)
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Today,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )

                Text(
                    text = dateText,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )

                Icon(
                    imageVector = if (expanded) {
                        Icons.Default.KeyboardArrowUp
                    } else {
                        Icons.Default.KeyboardArrowDown
                    },
                    contentDescription = if (expanded) "收起月历" else "展开月历",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.weight(1f))

                Box(
                    modifier = Modifier.size(7.dp)
                        .background(statusColor, CircleShape),
                )

                Text(
                    text = if (hasDiary) "已记录" else "未记录",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }

            IconButton(
                onClick = if (hasDiary) {
                    onRead
                } else {
                    { showCreateDialog = true }
                },
            ) {
                Icon(
                    imageVector = if (hasDiary) {
                        Icons.Outlined.Book
                    } else {
                        Icons.Outlined.Add
                    },
                    contentDescription = if (hasDiary) {
                        "阅读日记"
                    } else {
                        "新建日记"
                    },
                    tint = if (hasDiary) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(23.dp),
                )
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("暂无日记") },
            text = { Text("这一天还没有日记，是否新建一篇？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCreateDialog = false
                        onCreate()
                    },
                ) {
                    Text("新建日记")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCreateDialog = false },
                ) {
                    Text("取消")
                }
            },
        )
    }
}