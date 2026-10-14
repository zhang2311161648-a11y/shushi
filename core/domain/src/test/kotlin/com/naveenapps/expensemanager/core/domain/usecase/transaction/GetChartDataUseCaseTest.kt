package com.naveenapps.expensemanager.core.domain.usecase.transaction

import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.domain.usecase.account.GetAllAccountsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange.GetDateRangeUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange.GetTransactionGroupTypeUseCase
import com.naveenapps.expensemanager.core.model.*
import com.naveenapps.expensemanager.core.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.mockito.kotlin.*
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class GetChartDataUseCaseTest {
    private fun millis(date: String): Long = LocalDateTime.parse(date)
        .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun transaction(date: String, type: TransactionType, cents: Long) = Transaction(
        id = "$date-$type-$cents", notes = "", categoryId = "category", fromAccountId = "account",
        toAccountId = if (type == TransactionType.TRANSFER) "other" else null,
        amount = Amount(cents / 100.0), imagePath = "", type = type,
        createdOn = Date(millis(date)), updatedOn = Date(millis(date)),
    )

    private suspend fun chart(start: String, end: String, currentBalance: Long, selected: List<Transaction>, all: List<Transaction>, scheduler: TestCoroutineScheduler): AnalysisData {
        val currency = Currency("¥", "Chinese Yuan", code = "CNY")
        val currencyUseCase = mock<GetCurrencyUseCase>()
        whenever(currencyUseCase.invoke()).thenReturn(flowOf(currency))
        val format = mock<GetFormattedAmountUseCase>()
        whenever(format.invoke(any(), any())).thenAnswer { Amount(it.getArgument(0), "formatted") }
        val range = mock<GetDateRangeUseCase>()
        whenever(range.invoke()).thenReturn(flowOf(DateRangeModel("Custom", "", DateRangeType.CUSTOM, listOf(millis(start), millis(end)))))
        val grouping = mock<GetTransactionGroupTypeUseCase>()
        whenever(grouping.invoke(DateRangeType.CUSTOM)).thenReturn(GroupType.DATE)
        val filtered = mock<GetTransactionWithFilterUseCase>()
        whenever(filtered.invoke()).thenReturn(flowOf(selected))
        val accounts = mock<GetAllAccountsUseCase>()
        val account = Account("account", "Account", AccountType.REGULAR, StoredIcon("", ""), Date(), Date(), currentBalanceCents = currentBalance)
        whenever(accounts.invoke()).thenReturn(flowOf(listOf(account)))
        val repository = mock<TransactionRepository>()
        whenever(repository.getAllTransaction()).thenReturn(flowOf(all))
        val dispatcher = UnconfinedTestDispatcher(scheduler)
        return GetChartDataUseCase(currencyUseCase, format, range, grouping, filtered,
            AppCoroutineDispatchers(dispatcher, dispatcher, dispatcher), accounts, repository).invoke().first()
    }

    @Test
    fun historicalRangeRewindsFutureIncomeAndIgnoresTransfers() = runTest {
        val spending = transaction("2026-10-01T22:10:00", TransactionType.EXPENSE, 1000)
        val transfer = transaction("2026-10-02T00:05:00", TransactionType.TRANSFER, 50000)
        val futureIncome = transaction("2026-10-05T12:00:00", TransactionType.INCOME, 3000)
        val result = chart("2026-10-01T00:00:00", "2026-10-03T23:59:59", 12000,
            listOf(spending, transfer), listOf(spending, transfer, futureIncome), testScheduler).chartData!!
        assertEquals(listOf("10-01", "10-02", "10-03"), result.dates)
        assertEquals(listOf(1000L, 0L, 0L), result.chartData[0].map { it.totalCents })
        assertEquals(listOf(9000L, 9000L, 9000L), result.chartData[1].map { it.totalCents })
    }

    @Test
    fun sameDayTransactionsBeforeRangeStartAreNotAddedTwice() = runTest {
        val morning = transaction("2026-10-01T08:00:00", TransactionType.INCOME, 1000)
        val afternoon = transaction("2026-10-01T13:00:00", TransactionType.EXPENSE, 200)
        val result = chart("2026-10-01T12:00:00", "2026-10-01T23:59:59", 10800,
            listOf(afternoon), listOf(morning, afternoon), testScheduler).chartData!!
        assertEquals(listOf(10800L), result.chartData[1].map { it.totalCents })
    }

    @Test
    fun sameDayTransactionsAfterRangeEndDoNotChangeHistoricalBalance() = runTest {
        val morning = transaction("2026-10-01T08:00:00", TransactionType.EXPENSE, 200)
        val afternoon = transaction("2026-10-01T13:00:00", TransactionType.INCOME, 1000)
        val result = chart("2026-10-01T00:00:00", "2026-10-01T12:00:00", 10800,
            listOf(morning), listOf(morning, afternoon), testScheduler).chartData!!
        assertEquals(listOf(9800L), result.chartData[1].map { it.totalCents })
    }

    @Test
    fun emptySpendingRangeStillShowsAccountBalance() = runTest {
        val result = chart("2026-10-01T00:00:00", "2026-10-02T23:59:59", 12345,
            emptyList(), emptyList(), testScheduler)
        assertNotNull(result.chartData)
        assertEquals(listOf(12345L, 12345L), result.chartData!!.chartData[1].map { it.totalCents })
    }
}
