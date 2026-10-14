package com.naveenapps.expensemanager.core.domain.usecase.ledger

import org.koin.dsl.module

val LedgerUseCaseModule = module {
    single { ObserveLedgersUseCase(get()) }
    single { ObserveCurrentLedgerUseCase(get()) }
    single { CreateLedgerUseCase(get()) }
    single { RenameLedgerUseCase(get()) }
    single { SwitchLedgerUseCase(get()) }
    single { ArchiveLedgerUseCase(get()) }
}
