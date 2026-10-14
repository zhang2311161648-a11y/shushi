package com.naveenapps.expensemanager.core.domain.usecase.stats

import org.koin.dsl.module

val StatsRankingUseCaseModule = module {
    single {
        GetStatsCategoryRankingUseCase(
            repository = get(),
            getCurrencyUseCase = get(),
            getFormattedAmountUseCase = get(),
            dispatchers = get(),
        )
    }
    single {
        GetStatsCategoryTransactionRankingUseCase(
            repository = get(),
            getCurrencyUseCase = get(),
            getFormattedAmountUseCase = get(),
            dispatchers = get(),
        )
    }
}
