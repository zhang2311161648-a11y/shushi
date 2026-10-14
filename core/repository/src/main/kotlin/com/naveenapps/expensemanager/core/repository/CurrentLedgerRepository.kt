package com.naveenapps.expensemanager.core.repository

import kotlinx.coroutines.flow.Flow

interface CurrentLedgerRepository {
    fun observeCurrentLedgerId(): Flow<String>

    suspend fun getCurrentLedgerId(): String

    suspend fun setCurrentLedgerId(ledgerId: String): Boolean
}
