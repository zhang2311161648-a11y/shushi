package com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange

import com.naveenapps.expensemanager.core.model.DateRangeModel
import com.naveenapps.expensemanager.core.domain.usecase.accountingperiod.ObserveAccountingPeriodUseCase
import com.naveenapps.expensemanager.core.model.AccountingPeriod
import com.naveenapps.expensemanager.core.model.AccountingPeriodMode
import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.repository.DateRangeFilterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate
import java.time.ZoneId

class GetDateRangeUseCase(
    private val getDateRangeByTypeUseCase: GetDateRangeByTypeUseCase,
    private val dateRangeFilterRepository: DateRangeFilterRepository,
    private val observeAccountingPeriodUseCase: ObserveAccountingPeriodUseCase? = null,
) {

    operator fun invoke(): Flow<DateRangeModel> {
        val accountingPeriod = observeAccountingPeriodUseCase?.invoke() ?: flowOf(null)
        return combine(
            dateRangeFilterRepository.getDateRangeFilterType(),
            dateRangeFilterRepository.getDateRangeTimeFrame(),
            accountingPeriod,
        ) { type, _, period ->
            val dateRange = getDateRangeByTypeUseCase.invoke(type)
            if (type == DateRangeType.THIS_MONTH && period != null && period.shouldReplaceCalendarMonth()) {
                dateRange.copy(
                    dateRanges = listOf(
                        period.startDate.toEpochMillisAtStartOfDay(),
                        period.endExclusiveDate.toEpochMillisAtStartOfDay() - 1L,
                    ),
                    description = period.asShortDateRange(),
                )
            } else {
                dateRange
            }
        }
    }
}

private fun AccountingPeriod.shouldReplaceCalendarMonth(): Boolean =
    mode != AccountingPeriodMode.CALENDAR_MONTH && hasDefinedStart

private fun AccountingPeriod.asShortDateRange(): String =
    "%02d.%02d - %02d.%02d".format(
        startDate.monthValue,
        startDate.dayOfMonth,
        endDate.monthValue,
        endDate.dayOfMonth,
    )

private fun LocalDate.toEpochMillisAtStartOfDay(): Long =
    atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
