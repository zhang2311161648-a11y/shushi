package com.naveenapps.expensemanager.core.data.repository

import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.data.mappers.toDomainModel
import com.naveenapps.expensemanager.core.data.mappers.toEntityModel
import com.naveenapps.expensemanager.core.database.dao.MonthlySavingDao
import com.naveenapps.expensemanager.core.model.MonthlySaving
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.MonthlySavingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class MonthlySavingRepositoryImpl(
    private val monthlySavingDao: MonthlySavingDao,
    private val dispatchers: AppCoroutineDispatchers,
) : MonthlySavingRepository {

    override suspend fun getSaving(year: Int, month: Int): MonthlySaving? = withContext(dispatchers.io) {
        monthlySavingDao.getSaving(year, month)?.toDomainModel()
    }

    override fun observeSaving(year: Int, month: Int): Flow<MonthlySaving?> =
        monthlySavingDao.observeSaving(year, month).map { it?.toDomainModel() }

    override suspend fun getSaving(periodKey: String): MonthlySaving? = withContext(dispatchers.io) {
        monthlySavingDao.getSavingByPeriodKey(periodKey)?.toDomainModel()
    }

    override fun observeSaving(periodKey: String): Flow<MonthlySaving?> =
        monthlySavingDao.observeSavingByPeriodKey(periodKey).map { it?.toDomainModel() }

    override fun observeSavingsByYear(year: Int): Flow<List<MonthlySaving>> =
        monthlySavingDao.observeSavingsByYear(year).map { savings -> savings.map { it.toDomainModel() } }

    override fun getYearTotal(year: Int): Flow<Long> = monthlySavingDao.getYearTotal(year)

    override suspend fun upsertMonthlySaving(saving: MonthlySaving): Resource<Boolean> =
        withContext(dispatchers.io) {
            runCatching {
                val periodKey = saving.periodKey
                val existing = if (periodKey != null) {
                    monthlySavingDao.getSavingByPeriodKey(periodKey)
                } else {
                    monthlySavingDao.getSaving(saving.year, saving.month)
                }
                val entity = saving.toEntityModel(
                    idOverride = existing?.id ?: saving.id,
                    createdAtOverride = existing?.createdAt ?: saving.createdAt,
                )
                Resource.Success(monthlySavingDao.upsertMonthlySaving(entity) > 0L)
            }.getOrElse { Resource.Error(it as? Exception ?: Exception(it)) }
        }

    override suspend fun deleteMonthlySaving(saving: MonthlySaving): Resource<Boolean> =
        withContext(dispatchers.io) {
            runCatching {
                Resource.Success(monthlySavingDao.deleteMonthlySaving(saving.id) > 0)
            }.getOrElse { Resource.Error(it as? Exception ?: Exception(it)) }
        }
}
