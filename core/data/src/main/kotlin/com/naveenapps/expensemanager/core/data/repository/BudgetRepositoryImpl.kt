package com.naveenapps.expensemanager.core.data.repository

import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.data.mappers.toDomainModel
import com.naveenapps.expensemanager.core.data.mappers.toEntityModel
import com.naveenapps.expensemanager.core.database.dao.BudgetDao
import com.naveenapps.expensemanager.core.database.entity.BudgetEntity
import com.naveenapps.expensemanager.core.model.Budget
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.DEFAULT_LEDGER_ID
import com.naveenapps.expensemanager.core.repository.CurrentLedgerRepository
import com.naveenapps.expensemanager.core.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class BudgetRepositoryImpl(
    private val budgetDao: BudgetDao,
    private val dispatchers: AppCoroutineDispatchers,
    private val currentLedgerRepository: CurrentLedgerRepository? = null,
) : BudgetRepository {

    override fun getBudgets(): Flow<List<Budget>> {
        val budgets = currentLedgerRepository?.observeCurrentLedgerId()?.flatMapLatest { ledgerId ->
            budgetDao.getBudgets(ledgerId)
        } ?: budgetDao.getBudgets()
        return budgets.map { budgets ->
            return@map budgets?.map { budget ->
                val accounts = budgetDao.getBudgetAccounts(budget.id, budget.ledgerId)
                val categories = budgetDao.getBudgetCategories(budget.id, budget.ledgerId)
                budget.toDomainModel(
                    accounts = accounts?.map { it.accountId } ?: emptyList(),
                    categories = categories?.map { it.categoryId } ?: emptyList(),
                )
            } ?: emptyList()
        }
    }

    override fun findBudgetByIdFlow(budgetId: String): Flow<Budget?> {
        val budget = currentLedgerRepository?.observeCurrentLedgerId()?.flatMapLatest { ledgerId ->
            budgetDao.findByIdFlow(budgetId, ledgerId)
        } ?: budgetDao.findByIdFlow(budgetId)
        return budget.map {
            it?.let {
                convertBudgetModel(it)
            }
        }
    }

    override suspend fun findBudgetById(budgetId: String): Resource<Budget> =
        withContext(dispatchers.io) {
            return@withContext try {
                val ledgerId = currentLedgerRepository?.getCurrentLedgerId() ?: DEFAULT_LEDGER_ID
                val budget = budgetDao.findById(budgetId, ledgerId)
                if (budget != null) {
                    Resource.Success(convertBudgetModel(budget))
                } else {
                    Resource.Error(KotlinNullPointerException())
                }
            } catch (e: Exception) {
                Resource.Error(e)
            }
        }

    private suspend fun convertBudgetModel(budget: BudgetEntity): Budget {
        val accounts = budgetDao.getBudgetAccounts(budget.id, budget.ledgerId)
        val categories = budgetDao.getBudgetCategories(budget.id, budget.ledgerId)
        return budget.toDomainModel(
            accounts = accounts?.map { it.accountId } ?: emptyList(),
            categories = categories?.map { it.categoryId } ?: emptyList(),
        )
    }

    override suspend fun addBudget(budget: Budget): Resource<Boolean> =
        withContext(dispatchers.io) {
            return@withContext try {
                val response = budgetDao.insertBudget(
                    budget.toEntityModel().copy(
                        ledgerId = currentLedgerRepository?.getCurrentLedgerId() ?: DEFAULT_LEDGER_ID,
                    ),
                    budget.categories,
                    budget.accounts,
                )
                Resource.Success(response != -1L)
            } catch (exception: Exception) {
                Resource.Error(exception)
            }
        }

    override suspend fun updateBudget(budget: Budget): Resource<Boolean> =
        withContext(dispatchers.io) {
            return@withContext try {
                budgetDao.updateBudget(
                    budget.toEntityModel().copy(
                        ledgerId = currentLedgerRepository?.getCurrentLedgerId() ?: DEFAULT_LEDGER_ID,
                    ),
                    budget.categories,
                    budget.accounts,
                )
                Resource.Success(true)
            } catch (exception: Exception) {
                Resource.Error(exception)
            }
        }

    override suspend fun deleteBudget(budget: Budget): Resource<Boolean> =
        withContext(dispatchers.io) {
            return@withContext try {
                val response = budgetDao.delete(
                    budget.toEntityModel().copy(
                        ledgerId = currentLedgerRepository?.getCurrentLedgerId() ?: DEFAULT_LEDGER_ID,
                    ),
                )
                Resource.Success(response > 0)
            } catch (exception: Exception) {
                Resource.Error(exception)
            }
        }
}
