package com.naveenapps.expensemanager.core.domain.usecase.ledger

import com.naveenapps.expensemanager.core.model.Ledger
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.LedgerRepository
import kotlinx.coroutines.flow.Flow

class ObserveLedgersUseCase(private val repository: LedgerRepository) {
    operator fun invoke(): Flow<List<Ledger>> = repository.observeActiveLedgers()
}

class ObserveCurrentLedgerUseCase(private val repository: LedgerRepository) {
    operator fun invoke(): Flow<Ledger?> = repository.observeCurrentLedger()
}

class CreateLedgerUseCase(private val repository: LedgerRepository) {
    suspend operator fun invoke(name: String): Resource<Ledger> = repository.createLedger(name)
}

class RenameLedgerUseCase(private val repository: LedgerRepository) {
    suspend operator fun invoke(ledgerId: String, name: String): Resource<Boolean> =
        repository.renameLedger(ledgerId, name)
}

class SwitchLedgerUseCase(private val repository: LedgerRepository) {
    suspend operator fun invoke(ledgerId: String): Resource<Boolean> = repository.switchLedger(ledgerId)
}

class ArchiveLedgerUseCase(private val repository: LedgerRepository) {
    suspend operator fun invoke(ledgerId: String): Resource<Boolean> = repository.archiveLedger(ledgerId)
}
