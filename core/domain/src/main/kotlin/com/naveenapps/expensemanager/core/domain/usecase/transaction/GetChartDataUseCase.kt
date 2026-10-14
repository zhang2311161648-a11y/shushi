package com.naveenapps.expensemanager.core.domain.usecase.transaction

import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.common.utils.toCompleteDate
import com.naveenapps.expensemanager.core.common.utils.toCompleteDateWithDate
import com.naveenapps.expensemanager.core.common.utils.toMonthAndYear
import com.naveenapps.expensemanager.core.common.utils.toYear
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange.GetDateRangeUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange.GetTransactionGroupTypeUseCase
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.model.GroupType
import com.naveenapps.expensemanager.core.model.TransactionUiItem
import com.naveenapps.expensemanager.core.model.isExpense
import com.naveenapps.expensemanager.core.model.isIncome
import com.naveenapps.expensemanager.core.model.effectiveBaseAmountCents
import com.naveenapps.expensemanager.core.model.toAmountDouble
import com.naveenapps.expensemanager.core.model.toTransactionUIModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import com.naveenapps.expensemanager.core.domain.usecase.account.GetAllAccountsUseCase
import kotlin.time.ExperimentalTime

class GetChartDataUseCase(
    private val getCurrencyUseCase: GetCurrencyUseCase,
    private val getFormattedAmountUseCase: GetFormattedAmountUseCase,
    private val getDateRangeUseCase: GetDateRangeUseCase,
    private val getTransactionGroupTypeUseCase: GetTransactionGroupTypeUseCase,
    private val getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase,
    private val dispatcher: AppCoroutineDispatchers,
    private val getAllAccountsUseCase: GetAllAccountsUseCase? = null,
    private val transactionRepository: com.naveenapps.expensemanager.core.repository.TransactionRepository? = null,
) {
    @OptIn(ExperimentalTime::class)
    fun invoke(): Flow<AnalysisData> {
        return combine(
            getDateRangeUseCase.invoke(),
            getCurrencyUseCase.invoke(),
            getTransactionWithFilterUseCase.invoke(),
            getAllAccountsUseCase?.invoke() ?: kotlinx.coroutines.flow.flowOf(emptyList()),
            transactionRepository?.getAllTransaction() ?: getTransactionWithFilterUseCase.invoke(),
        ) { dateRangeModel, currency, transactions, accounts, allTransactions ->

            if (transactions.isNullOrEmpty() && allTransactions.isNullOrEmpty() && accounts.isEmpty()) {
                return@combine AnalysisData(emptyList(), null)
            }

            val isAllTime = dateRangeModel.type == DateRangeType.ALL

            // For "All time", always group by year regardless of the repository's suggestion.
            // ranges[1] is Long.MAX_VALUE for ALL, which would make the while loop iterate
            // from 1970 to year ~292 million. Forcing YEAR and clamping both ends to actual
            // transaction dates keeps the chart point count equal to the number of distinct
            // years in the data — naturally bounded, no hard cap needed.
            // A month selected from the Stats month picker is persisted as a CUSTOM range so
            // payroll-cycle settings cannot replace it with the current month. Keep the chart
            // daily for that exact calendar month, just like the built-in THIS_MONTH range.
            val groupType = when {
                isAllTime -> GroupType.YEAR
                dateRangeModel.dateRanges.isFullCalendarMonth() -> GroupType.DATE
                dateRangeModel.dateRanges.isFullCalendarYear() -> GroupType.MONTH
                else -> getTransactionGroupTypeUseCase.invoke(dateRangeModel.type)
            }

            val zeroFormatted = getFormattedAmountUseCase.invoke(0.0, currency)

            // Single pass: aggregate totals per date bucket AND build UI items simultaneously.
            // LongArray(2) holds [totalExpenseCents, totalIncomeCents] per bucket — avoids the
            // groupBy() call that previously created a HashMap<String, MutableList<Transaction>>,
            // which doubled peak memory by keeping a second reference-structure over all
            // transactions alive alongside the original list.
            val filteredTransactions = transactions.orEmpty()
            val bucketTotals = HashMap<String, LongArray>(minOf(filteredTransactions.size, MAX_CHART_POINTS))
            val transactionItems = ArrayList<TransactionUiItem>(filteredTransactions.size)

            filteredTransactions.forEach { item ->
                val key = groupValue(groupType, item.createdOn)
                val bucket = bucketTotals.getOrPut(key) { LongArray(2) }
                val amountCents = item.effectiveBaseAmountCents()
                when {
                    item.type == com.naveenapps.expensemanager.core.model.TransactionType.EXPENSE -> bucket[0] += amountCents
                    item.type == com.naveenapps.expensemanager.core.model.TransactionType.INCOME -> bucket[1] += amountCents
                }
                transactionItems.add(
                    item.toTransactionUIModel(
                        getFormattedAmountUseCase.invoke(amountCents.toAmountDouble(), currency),
                    )
                )
            }

            val ranges = dateRangeModel.dateRanges
            val earliestTransactionMs = allTransactions.orEmpty().minOfOrNull { it.createdOn.time } ?: Date().time
            val latestTransactionMs = allTransactions.orEmpty().maxOfOrNull { it.createdOn.time } ?: Date().time

            // For ALL: extend one year before the earliest transaction and one year after
            // the latest, so the chart shows an empty bucket on each side for visual context.
            // For other ranges: clamp only the start (keeps empty trailing buckets visible
            // for ranges the user explicitly selected).
            val effectiveFromMs = if (isAllTime) {
                kotlin.time.Instant.fromEpochMilliseconds(earliestTransactionMs)
                    .minus(1, DateTimeUnit.YEAR, TimeZone.UTC)
                    .toEpochMilliseconds()
            } else {
                ranges[0]
            }
            val effectiveToMs = if (isAllTime) {
                kotlin.time.Instant.fromEpochMilliseconds(latestTransactionMs)
                    .plus(1, DateTimeUnit.YEAR, TimeZone.UTC)
                    .toEpochMilliseconds()
            } else {
                val endIsMidnight = java.time.Instant.ofEpochMilli(ranges[1])
                    .atZone(java.time.ZoneId.systemDefault()).toLocalTime() == java.time.LocalTime.MIDNIGHT
                if (endIsMidnight && (ranges.isFullCalendarMonth() || ranges.isFullCalendarYear() ||
                    dateRangeModel.type == DateRangeType.THIS_WEEK || dateRangeModel.type == DateRangeType.TODAY)) {
                    ranges[1] - 1L
                } else ranges[1]
            }

            var fromDate = kotlin.time.Instant.fromEpochMilliseconds(effectiveFromMs)
            val toDate = kotlin.time.Instant.fromEpochMilliseconds(effectiveToMs)

            val dates = ArrayList<String>()
            val expenses = ArrayList<FloatEntryModel>()
            val balances = ArrayList<FloatEntryModel>()
            val totalAccountBalanceCents = accounts.sumOf { it.currentBalanceCents }
            val balanceTransactions = allTransactions.orEmpty()
            fun netChange(item: com.naveenapps.expensemanager.core.model.Transaction): Long = when (item.type) {
                com.naveenapps.expensemanager.core.model.TransactionType.INCOME -> item.effectiveBaseAmountCents()
                com.naveenapps.expensemanager.core.model.TransactionType.EXPENSE -> -item.effectiveBaseAmountCents()
                com.naveenapps.expensemanager.core.model.TransactionType.TRANSFER -> 0L
            }
            val balanceBuckets = balanceTransactions.filter { it.createdOn.time in effectiveFromMs..effectiveToMs }.groupBy { groupValue(groupType, it.createdOn) }
                .mapValues { (_, items) -> items.sumOf(::netChange) }
            var runningBalanceCents = totalAccountBalanceCents - balanceTransactions
                .filter { it.createdOn.time >= effectiveFromMs }.sumOf(::netChange)
            var index = 0

            // MAX_CHART_POINTS is a safety net for wide CUSTOM ranges (e.g. a custom daily
            // range spanning several years). For DateRangeType.ALL this cap is never hit
            // because the loop is bounded by actual transaction years above.
            while (fromDate <= toDate && index < MAX_CHART_POINTS) {
                val key = groupValue(groupType, fromDate.toEpochMilliseconds().toCompleteDate())
                val totals = bucketTotals[key]
                val totalExpenseCents = totals?.get(0) ?: 0L
                val totalIncomeCents = totals?.get(1) ?: 0L

                dates.add(if (groupType == GroupType.DATE) key.takeLast(5) else key)
                expenses.add(
                    FloatEntryModel(
                        index,
                        totalExpenseCents,
                        if (totalExpenseCents == 0L) zeroFormatted
                        else getFormattedAmountUseCase.invoke(totalExpenseCents.toAmountDouble(), currency)
                    )
                )
                runningBalanceCents += balanceBuckets[key] ?: 0L
                balances.add(
                    FloatEntryModel(
                        index,
                        runningBalanceCents,
                        getFormattedAmountUseCase.invoke(runningBalanceCents.toAmountDouble(), currency)
                    )
                )

                fromDate = getAdjustedDateTime(groupType, fromDate)
                index++
            }

            AnalysisData(
                transactionItems,
                AnalysisChartData(listOf(expenses, balances), dates)
            )
        }.flowOn(dispatcher.computation)
    }

    @OptIn(ExperimentalTime::class)
    private fun getAdjustedDateTime(
        groupType: GroupType,
        fromDate: kotlin.time.Instant,
    ) = when (groupType) {
        GroupType.YEAR -> fromDate.plus(1, DateTimeUnit.YEAR, TimeZone.UTC)
        GroupType.MONTH -> fromDate.plus(1, DateTimeUnit.MONTH, TimeZone.UTC)
        GroupType.DATE -> fromDate.plus(1, DateTimeUnit.DAY, TimeZone.UTC)
    }

    private fun groupValue(groupType: GroupType, transactionDate: Date) = when (groupType) {
        GroupType.YEAR -> transactionDate.toYear()
        GroupType.MONTH -> transactionDate.toMonthAndYear()
        GroupType.DATE -> SimpleDateFormat("yyyy-MM-dd", Locale.US).format(transactionDate)
    }

    companion object {
        // Caps the chart series length. A daily chart over 500 days is already hard to
        // read on mobile; this also prevents the while loop from allocating unboundedly
        // when the date range is very wide (e.g. DateRangeType.ALL).
        private const val MAX_CHART_POINTS = 500
    }
}

