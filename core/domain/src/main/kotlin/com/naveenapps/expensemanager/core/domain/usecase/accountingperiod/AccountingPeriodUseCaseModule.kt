package com.naveenapps.expensemanager.core.domain.usecase.accountingperiod

import org.koin.dsl.module

val AccountingPeriodUseCaseModule = module {
    single { AccountingPeriodResolver() }
    single { ObserveAccountingPeriodUseCase(repository = get(), resolver = get()) }
}
