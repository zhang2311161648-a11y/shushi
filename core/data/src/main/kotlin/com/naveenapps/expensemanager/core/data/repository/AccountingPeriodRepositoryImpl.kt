package com.naveenapps.expensemanager.core.data.repository

import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.data.mappers.toDomain
import com.naveenapps.expensemanager.core.data.mappers.toEntity
import com.naveenapps.expensemanager.core.database.dao.SalaryCycleDao
import com.naveenapps.expensemanager.core.datastore.SettingsDataStore
import com.naveenapps.expensemanager.core.model.AccountingPeriodSettings
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.SalaryCycle
import com.naveenapps.expensemanager.core.repository.AccountingPeriodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AccountingPeriodRepositoryImpl(
    private val settingsDataStore: SettingsDataStore,
    private val salaryCycleDao: SalaryCycleDao,
    private val dispatchers: AppCoroutineDispatchers,
) : AccountingPeriodRepository {

    override fun observeSettings(): Flow<AccountingPeriodSettings> =
        settingsDataStore.getAccountingPeriodSettings()

    override suspend fun saveSettings(settings: AccountingPeriodSettings): Resource<Boolean> =
        withContext(dispatchers.io) {
            settingsDataStore.setAccountingPeriodSettings(
                settings.copy(fixedStartDay = settings.fixedStartDay.coerceIn(1, 31)),
            )
            Resource.Success(true)
        }

    override fun observeSalaryCycles(): Flow<List<SalaryCycle>> =
        salaryCycleDao.observeAll().map { entities -> entities.mapNotNull { it.toDomain() } }

    override suspend fun addSalaryCycle(cycle: SalaryCycle): Resource<Boolean> =
        withContext(dispatchers.io) {
            val id = salaryCycleDao.insert(cycle.toEntity())
            if (id == -1L) {
                Resource.Error(IllegalArgumentException("这一天已经是一个周期开始日。"))
            } else {
                Resource.Success(true)
            }
        }

    override suspend fun deleteSalaryCycle(cycle: SalaryCycle): Resource<Boolean> =
        withContext(dispatchers.io) {
            if (salaryCycleDao.deleteById(cycle.id) > 0) {
                Resource.Success(true)
            } else {
                Resource.Error(IllegalStateException("周期不存在。"))
            }
        }
}