private fun List<Long>.isFullCalendarMonth(): Boolean {
    if (size < 2) return false
    val zone = java.time.ZoneId.systemDefault()
    val start = java.util.Date(this[0]).toInstant().atZone(zone).toLocalDate()
    val end = java.util.Date(this[1]).toInstant().atZone(zone).toLocalDate()
    return start.dayOfMonth == 1 && end == start.plusMonths(1)
}

private fun List<Long>.isFullCalendarYear(): Boolean {
    if (size < 2) return false
    val zone = java.time.ZoneId.systemDefault()
    val start = java.util.Date(this[0]).toInstant().atZone(zone).toLocalDate()
    val end = java.util.Date(this[1]).toInstant().atZone(zone).toLocalDate()
    return start.dayOfYear == 1 && end == start.plusYears(1)
}

data class FloatEntryModel(
    val index: Int,
    val totalCents: Long,
    val amountString: Amount,
) {
    /** Chart libraries consume Double; all monetary accumulation remains in cents. */
    val total: Double
        get() = totalCents.toAmountDouble()
}

data class AnalysisData(
    val transactions: List<TransactionUiItem>,
    val chartData: AnalysisChartData? = null,
)

data class AnalysisChartData(
    val chartData: List<List<FloatEntryModel>>,
    val dates: List<String>,
    val title: String? = null,
)
