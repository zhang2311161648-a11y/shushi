package com.naveenapps.expensemanager.core.data.repository

import androidx.room.withTransaction
import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.database.ExpenseManagerDatabase
import com.naveenapps.expensemanager.core.database.dao.AccountDao
import com.naveenapps.expensemanager.core.database.dao.CategoryDao
import com.naveenapps.expensemanager.core.database.dao.LedgerDao
import com.naveenapps.expensemanager.core.database.entity.AccountEntity
import com.naveenapps.expensemanager.core.database.entity.CategoryEntity
import com.naveenapps.expensemanager.core.database.entity.LedgerEntity
import com.naveenapps.expensemanager.core.model.DEFAULT_LEDGER_ID
import com.naveenapps.expensemanager.core.model.Ledger
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.CurrentLedgerRepository
import com.naveenapps.expensemanager.core.repository.LedgerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.UUID

class LedgerRepositoryImpl(
    private val ledgerDao: LedgerDao,
    private val categoryDao: CategoryDao,
    private val accountDao: AccountDao,
    private val database: ExpenseManagerDatabase,
    private val currentLedgerRepository: CurrentLedgerRepository,
    private val dispatchers: AppCoroutineDispatchers,
) : LedgerRepository {

    override fun observeActiveLedgers(): Flow<List<Ledger>> = ledgerDao.observeActive().map { ledgers ->
        ledgers.map { it.toDomainModel() }
    }

    override fun observeCurrentLedger(): Flow<Ledger?> = currentLedgerRepository
        .observeCurrentLedgerId()
        .flatMapLatest { ledgerDao.observeActiveById(it) }
        .map { it?.toDomainModel() }

    override suspend fun createLedger(name: String): Resource<Ledger> = withContext(dispatchers.io) {
        val normalizedName = name.trim()
        if (normalizedName.isBlank()) {
            return@withContext Resource.Error(IllegalArgumentException("账本名称不能为空"))
        }

        return@withContext try {
            val sourceLedgerId = currentLedgerRepository.getCurrentLedgerId()
            val now = Date()
            val newLedger = LedgerEntity(
                id = UUID.randomUUID().toString(),
                name = normalizedName,
                createdOn = now,
                updatedOn = now,
            )
            database.withTransaction {
                ledgerDao.insert(newLedger)
                categoryDao.getAllValues(sourceLedgerId).orEmpty().forEach { category ->
                    categoryDao.insert(category.copy(
                        id = UUID.randomUUID().toString(),
                        ledgerId = newLedger.id,
                        createdOn = now,
                        updatedOn = now,
                    ))
                }
                accountDao.getAllValues(sourceLedgerId).forEach { account ->
                    accountDao.insert(account.copy(
                        id = UUID.randomUUID().toString(),
                        ledgerId = newLedger.id,
                        amount = 0.0,
                        initialBalanceCents = 0L,
                        currentBalanceCents = 0L,
                        createdOn = now,
                        updatedOn = now,
                    ))
                }
            }
            Resource.Success(newLedger.toDomainModel())
        } catch (exception: Exception) {
            Resource.Error(exception)
        }
    }

    override suspend fun renameLedger(ledgerId: String, name: String): Resource<Boolean> = withContext(dispatchers.io) {
        val normalizedName = name.trim()
        if (normalizedName.isBlank()) {
            return@withContext Resource.Error(IllegalArgumentException("账本名称不能为空"))
        }
        return@withContext try {
            Resource.Success(ledgerDao.rename(ledgerId, normalizedName, Date()) > 0)
        } catch (exception: Exception) {
            Resource.Error(exception)
        }
    }

    override suspend fun switchLedger(ledgerId: String): Resource<Boolean> = withContext(dispatchers.io) {
        return@withContext try {
            val ledger = ledgerDao.findById(ledgerId)
            if (ledger == null || ledger.isArchived) {
                Resource.Error(IllegalArgumentException("账本不存在或已归档"))
            } else {
                Resource.Success(currentLedgerRepository.setCurrentLedgerId(ledgerId))
            }
        } catch (exception: Exception) {
            Resource.Error(exception)
        }
    }

    override suspend fun archiveLedger(ledgerId: String): Resource<Boolean> = withContext(dispatchers.io) {
        return@withContext try {
            val ledger = ledgerDao.findById(ledgerId)
            if (ledger == null || ledger.isArchived) {
                Resource.Error(IllegalArgumentException("账本不存在或已归档"))
            } else {
                val activeLedgers = ledgerDao.getActive()
                if (activeLedgers.size <= 1) {
                    Resource.Error(IllegalStateException("至少保留一个可用账本"))
                } else {
                    val currentId = currentLedgerRepository.getCurrentLedgerId()
                    if (currentId == ledgerId) {
                        val replacement = activeLedgers.firstOrNull { it.id != ledgerId }
                        if (replacement != null) {
                            currentLedgerRepository.setCurrentLedgerId(replacement.id)
                        }
                    }
                    Resource.Success(ledgerDao.archive(ledgerId, Date()) > 0)
                }
            }
        } catch (exception: Exception) {
            Resource.Error(exception)
        }
    }

    private fun LedgerEntity.toDomainModel() = Ledger(
        id = id,
        name = name,
        createdOn = createdOn,
        updatedOn = updatedOn,
        isArchived = isArchived,
    )
}
