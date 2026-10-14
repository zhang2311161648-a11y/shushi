package com.naveenapps.expensemanager.core.domain.usecase.stats

import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.model.CategoryRankingItem
import com.naveenapps.expensemanager.core.model.CategoryTransactionRankingItem
import com.naveenapps.expensemanager.core.model.RankingMode
import com.naveenapps.expensemanager.core.model.RankingSortMode
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.repository.StatsRankingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn

class GetStatsCategoryRankingUseCase(
    private val repository: StatsRankingRepository,
    private val getCurrencyUseCase: GetCurrencyUseCase,
    private val getFormattedAmountUseCase: GetFormattedAmountUseCase,
    private val dispatchers: AppCoroutineDispatchers,
) {

    fun invoke(
        mode: RankingMode,
        startDate: Long,
        endDate: Long,
        comparisonStartDate: Long? = null,
        comparisonEndDate: Long? = null,
    ): Flow<List<CategoryRankingItem>> {
        val source = when (mode) {
            RankingMode.EXPENSE -> repository.getExpenseCategoryRanking(startDate, endDate)
            RankingMode.INCOME -> repository.getIncomeCategoryRanking(startDate, endDate)
        }

        val previousSource = if (mode == RankingMode.EXPENSE && comparisonStartDate != null && comparisonEndDate != null) {
            repository.getExpenseCategoryRanking(comparisonStartDate, comparisonEndDate)
        } else kotlinx.coroutines.flow.flowOf(emptyList())
        return combine(source, previousSource, getCurrencyUseCase.invoke()) { items, previous, currency ->
            val total = items.sumOf { it.totalAmountCents }
            val previousAmounts = previous.associate { it.categoryId to it.totalAmountCents }
            items.map { item ->
                val prior = if (mode == RankingMode.EXPENSE && comparisonStartDate != null && comparisonEndDate != null) previousAmounts[item.categoryId] ?: 0L else null
                val change = prior?.let { item.totalAmountCents - it }
                item.copy(
                    totalAmount = getFormattedAmountUseCase.invoke(
                        amount = item.totalAmountCents / 100.0,
                        currency = currency,
                    ).amountString ?: "",
                    percentage = percentageOf(item.totalAmountCents, total),
                    previousMonthAmountCents = prior,
                    monthChangeCents = change,
                    monthChangeAmount = change?.let {
                        getFormattedAmountUseCase.invoke(kotlin.math.abs(it) / 100.0, currency).amountString ?: ""
                    },
                )
            }
        }.flowOn(dispatchers.computation)
    }
}

class GetStatsCategoryTransactionRankingUseCase(
    private val repository: StatsRankingRepository,
    private val getCurrencyUseCase: GetCurrencyUseCase,
    private val getFormattedAmountUseCase: GetFormattedAmountUseCase,
    private val dispatchers: AppCoroutineDispatchers,
) {

    fun invoke(
        categoryId: String,
        startDate: Long,
        endDate: Long,
        mode: RankingMode,
        sortMode: RankingSortMode,
    ): Flow<List<CategoryTransactionRankingItem>> {
        return combine(
            repository.getTransactionRankingByCategory(
                categoryId = categoryId,
                startDate = startDate,
                endDate = endDate,
                transactionType = when (mode) {
                    RankingMode.EXPENSE -> TransactionType.EXPENSE
                    RankingMode.INCOME -> TransactionType.INCOME
                },
                sortMode = sortMode,
            ),
            getCurrencyUseCase.invoke(),
        ) { items, currency ->
            val total = items.sumOf { it.amountCents }
            items.map { item ->
                item.copy(
                    amount = getFormattedAmountUseCase.invoke(
                        amount = item.amountCents / 100.0,
                        currency = currency,
                    ).amountString ?: "",
                    percentage = percentageOf(item.amountCents, total),
                )
            }
        }.flowOn(dispatchers.computation)
    }
}

internal fun percentageOf(amountCents: Long, totalCents: Long): Double {
    return if (totalCents > 0L) amountCents.toDouble() / totalCents * 100.0 else 0.0
}
