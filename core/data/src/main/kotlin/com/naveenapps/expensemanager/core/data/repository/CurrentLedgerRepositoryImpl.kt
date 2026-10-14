package com.naveenapps.expensemanager.core.data.repository

import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.datastore.CurrentLedgerDataStore
import com.naveenapps.expensemanager.core.repository.CurrentLedgerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class CurrentLedgerRepositoryImpl(
    private val dataStore: CurrentLedgerDataStore,
    private val dispatchers: AppCoroutineDispatchers,
) : CurrentLedgerRepository {

    override fun observeCurrentLedgerId(): Flow<String> = dataStore.getCurrentLedgerId()

    override suspend fun getCurrentLedgerId(): String = withContext(dispatchers.io) {
        dataStore.getCurrentLedgerId().first()
    }

    override suspend fun setCurrentLedgerId(ledgerId: String): Boolean = withContext(dispatchers.io) {
        dataStore.setCurrentLedgerId(ledgerId)
        true
    }
}
