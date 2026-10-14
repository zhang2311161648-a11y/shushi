package com.naveenapps.expensemanager.core.repository

import com.naveenapps.expensemanager.core.model.AccountingPeriodSettings
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.SalaryCycle
import kotlinx.coroutines.flow.Flow

interface AccountingPeriodRepository {

    fun observeSettings(): Flow<AccountingPeriodSettings>

    suspend fun saveSettings(settings: AccountingPeriodSettings): Resource<Boolean>

    fun observeSalaryCycles(): Flow<List<SalaryCycle>>

    suspend fun addSalaryCycle(cycle: SalaryCycle): Resource<Boolean>

    suspend fun deleteSalaryCycle(cycle: SalaryCycle): Resource<Boolean>
}
