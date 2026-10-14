package com.naveenapps.expensemanager.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.naveenapps.expensemanager.core.database.dao.AccountDao
import com.naveenapps.expensemanager.core.database.dao.AssetDao
import com.naveenapps.expensemanager.core.database.dao.AnxietyReliefItemDao
import com.naveenapps.expensemanager.core.database.dao.BudgetDao
import com.naveenapps.expensemanager.core.database.dao.CategoryDao
import com.naveenapps.expensemanager.core.database.dao.CategorySuggestionRuleDao
import com.naveenapps.expensemanager.core.database.dao.MonthlySavingDao
import com.naveenapps.expensemanager.core.database.dao.TransactionDao
import com.naveenapps.expensemanager.core.database.dao.StatsRankingDao
import com.naveenapps.expensemanager.core.database.dao.SalaryCycleDao
import com.naveenapps.expensemanager.core.database.dao.LedgerDao
import com.naveenapps.expensemanager.core.database.entity.AccountEntity
import com.naveenapps.expensemanager.core.database.entity.AssetEntity
import com.naveenapps.expensemanager.core.database.entity.AnxietyReliefItemEntity
import com.naveenapps.expensemanager.core.database.entity.BudgetAccountEntity
import com.naveenapps.expensemanager.core.database.entity.BudgetCategoryEntity
import com.naveenapps.expensemanager.core.database.entity.BudgetEntity
import com.naveenapps.expensemanager.core.database.entity.CategoryEntity
import com.naveenapps.expensemanager.core.database.entity.CategorySuggestionRuleEntity
import com.naveenapps.expensemanager.core.database.entity.TransactionAttachmentEntity
import com.naveenapps.expensemanager.core.database.entity.TransactionEntity
import com.naveenapps.expensemanager.core.database.entity.MonthlySavingEntity
import com.naveenapps.expensemanager.core.database.entity.SalaryCycleEntity
import com.naveenapps.expensemanager.core.database.entity.LedgerEntity
import com.naveenapps.expensemanager.core.database.utils.AccountTypeConverter
import com.naveenapps.expensemanager.core.database.utils.CategoryTypeConverter
import com.naveenapps.expensemanager.core.database.utils.DateConverter
import com.naveenapps.expensemanager.core.database.utils.TransactionTypeConverter

/**
 * The Room database for this app
 */
@Database(
    entities = [
        CategoryEntity::class,
        TransactionEntity::class,
        AccountEntity::class,
        BudgetEntity::class,
        BudgetCategoryEntity::class,
        BudgetAccountEntity::class,
        TransactionAttachmentEntity::class,
        AssetEntity::class,
        AnxietyReliefItemEntity::class,
        MonthlySavingEntity::class,
        CategorySuggestionRuleEntity::class,
        SalaryCycleEntity::class,
        LedgerEntity::class,
        com.naveenapps.expensemanager.core.database.entity.BookkeepingDraftBatchEntity::class,
    ],
    version = 19,
    exportSchema = true,
)
@TypeConverters(
    DateConverter::class,
    TransactionTypeConverter::class,
    CategoryTypeConverter::class,
    AccountTypeConverter::class,
)
abstract class ExpenseManagerDatabase : RoomDatabase() {
    abstract fun bookkeepingDraftDao(): com.naveenapps.expensemanager.core.database.dao.BookkeepingDraftDao

    abstract fun categoryDao(): CategoryDao

    abstract fun categorySuggestionRuleDao(): CategorySuggestionRuleDao

    abstract fun transactionDao(): TransactionDao

    abstract fun statsRankingDao(): StatsRankingDao

    abstract fun accountDao(): AccountDao

    abstract fun assetDao(): AssetDao

    abstract fun anxietyReliefItemDao(): AnxietyReliefItemDao

    abstract fun monthlySavingDao(): MonthlySavingDao

    abstract fun salaryCycleDao(): SalaryCycleDao

    abstract fun budgetDao(): BudgetDao

    abstract fun ledgerDao(): LedgerDao
}
