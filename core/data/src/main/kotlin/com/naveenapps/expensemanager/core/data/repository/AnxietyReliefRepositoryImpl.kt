package com.naveenapps.expensemanager.core.data.repository

import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.data.mappers.toDomainModel
import com.naveenapps.expensemanager.core.data.mappers.toEntityModel
import com.naveenapps.expensemanager.core.database.dao.AnxietyReliefItemDao
import com.naveenapps.expensemanager.core.datastore.AnxietyReliefDataStore
import com.naveenapps.expensemanager.core.model.AnxietyReliefItem
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.AnxietyReliefRepository
import com.naveenapps.expensemanager.core.repository.AnxietyReliefSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AnxietyReliefRepositoryImpl(
    private val itemDao: AnxietyReliefItemDao,
    private val dispatchers: AppCoroutineDispatchers,
) : AnxietyReliefRepository {
    override fun getItems(): Flow<List<AnxietyReliefItem>> =
        itemDao.getAllItems().map { items -> items.map { it.toDomainModel() } }

    override suspend fun add(item: AnxietyReliefItem): Resource<Boolean> = withContext(dispatchers.io) {
        runCatching { Resource.Success(itemDao.insert(item.toEntityModel()) != -1L) }
            .getOrElse { Resource.Error(it.asException()) }
    }

    override suspend fun update(item: AnxietyReliefItem): Resource<Boolean> = withContext(dispatchers.io) {
        runCatching {
            itemDao.update(item.toEntityModel())
            Resource.Success(true)
        }.getOrElse { Resource.Error(it.asException()) }
    }

    override suspend fun archive(item: AnxietyReliefItem): Resource<Boolean> = withContext(dispatchers.io) {
        runCatching {
            itemDao.update(item.copy(isArchived = true).toEntityModel())
            Resource.Success(true)
        }.getOrElse { Resource.Error(it.asException()) }
    }

    override suspend fun delete(item: AnxietyReliefItem): Resource<Boolean> = withContext(dispatchers.io) {
        runCatching { Resource.Success(itemDao.delete(item.toEntityModel()) > 0) }
            .getOrElse { Resource.Error(it.asException()) }
    }
}

class AnxietyReliefSettingsRepositoryImpl(
    private val dataStore: AnxietyReliefDataStore,
) : AnxietyReliefSettingsRepository {
    override fun getDefaultDailyTargetCents(): Flow<Long> = dataStore.defaultDailyTargetCents

    override suspend fun setDefaultDailyTargetCents(value: Long): Resource<Boolean> =
        runCatching {
            dataStore.setDefaultDailyTargetCents(value)
            Resource.Success(true)
        }.getOrElse { Resource.Error(it.asException()) }
}

private fun Throwable.asException(): Exception = this as? Exception ?: Exception(this)
