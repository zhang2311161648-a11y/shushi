package com.naveenapps.expensemanager.core.model

import java.time.LocalDate

/** The user's preferred boundary for interpreting a group of transactions. */
enum class AccountingPeriodMode {
    CALENDAR_MONTH,
    FIXED_START_DAY,
    SALARY_CYCLE,
}

data class AccountingPeriodSettings(
    val mode: AccountingPeriodMode = AccountingPeriodMode.CALENDAR_MONTH,
    val fixedStartDay: Int = 1,
)

data class SalaryCycle(
    val id: Long = 0L,
    val startDate: LocalDate,
    val note: String? = null,
    val createdAt: Long,
)

/**
 * A half-open local-date range: [startDate, endExclusiveDate).
 *
 * Keeping the end exclusive makes it safe to pass the range to existing timestamp queries and
 * guarantees that the whole final calendar day is included.
 */
data class AccountingPeriod(
    val startDate: LocalDate,
    val endExclusiveDate: LocalDate,
    val mode: AccountingPeriodMode,
    val isOpenEnded: Boolean = false,
    val hasDefinedStart: Boolean = true,
) {
    val endDate: LocalDate
        get() = endExclusiveDate.minusDays(1)

    fun contains(date: LocalDate): Boolean =
        !date.isBefore(startDate) && date.isBefore(endExclusiveDate)
}
