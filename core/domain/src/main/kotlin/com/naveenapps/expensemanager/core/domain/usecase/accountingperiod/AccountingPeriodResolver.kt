package com.naveenapps.expensemanager.core.domain.usecase.accountingperiod

import com.naveenapps.expensemanager.core.model.AccountingPeriod
import com.naveenapps.expensemanager.core.model.AccountingPeriodMode
import com.naveenapps.expensemanager.core.model.AccountingPeriodSettings
import com.naveenapps.expensemanager.core.model.SalaryCycle
import java.time.LocalDate
import java.time.YearMonth

/**
 * Resolves one local calendar date to exactly one accounting period.
 *
 * This class is deliberately stateless and does not know about Room, Compose, or timestamps. All
 * consumers therefore share the same month-end and salary-cycle boundary rules.
 */
class AccountingPeriodResolver {

    fun current(
        settings: AccountingPeriodSettings,
        salaryCycles: List<SalaryCycle>,
        today: LocalDate = LocalDate.now(),
    ): AccountingPeriod = resolve(today, settings, salaryCycles)

    fun resolve(
        date: LocalDate,
        settings: AccountingPeriodSettings,
        salaryCycles: List<SalaryCycle>,
    ): AccountingPeriod = when (settings.mode) {
        AccountingPeriodMode.CALENDAR_MONTH -> calendarMonth(date)
        AccountingPeriodMode.FIXED_START_DAY -> fixedStartDay(date, settings.fixedStartDay)
        AccountingPeriodMode.SALARY_CYCLE -> salaryCycle(date, salaryCycles)
    }

    private fun calendarMonth(date: LocalDate): AccountingPeriod {
        val start = date.withDayOfMonth(1)
        return AccountingPeriod(
            startDate = start,
            endExclusiveDate = start.plusMonths(1),
            mode = AccountingPeriodMode.CALENDAR_MONTH,
        )
    }

    private fun fixedStartDay(date: LocalDate, requestedDay: Int): AccountingPeriod {
        val startDay = requestedDay.coerceIn(1, 31)
        val monthStart = startOfMonthClamped(YearMonth.from(date), startDay)
        val start = if (date.isBefore(monthStart)) {
            startOfMonthClamped(YearMonth.from(date).minusMonths(1), startDay)
        } else {
            monthStart
        }
        val nextStart = startOfMonthClamped(YearMonth.from(start).plusMonths(1), startDay)
        return AccountingPeriod(
            startDate = start,
            endExclusiveDate = nextStart,
            mode = AccountingPeriodMode.FIXED_START_DAY,
        )
    }

    private fun salaryCycle(date: LocalDate, salaryCycles: List<SalaryCycle>): AccountingPeriod {
        val ordered = salaryCycles.sortedBy { it.startDate }
        val current = ordered.lastOrNull { !it.startDate.isAfter(date) }
            ?: return calendarMonth(date).copy(
                mode = AccountingPeriodMode.SALARY_CYCLE,
                hasDefinedStart = false,
            )
        val nextStart = ordered.firstOrNull { it.startDate.isAfter(current.startDate) }?.startDate
        return AccountingPeriod(
            startDate = current.startDate,
            endExclusiveDate = nextStart ?: date.plusDays(1),
            mode = AccountingPeriodMode.SALARY_CYCLE,
            isOpenEnded = nextStart == null,
        )
    }

    private fun startOfMonthClamped(yearMonth: YearMonth, requestedDay: Int): LocalDate =
        yearMonth.atDay(requestedDay.coerceAtMost(yearMonth.lengthOfMonth()))
}
