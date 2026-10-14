package com.naveenapps.expensemanager.core.domain.usecase.monthlysaving

import org.koin.dsl.module

val MonthlySavingUseCaseModule = module {
    single { GetMonthlySavingUseCase(get()) }
    single { ObserveMonthlySavingUseCase(get()) }
    single { ObserveMonthlySavingsByYearUseCase(get()) }
    single { GetMonthlySavingsYearTotalUseCase(get()) }
    single { UpsertMonthlySavingUseCase(get()) }
    single { DeleteMonthlySavingUseCase(get()) }
}
