package com.naveenapps.expensemanager.core.domain.usecase.budget

import com.naveenapps.expensemanager.core.common.utils.fromMonthAndYearKey
import com.naveenapps.expensemanager.core.common.utils.fromYear
import com.naveenapps.expensemanager.core.common.utils.getEndOfTheMonth
import com.naveenapps.expensemanager.core.common.utils.getEndOfTheYear
import com.naveenapps.expensemanager.core.common.utils.getStartOfTheMonth
import com.naveenapps.expensemanager.core.common.utils.getStartOfTheYear
import com.naveenapps.expensemanager.core.common.utils.toMonthAndYearKey
import com.naveenapps.expensemanager.core.common.utils.toYear
import com.naveenapps.expensemanager.core.model.Budget
import com.naveenapps.expensemanager.core.model.BudgetPeriod
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.repository.AccountRepository
import com.naveenapps.expensemanager.core.repository.CategoryRepository
import com.naveenapps.expensemanager.core.repository.TransactionRepository
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate
import java.time.ZoneId

class GetBudgetTransactionsUseCase(
    private val categoryRepository: CategoryRepository,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(budget: Budget): Resource<List<Transaction>> {
        val accounts: List<String> = if (budget.isAllAccountsSelected) {
            accountRepository.getAccounts().firstOrNull()?.map { it.id } ?: emptyList()
        } else {
            budget.accounts
        }

        val categories: List<String> = if (budget.isAllCategoriesSelected) {
            categoryRepository.getCategories().firstOrNull()?.map { it.id } ?: emptyList()
        } else {
            budget.categories
        }

        val customStart = budget.accountingPeriodStartDate
        val customEndExclusive = budget.accountingPeriodEndExclusiveDate
        val isCustomPeriod = customStart != null && customEndExclusive != null
        val isYearly = budget.periodType == BudgetPeriod.YEARLY
        val date = if (isYearly) budget.selectedMonth.fromYear() else budget.selectedMonth.fromMonthAndYearKey()

        val startDate: Long
        val endDate: Long
        if (isCustomPeriod) {
            startDate = customStart!!.atStartOfDayMillis()
            // Existing transaction queries use an inclusive upper bound. Subtract one millisecond
            // from the exclusive local-date boundary so the next period cannot leak in.
            endDate = customEndExclusive!!.atStartOfDayMillis() - 1L
        } else {
            date ?: return Resource.Error(IllegalArgumentException("Unknown month value"))
            startDate = if (isYearly) date.getStartOfTheYear() else date.getStartOfTheMonth()
            endDate = if (isYearly) date.getEndOfTheYear() else date.getEndOfTheMonth()
        }

        val transaction = transactionRepository.getFilteredTransaction(
            accounts = accounts,
            categories = categories,
            transactionType = CategoryType.entries.map { it.ordinal }.toList(),
            startDate = startDate,
            endDate = endDate,
        ).firstOrNull()?.filter {
            if (isCustomPeriod) {
                it.createdOn.time >= startDate && it.createdOn.time < customEndExclusive!!
                    .atStartOfDayMillis()
            } else if (isYearly) {
                it.createdOn.toYear() == budget.selectedMonth
            } else {
                it.createdOn.toMonthAndYearKey() == budget.selectedMonth
            }
        }

        return Resource.Success(transaction ?: emptyList())
    }

    private fun LocalDate.atStartOfDayMillis(): Long =
        atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}
