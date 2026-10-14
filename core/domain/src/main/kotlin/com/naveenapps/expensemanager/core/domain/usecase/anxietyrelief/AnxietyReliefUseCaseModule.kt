package com.naveenapps.expensemanager.core.domain.usecase.anxietyrelief

import org.koin.dsl.module

val AnxietyReliefUseCaseModule = module {
    single { GetAnxietyReliefItemsUseCase(get()) }
    single { AddAnxietyReliefItemUseCase(get()) }
    single { UpdateAnxietyReliefItemUseCase(get()) }
    single { ArchiveAnxietyReliefItemUseCase(get()) }
    single { DeleteAnxietyReliefItemUseCase(get()) }
    single { GetAnxietyReliefDefaultTargetUseCase(get()) }
    single { SetAnxietyReliefDefaultTargetUseCase(get()) }
}
