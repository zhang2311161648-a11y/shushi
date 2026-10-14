package com.naveenapps.expensemanager.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.naveenapps.expensemanager.core.database.entity.BudgetAccountEntity
import com.naveenapps.expensemanager.core.database.entity.BudgetCategoryEntity
import com.naveenapps.expensemanager.core.database.entity.BudgetEntity
import com.naveenapps.expensemanager.core.model.DEFAULT_LEDGER_ID
import kotlinx.coroutines.flow.Flow
import java.util.Date
import java.util.UUID

@Dao
interface BudgetDao : BaseDao<BudgetEntity> {

    @Query("SELECT * FROM budget WHERE ledger_id = :ledgerId ORDER BY created_on DESC")
    fun getBudgets(ledgerId: String): Flow<List<BudgetEntity>?>

    fun getBudgets(): Flow<List<BudgetEntity>?> = getBudgets(DEFAULT_LEDGER_ID)

    @Query("SELECT * FROM budget WHERE id = :id AND ledger_id = :ledgerId")
    fun findByIdFlow(id: String, ledgerId: String): Flow<BudgetEntity?>

    fun findByIdFlow(id: String): Flow<BudgetEntity?> = findByIdFlow(id, DEFAULT_LEDGER_ID)

    @Query("SELECT * FROM budget WHERE id = :id AND ledger_id = :ledgerId")
    suspend fun findById(id: String, ledgerId: String): BudgetEntity?

    suspend fun findById(id: String): BudgetEntity? = findById(id, DEFAULT_LEDGER_ID)

    @Query("SELECT * FROM budget_category_relation WHERE budget_id=:budgetId AND ledger_id = :ledgerId ORDER BY created_on DESC")
    suspend fun getBudgetCategories(budgetId: String, ledgerId: String): List<BudgetCategoryEntity>?

    suspend fun getBudgetCategories(budgetId: String): List<BudgetCategoryEntity>? = getBudgetCategories(budgetId, DEFAULT_LEDGER_ID)

    @Query("SELECT * FROM budget_account_relation WHERE budget_id=:budgetId AND ledger_id = :ledgerId ORDER BY created_on DESC")
    suspend fun getBudgetAccounts(budgetId: String, ledgerId: String): List<BudgetAccountEntity>?

    suspend fun getBudgetAccounts(budgetId: String): List<BudgetAccountEntity>? = getBudgetAccounts(budgetId, DEFAULT_LEDGER_ID)

    @Query("DELETE FROM budget_category_relation WHERE budget_id = :budgetId")
    suspend fun removeBudgetCategories(budgetId: String)

    @Query("DELETE FROM budget_account_relation WHERE budget_id = :budgetId")
    suspend fun removeBudgetAccounts(budgetId: String)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertBudget(budgetEntity: BudgetEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertBudgetAccountRelation(budgetAccountEntity: BudgetAccountEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertBudgetCategoryRelation(budgetCategoryEntity: BudgetCategoryEntity): Long

    @Update(onConflict = OnConflictStrategy.ABORT)
    suspend fun updateBudget(budgetEntity: BudgetEntity)

    @Transaction
    suspend fun insertBudget(
        budgetEntity: BudgetEntity,
        categories: List<String>,
        accounts: List<String>,
    ): Long {
        val id = insertBudget(budgetEntity)
        if (id != -1L) {
            if (categories.isNotEmpty()) {
                categories.forEach { categoryId ->
                    insertBudgetCategoryRelation(
                        BudgetCategoryEntity(
                            id = UUID.randomUUID().toString(),
                            budgetId = budgetEntity.id,
                            categoryId = categoryId,
                            createdOn = Date(),
                            updatedOn = Date(),
                            ledgerId = budgetEntity.ledgerId,
                        ),
                    )
                }
            }
            if (accounts.isNotEmpty()) {
                accounts.forEach { accountId ->
                    insertBudgetAccountRelation(
                        BudgetAccountEntity(
                            id = UUID.randomUUID().toString(),
                            budgetId = budgetEntity.id,
                            accountId = accountId,
                            createdOn = Date(),
                            updatedOn = Date(),
                            ledgerId = budgetEntity.ledgerId,
                        ),
                    )
                }
            }
        }
        return id
    }

    @Transaction
    suspend fun updateBudget(
        budgetEntity: BudgetEntity,
        categories: List<String>,
        accounts: List<String>,
    ) {
        updateBudget(budgetEntity)

        removeBudgetCategories(budgetId = budgetEntity.id)
        removeBudgetAccounts(budgetId = budgetEntity.id)

        if (categories.isNotEmpty()) {
            categories.forEach { categoryId ->
                insertBudgetCategoryRelation(
                    BudgetCategoryEntity(
                        id = UUID.randomUUID().toString(),
                        budgetId = budgetEntity.id,
                        categoryId = categoryId,
                        createdOn = Date(),
                        updatedOn = Date(),
                        ledgerId = budgetEntity.ledgerId,
                    ),
                )
            }
        }
        if (accounts.isNotEmpty()) {
            accounts.forEach { accountId ->
                insertBudgetAccountRelation(
                    BudgetAccountEntity(
                        id = UUID.randomUUID().toString(),
                        budgetId = budgetEntity.id,
                        accountId = accountId,
                        createdOn = Date(),
                        updatedOn = Date(),
                        ledgerId = budgetEntity.ledgerId,
                    ),
                )
            }
        }
    }
}
