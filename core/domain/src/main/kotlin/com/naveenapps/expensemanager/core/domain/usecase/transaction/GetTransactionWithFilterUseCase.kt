package com.naveenapps.expensemanager.core.domain.usecase.transaction

import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange.GetDateRangeUseCase
import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.repository.AccountRepository
import com.naveenapps.expensemanager.core.repository.CategoryRepository
import com.naveenapps.expensemanager.core.repository.SettingsRepository
import com.naveenapps.expensemanager.core.repository.TransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest

class GetTransactionWithFilterUseCase(
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository,
    private val getDateRangeUseCase: GetDateRangeUseCase,
    private val transactionRepository: TransactionRepository,
) {
    /** Complete ledger stream for home summaries, independent of list filters. */
    fun invokeUnfiltered(): Flow<List<Transaction>?> = transactionRepository.getAllTransaction()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun invoke(): Flow<List<Transaction>?> {
        return combine(
            settingsRepository.getTransactionTypes(),
            settingsRepository.getCategories(),
            settingsRepository.getAccounts(),
            getDateRangeUseCase.invoke(),
            combine(accountRepository.getAccounts(), categoryRepository.getCategories()) { accounts, categories ->
                accounts.map { it.id } to categories.map { it.id }
            },
        ) { selectedTransactionTypes, selectedCategories, selectedAccounts, dateRangeModel, available ->

            val transactionTypes: List<Int> = if (selectedTransactionTypes.isNullOrEmpty()) {
                TransactionType.entries.map { it.ordinal }
            } else {
                selectedTransactionTypes.map { it.ordinal }
            }

            val accounts: List<String> = if (selectedAccounts.isNullOrEmpty()) {
                available.first
            } else {
                val availableAccountIds = available.first
                selectedAccounts.filter { it in availableAccountIds }.ifEmpty { availableAccountIds }
            }

            val categories: List<String> = if (selectedCategories.isNullOrEmpty()) {
                available.second
            } else {
                val availableCategoryIds = available.second
                selectedCategories.filter { it in availableCategoryIds }.ifEmpty { availableCategoryIds }
            }

            FilterValue(
                dateRangeModel.type,
                dateRangeModel.dateRanges,
                accounts,
                categories,
                transactionTypes,
            )
        }.flatMapLatest {
            return@flatMapLatest if (it.dateRangeType == DateRangeType.ALL) {
                transactionRepository.getAllFilteredTransaction(
                    it.accounts,
                    it.categories,
                    it.transactionTypes,
                )
            } else {
                transactionRepository.getFilteredTransaction(
                    it.accounts,
                    it.categories,
                    it.transactionTypes,
                    it.filterRange[0],
                    it.filterRange[1],
                )
            }
        }
    }
}

data class FilterValue(
    val dateRangeType: DateRangeType,
    val filterRange: List<Long>,
    val accounts: List<String>,
    val categories: List<String>,
    val transactionTypes: List<Int>,
)
