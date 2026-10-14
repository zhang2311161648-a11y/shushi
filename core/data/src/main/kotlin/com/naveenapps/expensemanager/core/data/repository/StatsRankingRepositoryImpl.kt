package com.naveenapps.expensemanager.core.data.repository

import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.common.utils.fromLocalToUTCTimeStamp
import com.naveenapps.expensemanager.core.data.mappers.toDomainModel
import com.naveenapps.expensemanager.core.database.dao.StatsRankingDao
import com.naveenapps.expensemanager.core.model.CategoryRankingItem
import com.naveenapps.expensemanager.core.model.CategoryTransactionRankingItem
import com.naveenapps.expensemanager.core.model.RankingSortMode
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.model.DEFAULT_LEDGER_ID
import com.naveenapps.expensemanager.core.repository.CurrentLedgerRepository
import com.naveenapps.expensemanager.core.repository.StatsRankingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class StatsRankingRepositoryImpl(
    private val statsRankingDao: StatsRankingDao,
    private val dispatchers: AppCoroutineDispatchers,
    private val currentLedgerRepository: CurrentLedgerRepository? = null,
) : StatsRankingRepository {

    override fun getExpenseCategoryRanking(
        startDate: Long,
        endDate: Long,
    ): Flow<List<CategoryRankingItem>> = getCategoryRanking(
        transactionType = TransactionType.EXPENSE,
        startDate = startDate,
        endDate = endDate,
    )

    override fun getIncomeCategoryRanking(
        startDate: Long,
        endDate: Long,
    ): Flow<List<CategoryRankingItem>> = getCategoryRanking(
        transactionType = TransactionType.INCOME,
        startDate = startDate,
        endDate = endDate,
    )

    override fun getTransactionRankingByCategory(
        categoryId: String,
        startDate: Long,
        endDate: Long,
        transactionType: TransactionType,
        sortMode: RankingSortMode,
    ): Flow<List<CategoryTransactionRankingItem>> {
        val startUtc = startDate.fromLocalToUTCTimeStamp()
        val endUtc = endDate.fromLocalToUTCTimeStamp()
        val transactions = currentLedgerRepository?.observeCurrentLedgerId()?.flatMapLatest { ledgerId ->
            when (sortMode) {
                RankingSortMode.AMOUNT -> statsRankingDao.getTransactionsByCategoryByAmount(
                    categoryId = categoryId,
                    transactionType = transactionType.ordinal,
                    startDate = startUtc,
                    endDate = endUtc,
                    ledgerId = ledgerId,
                )

                RankingSortMode.TIME -> statsRankingDao.getTransactionsByCategoryByTime(
                    categoryId = categoryId,
                    transactionType = transactionType.ordinal,
                    startDate = startUtc,
                    endDate = endUtc,
                    ledgerId = ledgerId,
                )
            }
        } ?: when (sortMode) {
            RankingSortMode.AMOUNT -> statsRankingDao.getTransactionsByCategoryByAmount(
                categoryId = categoryId,
                transactionType = transactionType.ordinal,
                startDate = startUtc,
                endDate = endUtc,
            )

            RankingSortMode.TIME -> statsRankingDao.getTransactionsByCategoryByTime(
                categoryId = categoryId,
                transactionType = transactionType.ordinal,
                startDate = startUtc,
                endDate = endUtc,
            )
        }
        return transactions
            .map { rows -> rows.map { it.toDomainModel() } }
            .flowOn(dispatchers.io)
    }

    private fun getCategoryRanking(
        transactionType: TransactionType,
        startDate: Long,
        endDate: Long,
    ): Flow<List<CategoryRankingItem>> {
        val rankings = currentLedgerRepository?.observeCurrentLedgerId()?.flatMapLatest { ledgerId ->
            statsRankingDao.getCategoryRanking(
                transactionType = transactionType.ordinal,
                startDate = startDate.fromLocalToUTCTimeStamp(),
                endDate = endDate.fromLocalToUTCTimeStamp(),
                ledgerId = ledgerId,
            )
        } ?: statsRankingDao.getCategoryRanking(
            transactionType = transactionType.ordinal,
            startDate = startDate.fromLocalToUTCTimeStamp(),
            endDate = endDate.fromLocalToUTCTimeStamp(),
        )
        return rankings.map { rows -> rows.map { it.toDomainModel() } }
            .flowOn(dispatchers.io)
    }
}
