package com.naveenapps.expensemanager.core.domain.usecase.accountingperiod

import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.model.AccountingPeriodMode
import com.naveenapps.expensemanager.core.model.AccountingPeriodSettings
import com.naveenapps.expensemanager.core.model.SalaryCycle
import org.junit.Test
import java.time.LocalDate

class AccountingPeriodResolverTest {

    private val resolver = AccountingPeriodResolver()
    private val calendarSettings = AccountingPeriodSettings()

    @Test
    fun calendarMonthUsesFirstDayAndNextMonthAsExclusiveEnd() {
        val period = resolver.resolve(LocalDate.of(2026, 8, 15), calendarSettings, emptyList())

        assertThat(period.startDate).isEqualTo(LocalDate.of(2026, 8, 1))
        assertThat(period.endDate).isEqualTo(LocalDate.of(2026, 8, 31))
    }

    @Test
    fun fixedStartDayUsesPreviousMonthBeforeBoundary() {
        val settings = AccountingPeriodSettings(AccountingPeriodMode.FIXED_START_DAY, fixedStartDay = 25)

        val period = resolver.resolve(LocalDate.of(2026, 9, 7), settings, emptyList())

        assertThat(period.startDate).isEqualTo(LocalDate.of(2026, 8, 25))
        assertThat(period.endDate).isEqualTo(LocalDate.of(2026, 9, 24))
    }

    @Test
    fun fixedStartDayUsesCurrentMonthOnBoundary() {
        val settings = AccountingPeriodSettings(AccountingPeriodMode.FIXED_START_DAY, fixedStartDay = 25)

        val period = resolver.resolve(LocalDate.of(2026, 9, 25), settings, emptyList())

        assertThat(period.startDate).isEqualTo(LocalDate.of(2026, 9, 25))
        assertThat(period.endDate).isEqualTo(LocalDate.of(2026, 10, 24))
    }

    @Test
    fun fixedDayThirtyOneClampsFebruaryWithoutCrashing() {
        val settings = AccountingPeriodSettings(AccountingPeriodMode.FIXED_START_DAY, fixedStartDay = 31)

        val period = resolver.resolve(LocalDate.of(2026, 3, 7), settings, emptyList())

        assertThat(period.startDate).isEqualTo(LocalDate.of(2026, 2, 28))
        assertThat(period.endDate).isEqualTo(LocalDate.of(2026, 3, 30))
    }

    @Test
    fun salaryCycleEndsTheDayBeforeNextSalaryCycle() {
        val cycles = listOf(
            salaryCycle("2026-08-25"),
            salaryCycle("2026-09-27"),
        )

        val period = resolver.resolve(
            LocalDate.of(2026, 9, 10),
            AccountingPeriodSettings(AccountingPeriodMode.SALARY_CYCLE),
            cycles,
        )

        assertThat(period.startDate).isEqualTo(LocalDate.of(2026, 8, 25))
        assertThat(period.endDate).isEqualTo(LocalDate.of(2026, 9, 26))
        assertThat(period.isOpenEnded).isFalse()
    }

    @Test
    fun latestSalaryCycleIsOpenEndedThroughToday() {
        val period = resolver.resolve(
            LocalDate.of(2026, 9, 30),
            AccountingPeriodSettings(AccountingPeriodMode.SALARY_CYCLE),
            listOf(salaryCycle("2026-08-25"), salaryCycle("2026-09-27")),
        )

        assertThat(period.startDate).isEqualTo(LocalDate.of(2026, 9, 27))
        assertThat(period.endDate).isEqualTo(LocalDate.of(2026, 9, 30))
        assertThat(period.isOpenEnded).isTrue()
    }

    @Test
    fun salaryModeWithoutAnchorFallsBackSafely() {
        val period = resolver.resolve(
            LocalDate.of(2026, 9, 30),
            AccountingPeriodSettings(AccountingPeriodMode.SALARY_CYCLE),
            emptyList(),
        )

        assertThat(period.startDate).isEqualTo(LocalDate.of(2026, 9, 1))
        assertThat(period.endDate).isEqualTo(LocalDate.of(2026, 9, 30))
        assertThat(period.hasDefinedStart).isFalse()
    }

    private fun salaryCycle(date: String) = SalaryCycle(
        startDate = LocalDate.parse(date),
        createdAt = 1L,
    )
}
