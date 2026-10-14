package com.naveenapps.expensemanager.core.domain.usecase.accountingperiod

import com.naveenapps.expensemanager.core.model.AccountingPeriod
import com.naveenapps.expensemanager.core.repository.AccountingPeriodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class ObserveAccountingPeriodUseCase(
    private val repository: AccountingPeriodRepository,
    private val resolver: AccountingPeriodResolver,
) {

    operator fun invoke(): Flow<AccountingPeriod> = combine(
        repository.observeSettings(),
        repository.observeSalaryCycles(),
    ) { settings, salaryCycles ->
        resolver.current(settings, salaryCycles)
    }

    fun forDate(date: LocalDate): Flow<AccountingPeriod> = combine(
        repository.observeSettings(),
        repository.observeSalaryCycles(),
    ) { settings, salaryCycles ->
        resolver.resolve(date, settings, salaryCycles)
    }
}
