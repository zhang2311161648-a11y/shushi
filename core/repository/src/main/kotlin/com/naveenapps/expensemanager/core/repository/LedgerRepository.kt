package com.naveenapps.expensemanager.core.repository

import com.naveenapps.expensemanager.core.model.Ledger
import com.naveenapps.expensemanager.core.model.Resource
import kotlinx.coroutines.flow.Flow

interface LedgerRepository {
    fun observeActiveLedgers(): Flow<List<Ledger>>

    fun observeCurrentLedger(): Flow<Ledger?>

    suspend fun createLedger(name: String): Resource<Ledger>

    suspend fun renameLedger(ledgerId: String, name: String): Resource<Boolean>

    suspend fun switchLedger(ledgerId: String): Resource<Boolean>

    suspend fun archiveLedger(ledgerId: String): Resource<Boolean>
}
