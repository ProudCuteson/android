package top.role.android.feature.day.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import top.role.android.core.preferences.WeekStart

import java.time.YearMonth


@Composable
fun DayCalendar(
    yearMonth: YearMonth,
    selectedDayKey: String,
    dottedDayKeys: Set<String>,
    todayDayKey: String,
    weekStart: WeekStart,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDay: (String) -> Unit,
) {
    Column(
        verticalArrangement =
            Arrangement.spacedBy(
                8.dp
            ),
    ) {

        MonthHeader(
            yearMonth =
                yearMonth,

            onPreviousMonth =
                onPreviousMonth,

            onNextMonth =
                onNextMonth,
        )


        CalendarGrid(
            yearMonth =
                yearMonth,

            selectedDayKey =
                selectedDayKey,

            dottedDayKeys =
                dottedDayKeys,

            todayDayKey =
                todayDayKey,

            weekStart =
                weekStart,

            onSelectDay =
                onSelectDay,
        )
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
                    Icons
                        .AutoMirrored
                        .Filled
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
                    Icons
                        .AutoMirrored
                        .Filled
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
    weekStart: WeekStart,
    onSelectDay: (String) -> Unit,
) {
    Column(
        verticalArrangement =
            Arrangement.spacedBy(
                6.dp
            ),
    ) {

        WeekdayHeader(
            weekStart = weekStart,
            )

        val firstDay =
            yearMonth.atDay(1)

        val leadingEmptyCount =
            when (
                weekStart
            ) {

                WeekStart.Monday -> {
                    /*
                     * java.time:
                     * Monday = 1
                     * Sunday = 7
                     */
                    firstDay
                        .dayOfWeek
                        .value - 1
                }

                WeekStart.Sunday -> {
                    /*
                     * Sunday:
                     * Sunday = 0
                     * Monday = 1
                     * ...
                     * Saturday = 6
                     */
                    firstDay
                        .dayOfWeek
                        .value % 7
                }
            }

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
                                    48.dp
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
                                        dayNumber
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
                                        dayKey
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
private fun WeekdayHeader(
    weekStart: WeekStart,
) {
    val labels =
        when (
            weekStart
        ) {

            WeekStart.Monday ->
                listOf(
                    "一",
                    "二",
                    "三",
                    "四",
                    "五",
                    "六",
                    "日",
                )

            WeekStart.Sunday ->
                listOf(
                    "日",
                    "一",
                    "二",
                    "三",
                    "四",
                    "五",
                    "六",
                )
        }

    Row(
        modifier =
            Modifier.fillMaxWidth(),
    ) {

        labels.forEach { label ->

            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .height(
                            28.dp
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
                    44.dp
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
                    32.dp
                ),

            shape =
                CircleShape,

            color =
                if (
                    selected
                ) {
                    MaterialTheme
                        .colorScheme
                        .primary
                } else {
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
                        dayNumber.toString(),

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
                        if (
                            selected
                        ) {
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


        if (
            hasNote
        ) {
            Box(
                modifier =
                    Modifier
                        .padding(
                            top = 2.dp
                        )
                        .size(
                            4.dp
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
                        6.dp
                    )
            )
        }
    }
}