package com.naveenapps.expensemanager.core.data.mappers

import com.naveenapps.expensemanager.core.database.entity.BudgetEntity
import com.naveenapps.expensemanager.core.model.Budget
import com.naveenapps.expensemanager.core.model.BudgetPeriod
import java.time.LocalDate

fun Budget.toEntityModel(): BudgetEntity {
    return BudgetEntity(
        id = id,
        amount = amount,
        selectedMonth = selectedMonth,
        periodType = periodType.ordinal,
        isAllCategoriesSelected = isAllCategoriesSelected,
        isAllAccountsSelected = isAllAccountsSelected,
        createdOn = createdOn,
        updatedOn = updatedOn,
        accountingPeriodStartDate = accountingPeriodStartDate?.toString(),
        accountingPeriodEndExclusiveDate = accountingPeriodEndExclusiveDate?.toString(),
    )
}

fun BudgetEntity.toDomainModel(categories: List<String>, accounts: List<String>): Budget {
    return Budget(
        id = id,
        amount = amount,
        selectedMonth = selectedMonth,
        // Guard against an out-of-range ordinal (e.g. a downgraded app reading a value written by
        // a newer BudgetPeriod entry it doesn't know about) by falling back to MONTHLY.
        periodType = BudgetPeriod.entries.getOrElse(periodType) { BudgetPeriod.MONTHLY },
        categories = categories,
        accounts = accounts,
        isAllCategoriesSelected = isAllCategoriesSelected,
        isAllAccountsSelected = isAllAccountsSelected,
        createdOn = createdOn,
        updatedOn = updatedOn,
        accountingPeriodStartDate = accountingPeriodStartDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
        accountingPeriodEndExclusiveDate = accountingPeriodEndExclusiveDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
    )
}
