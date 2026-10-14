package com.naveenapps.expensemanager.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "budget", indices = [Index(value = ["ledger_id"])])
data class BudgetEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "selected_month")
    val selectedMonth: String,
    @ColumnInfo(name = "amount")
    val amount: Double,
    /** Raw ordinal of `core.model.BudgetPeriod` (0 = MONTHLY, 1 = YEARLY). Defaults to 0 so rows
     * written before this column existed are backfilled as MONTHLY by MIGRATION_5_6. */
    @ColumnInfo(name = "period_type", defaultValue = "0")
    val periodType: Int = 0,
    @ColumnInfo(name = "all_accounts_selected")
    val isAllAccountsSelected: Boolean,
    @ColumnInfo(name = "all_categories_selected")
    val isAllCategoriesSelected: Boolean,
    @ColumnInfo(name = "created_on")
    val createdOn: Date,
    @ColumnInfo(name = "updated_on")
    val updatedOn: Date,
    @ColumnInfo(name = "accounting_period_start_date", defaultValue = "NULL")
    val accountingPeriodStartDate: String? = null,
    @ColumnInfo(name = "accounting_period_end_date", defaultValue = "NULL")
    val accountingPeriodEndExclusiveDate: String? = null,
    @ColumnInfo(name = "ledger_id", defaultValue = "'default'")
    val ledgerId: String = com.naveenapps.expensemanager.core.model.DEFAULT_LEDGER_ID,
)
