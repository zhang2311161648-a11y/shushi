package com.naveenapps.expensemanager.core.domain.usecase.stats

import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.CategoryRankingItem
import com.naveenapps.expensemanager.core.model.CategoryTransactionRankingItem
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.RankingMode
import com.naveenapps.expensemanager.core.model.RankingSortMode
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.repository.StatsRankingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

class StatsRankingUseCasesTest {

    @Test
    fun monthlyComparisonMatchesCategoryIdsAndHandlesMissingPreviousSpending() = runTest {
        whenever(currencyUseCase.invoke()).thenReturn(flowOf(currency))
        whenever(formattedAmountUseCase.invoke(any(), eq(currency))).thenAnswer { call ->
            val amount = call.getArgument<Double>(0)
            Amount(amount = amount, amountString = String.format(java.util.Locale.ROOT, "¥%.2f", amount), currency = currency)
        }
        whenever(repository.getExpenseCategoryRanking(1000L, 2000L)).thenReturn(flowOf(listOf(
            category("increase", 1234), category("decrease", 1000), category("equal", 500), category("new", 250),
        )))
        whenever(repository.getExpenseCategoryRanking(0L, 1000L)).thenReturn(flowOf(listOf(
            category("equal", 500), category("decrease", 2345), category("increase", 1000),
        )))
        val result = GetStatsCategoryRankingUseCase(repository, currencyUseCase, formattedAmountUseCase, dispatchers)
            .invoke(RankingMode.EXPENSE, 1000L, 2000L, 0L, 1000L).first().associateBy { it.categoryId }
        assertEquals(234L, result.getValue("increase").monthChangeCents)
        assertEquals("¥2.34", result.getValue("increase").monthChangeAmount)
        assertEquals(-1345L, result.getValue("decrease").monthChangeCents)
        assertEquals("¥13.45", result.getValue("decrease").monthChangeAmount)
        assertEquals(0L, result.getValue("equal").monthChangeCents)
        assertEquals(0L, result.getValue("new").previousMonthAmountCents)
        assertEquals(250L, result.getValue("new").monthChangeCents)
    }

    private val repository: StatsRankingRepository = mock()
    private val currencyUseCase: GetCurrencyUseCase = mock()
    private val formattedAmountUseCase: GetFormattedAmountUseCase = mock()
    private val currency = Currency(symbol = "¥", name = "CNY")
    private val dispatchers = AppCoroutineDispatchers(
        main = Dispatchers.Unconfined,
        io = Dispatchers.Unconfined,
        computation = Dispatchers.Unconfined,
    )

    @Test
    fun categoryRankingUsesCentsForTotalsAndPercentages() = runTest {
        whenever(currencyUseCase.invoke()).thenReturn(flowOf(currency))
        whenever(formattedAmountUseCase.invoke(any(), eq(currency))).thenAnswer { invocation ->
            Amount(
                amount = invocation.getArgument<Double>(0),
                amountString = "formatted",
                currency = currency,
            )
        }
        whenever(repository.getExpenseCategoryRanking(1_000L, 2_000L)).thenReturn(
            flowOf(
                listOf(
                    category("food", 6_575L),
                    category("shopping", 2_474L),
                ),
            ),
        )

        val result = GetStatsCategoryRankingUseCase(
            repository = repository,
            getCurrencyUseCase = currencyUseCase,
            getFormattedAmountUseCase = formattedAmountUseCase,
            dispatchers = dispatchers,
        ).invoke(
            mode = RankingMode.EXPENSE,
            startDate = 1_000L,
            endDate = 2_000L,
        ).first()

        assertEquals(6_575L, result[0].totalAmountCents)
        assertEquals("formatted", result[0].totalAmount)
        assertEquals(6_575.0 / 9_049.0 * 100.0, result[0].percentage, 0.0001)
        assertEquals(2_474.0 / 9_049.0 * 100.0, result[1].percentage, 0.0001)
    }

    @Test
    fun transactionRankingComputesPercentWithinCategoryAndForwardsSortMode() = runTest {
        whenever(currencyUseCase.invoke()).thenReturn(flowOf(currency))
        whenever(formattedAmountUseCase.invoke(any(), eq(currency))).thenAnswer { invocation ->
            Amount(
                amount = invocation.getArgument<Double>(0),
                amountString = "formatted",
                currency = currency,
            )
        }
        whenever(
            repository.getTransactionRankingByCategory(
                categoryId = "food",
                startDate = 10L,
                endDate = 20L,
                transactionType = TransactionType.EXPENSE,
                sortMode = RankingSortMode.TIME,
            ),
        ).thenReturn(
            flowOf(
                listOf(
                    transaction("one", 5_500L),
                    transaction("two", 5_000L),
                ),
            ),
        )

        val result = GetStatsCategoryTransactionRankingUseCase(
            repository = repository,
            getCurrencyUseCase = currencyUseCase,
            getFormattedAmountUseCase = formattedAmountUseCase,
            dispatchers = dispatchers,
        ).invoke(
            categoryId = "food",
            startDate = 10L,
            endDate = 20L,
            mode = RankingMode.EXPENSE,
            sortMode = RankingSortMode.TIME,
        ).first()

        verify(repository).getTransactionRankingByCategory(
            categoryId = "food",
            startDate = 10L,
            endDate = 20L,
            transactionType = TransactionType.EXPENSE,
            sortMode = RankingSortMode.TIME,
        )
        assertEquals(5_500L, result[0].amountCents)
        assertEquals(5_500.0 / 10_500.0 * 100.0, result[0].percentage, 0.0001)
        assertEquals(5_000.0 / 10_500.0 * 100.0, result[1].percentage, 0.0001)
    }

    @Test
    fun percentageIsZeroWhenCategoryTotalIsZero() {
        assertEquals(0.0, percentageOf(0L, 0L), 0.0)
    }

    private fun category(id: String, cents: Long) = CategoryRankingItem(
        categoryId = id,
        categoryName = id,
        categoryTitleResId = null,
        categoryIcon = StoredIcon(name = "restaurant", backgroundColor = "#8B1E3F"),
        totalAmountCents = cents,
        totalAmount = "",
        percentage = 0.0,
    )

    private fun transaction(id: String, cents: Long) = CategoryTransactionRankingItem(
        transactionId = id,
        displayName = id,
        categoryName = "food",
        categoryTitleResId = null,
        categoryIcon = StoredIcon(name = "restaurant", backgroundColor = "#8B1E3F"),
        amountCents = cents,
        amount = "",
        percentage = 0.0,
        createdOn = Date(1_000L),
    )
}
