package com.naveenapps.expensemanager.core.domain.usecase.transaction

import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange.GetDateRangeByTypeUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange.GetDateRangeUseCase
import com.naveenapps.expensemanager.core.model.*
import com.naveenapps.expensemanager.core.repository.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.*
import java.util.Date

class GetTransactionWithFilterUseCaseTest {
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun importedAccountsAndCategoriesRefreshExistingSubscription() = runTest {
        val accounts = MutableStateFlow<List<Account>>(emptyList())
        val categories = MutableStateFlow<List<Category>>(emptyList())
        val accountRepository = mock<AccountRepository> {
            on { getAccounts() } doReturn accounts
        }
        val categoryRepository = mock<CategoryRepository> {
            on { getCategories() } doReturn categories
        }
        val settings = mock<SettingsRepository> {
            on { getTransactionTypes() } doReturn flowOf(emptyList())
            on { getCategories() } doReturn flowOf(emptyList())
            on { getAccounts() } doReturn flowOf(emptyList())
        }
        val dates = mock<DateRangeFilterRepository> {
            on { getDateRangeFilterType() } doReturn flowOf(DateRangeType.ALL)
            on { getDateRangeTimeFrame() } doReturn flowOf(null)
        }
        whenever(dates.getDateRangeFilterTypeString(DateRangeType.ALL))
            .thenReturn(DateRangeModel("All", "", DateRangeType.ALL, emptyList()))
        val repository = mock<TransactionRepository>()
        whenever(repository.getAllFilteredTransaction(any(), any(), any())).thenReturn(flowOf(emptyList()))
        val useCase = GetTransactionWithFilterUseCase(
            accountRepository, categoryRepository, settings,
            GetDateRangeUseCase(GetDateRangeByTypeUseCase(dates), dates), repository,
        )
        val job = backgroundScope.launch { useCase.invoke().collect() }
        runCurrent()
        verify(repository).getAllFilteredTransaction(emptyList(), emptyList(), TransactionType.entries.map { it.ordinal })
        val icon = StoredIcon("account_balance", "#FF0000", "")
        accounts.value = listOf(Account("imported-account", "Imported", AccountType.REGULAR, icon, Date(), Date()))
        categories.value = listOf(Category("imported-category", "Imported", CategoryType.EXPENSE, icon, Date(), Date()))
        runCurrent()
        verify(repository).getAllFilteredTransaction(
            listOf("imported-account"), listOf("imported-category"), TransactionType.entries.map { it.ordinal },
        )
        job.cancel()
    }

    @Test
    fun homeStreamDoesNotDependOnPersistedTransactionFilters() {
        val repository = mock<TransactionRepository>()
        whenever(repository.getAllTransaction()).thenReturn(flowOf(emptyList()))
        val useCase = GetTransactionWithFilterUseCase(mock(), mock(), mock(), mock(), repository)
        useCase.invokeUnfiltered()
        verify(repository).getAllTransaction()
        verifyNoMoreInteractions(repository)
    }
}
