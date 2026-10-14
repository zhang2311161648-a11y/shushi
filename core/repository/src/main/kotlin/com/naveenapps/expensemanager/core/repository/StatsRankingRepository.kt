package com.naveenapps.expensemanager.core.repository

import com.naveenapps.expensemanager.core.model.CategoryRankingItem
import com.naveenapps.expensemanager.core.model.CategoryTransactionRankingItem
import com.naveenapps.expensemanager.core.model.RankingMode
import com.naveenapps.expensemanager.core.model.RankingSortMode
import com.naveenapps.expensemanager.core.model.TransactionType
import kotlinx.coroutines.flow.Flow

interface StatsRankingRepository {

    fun getExpenseCategoryRanking(startDate: Long, endDate: Long): Flow<List<CategoryRankingItem>>

    fun getIncomeCategoryRanking(startDate: Long, endDate: Long): Flow<List<CategoryRankingItem>>

    fun getTransactionRankingByCategory(
        categoryId: String,
        startDate: Long,
        endDate: Long,
        transactionType: TransactionType,
        sortMode: RankingSortMode,
    ): Flow<List<CategoryTransactionRankingItem>>
}
