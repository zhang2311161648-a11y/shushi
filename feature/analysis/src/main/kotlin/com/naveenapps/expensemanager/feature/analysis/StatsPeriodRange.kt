package com.naveenapps.expensemanager.feature.analysis

import com.naveenapps.expensemanager.core.model.AccountingPeriod
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.Date

internal data class StatsSelection(
    val period: StatsPeriod,
    val month: YearMonth,
    val weekStart: LocalDate,
    val year: Int,
)

internal fun currentCalendarWeekStart(today: LocalDate = LocalDate.now()): LocalDate =
    today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

internal fun StatsPeriod.dateRange(
    accountingPeriod: AccountingPeriod?,
    selectedMonth: YearMonth,
    selectedWeekStart: LocalDate,
    selectedYear: Int,
): List<Long> = when (this) {
    StatsPeriod.WEEK -> selectedWeekStart.toWeekRange()
    StatsPeriod.MONTH -> selectedMonth.toDateRange()
    StatsPeriod.YEAR -> selectedYear.toDateRange()
    StatsPeriod.CURRENT_PERIOD -> accountingPeriod?.let {
        listOf(
            it.startDate.toEpochMillisAtStartOfDay(),
            it.endExclusiveDate.toEpochMillisAtStartOfDay(),
        )
    } ?: YearMonth.now().toDateRange()
}

internal fun YearMonth.toDateRange(): List<Long> = listOf(
    atDay(1).toEpochMillisAtStartOfDay(),
    plusMonths(1).atDay(1).toEpochMillisAtStartOfDay(),
)

internal fun LocalDate.toWeekRange(): List<Long> = listOf(
    toEpochMillisAtStartOfDay(),
    plusWeeks(1).toEpochMillisAtStartOfDay(),
)

internal fun LocalDate.toDateAtStartOfDay(): Date =
    Date(toEpochMillisAtStartOfDay())

internal fun Date.toLocalDateAtStartOfDay(): LocalDate =
    toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

internal fun Int.toDateRange(): List<Long> {
    val year = Year.of(this)
    return listOf(
        year.atDay(1).toEpochMillisAtStartOfDay(),
        year.plusYears(1).atDay(1).toEpochMillisAtStartOfDay(),
    )
}

internal fun List<Long>.toCalendarMonthOrNull(): YearMonth? {
    val dates = toLocalDatesOrNull() ?: return null
    val start = dates.first
    val end = dates.second
    return if (start.dayOfMonth == 1 && end == start.plusMonths(1)) {
        YearMonth.of(start.year, start.monthValue)
    } else {
        null
    }
}

internal fun List<Long>.toCalendarWeekStartOrNull(): LocalDate? {
    val dates = toLocalDatesOrNull() ?: return null
    val start = dates.first
    return if (
        start.dayOfWeek == DayOfWeek.MONDAY &&
        dates.second == start.plusWeeks(1)
    ) {
        start
    } else {
        null
    }
}

internal fun List<Long>.toCalendarYearOrNull(): Int? {
    val dates = toLocalDatesOrNull() ?: return null
    val start = dates.first
    return if (start.dayOfYear == 1 && dates.second == start.plusYears(1)) {
        start.year
    } else {
        null
    }
}

private fun List<Long>.toLocalDatesOrNull(): Pair<LocalDate, LocalDate>? {
    if (size < 2) return null
    val zone = ZoneId.systemDefault()
    return Date(this[0]).toInstant().atZone(zone).toLocalDate() to
        Date(this[1]).toInstant().atZone(zone).toLocalDate()
}

internal fun LocalDate.toEpochMillisAtStartOfDay(): Long =
    atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
