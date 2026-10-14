package com.naveenapps.expensemanager.feature.analysis

import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import java.time.YearMonth
import org.junit.Test

class StatsPeriodRangeTest {

    @Test
    fun `month navigation creates exact calendar month range`() {
        val range = YearMonth.of(2026, 2).toDateRange()

        assertThat(range.toCalendarMonthOrNull()).isEqualTo(YearMonth.of(2026, 2))
        assertThat(range.toCalendarWeekStartOrNull()).isNull()
        assertThat(range.toCalendarYearOrNull()).isNull()
    }

    @Test
    fun `week navigation creates seven day Monday based range`() {
        val weekStart = LocalDate.of(2026, 9, 7)
        val range = weekStart.toWeekRange()

        assertThat(range.toCalendarWeekStartOrNull()).isEqualTo(weekStart)
        assertThat(range.toCalendarMonthOrNull()).isNull()
    }

    @Test
    fun `year navigation creates exact calendar year range`() {
        val range = 2025.toDateRange()

        assertThat(range.toCalendarYearOrNull()).isEqualTo(2025)
        assertThat(range.toCalendarMonthOrNull()).isNull()
        assertThat(range.toCalendarWeekStartOrNull()).isNull()
    }

    @Test
    fun `week start is normalized to Monday`() {
        assertThat(currentCalendarWeekStart(LocalDate.of(2026, 9, 9)))
            .isEqualTo(LocalDate.of(2026, 9, 7))
        assertThat(currentCalendarWeekStart(LocalDate.of(2026, 9, 7)))
            .isEqualTo(LocalDate.of(2026, 9, 7))
    }
}
