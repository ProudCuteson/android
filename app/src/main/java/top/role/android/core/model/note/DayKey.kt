package top.role.android.core.model.note

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

object DayKey {

    private val formatter =
        DateTimeFormatter.ISO_LOCAL_DATE

    fun today(): String {
        return format(
            LocalDate.now(),
        )
    }

    fun format(
        date: LocalDate,
    ): String {
        return date.format(
            formatter,
        )
    }

    fun parse(
        dayKey: String,
    ): LocalDate? {
        return runCatching {
            LocalDate.parse(
                dayKey.trim(),
                formatter,
            )
        }.getOrNull()
    }

    fun isValid(
        dayKey: String,
    ): Boolean {
        return parse(dayKey) != null
    }

    fun monthKey(
        dayKey: String,
    ): String {
        val date =
            requireNotNull(
                parse(dayKey),
            ) {
                "Invalid dayKey: $dayKey"
            }

        return YearMonth
            .from(date)
            .toString()
    }

    fun monthRange(
        dayKey: String,
    ): MonthRange {
        val date =
            requireNotNull(
                parse(dayKey),
            ) {
                "Invalid dayKey: $dayKey"
            }

        val month =
            YearMonth.from(date)

        return MonthRange(
            start =
                format(
                    month.atDay(1),
                ),
            endExclusive =
                format(
                    month
                        .plusMonths(1)
                        .atDay(1),
                ),
        )
    }

    data class MonthRange(
        val start: String,
        val endExclusive: String,
    )
}