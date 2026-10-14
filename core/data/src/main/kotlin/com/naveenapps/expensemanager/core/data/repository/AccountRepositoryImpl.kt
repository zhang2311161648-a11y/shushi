package com.naveenapps.expensemanager.core.data.repository

import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.data.mappers.toDomainModel
import com.naveenapps.expensemanager.core.data.mappers.toEntityModel
import com.naveenapps.expensemanager.core.database.dao.AccountDao
import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.DEFAULT_LEDGER_ID
import com.naveenapps.expensemanager.core.repository.CurrentLedgerRepository
import com.naveenapps.expensemanager.core.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AccountRepositoryImpl(
    private val accountDao: AccountDao,
    private val dispatchers: AppCoroutineDispatchers,
    private val currentLedgerRepository: CurrentLedgerRepository? = null,
) : AccountRepository {

    override fun getAccounts(): Flow<List<Account>> {
        val accounts = currentLedgerRepository?.observeCurrentLedgerId()?.flatMapLatest { ledgerId ->
            accountDao.getAccounts(ledgerId)
        } ?: accountDao.getAccounts()
        return accounts.map { accounts ->
            return@map accounts?.map { it.toDomainModel() } ?: emptyList()
        }
    }

    override suspend fun findAccount(accountId: String): Resource<Account> =
        withContext(dispatchers.io) {
            return@withContext try {
                val account = currentLedgerRepository?.let {
                    accountDao.findById(accountId, it.getCurrentLedgerId())
                } ?: accountDao.findById(accountId)

                if (account != null) {
                    Resource.Success(account.toDomainModel())
                } else {
                    Resource.Error(KotlinNullPointerException())
                }
            } catch (e: Exception) {
                Resource.Error(e)
            }
        }

    override suspend fun addAccount(account: Account): Resource<Boolean> =
        withContext(dispatchers.io) {
            return@withContext try {
                val ledgerId = currentLedgerRepository?.getCurrentLedgerId() ?: DEFAULT_LEDGER_ID
                val response = accountDao.insert(account.toEntityModel().copy(ledgerId = ledgerId))
                Resource.Success(response != -1L)
            } catch (exception: Exception) {
                Resource.Error(exception)
            }
        }

    override suspend fun updateAccount(account: Account, currentBalanceCents: Long?): Resource<Boolean> =
        withContext(dispatchers.io) {
            return@withContext try {
                val ledgerId = currentLedgerRepository?.getCurrentLedgerId() ?: DEFAULT_LEDGER_ID
                Resource.Success(accountDao.updateAccountAndBalances(
                    account.toEntityModel().copy(ledgerId = ledgerId), currentBalanceCents,
                ))
            } catch (exception: Exception) {
                Resource.Error(exception)
            }
        }

    override suspend fun updateAllAccount(accounts: List<Account>): Resource<Boolean> =
        withContext(dispatchers.io) {
            return@withContext try {
                val ledgerId = currentLedgerRepository?.getCurrentLedgerId() ?: DEFAULT_LEDGER_ID
                accountDao.reorderAccounts(accounts.map { it.toEntityModel().copy(ledgerId = ledgerId) })
                Resource.Success(true)
            } catch (exception: Exception) {
                Resource.Error(exception)
            }
        }

    override suspend fun deleteAccount(account: Account): Resource<Boolean> =
        withContext(dispatchers.io) {
            return@withContext try {
                val ledgerId = currentLedgerRepository?.getCurrentLedgerId() ?: DEFAULT_LEDGER_ID
                val response = accountDao.delete(account.toEntityModel().copy(ledgerId = ledgerId))
                Resource.Success(response > 0)
            } catch (exception: Exception) {
                Resource.Error(exception)
            }
        }
}
