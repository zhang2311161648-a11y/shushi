package com.naveenapps.expensemanager.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.common.utils.toMonthAndYearKey
import com.naveenapps.expensemanager.core.common.utils.toYear
import com.naveenapps.expensemanager.core.domain.usecase.account.GetAllAccountsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.accountingperiod.ObserveAccountingPeriodUseCase
import com.naveenapps.expensemanager.core.domain.usecase.budget.GetBudgetsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.budget.budgetName
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange.GetDateRangeUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.GetTransactionGroupByCategoryUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.GetTransactionWithFilterUseCase
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.BudgetPeriod
import com.naveenapps.expensemanager.core.model.CategoryTransactionState
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.ExpenseFlowState
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.model.toAmountDouble
import com.naveenapps.expensemanager.core.model.getAvailableCreditLimit
import com.naveenapps.expensemanager.core.model.toAccountUiModel
import com.naveenapps.expensemanager.core.model.toTransactionUIModel
import com.naveenapps.expensemanager.core.model.effectiveBaseAmountCents
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import com.naveenapps.expensemanager.core.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date


class DashboardViewModel(
    getTransactionWithFilterUseCase: GetTransactionWithFilterUseCase,
    getCurrencyUseCase: GetCurrencyUseCase,
    getFormattedAmountUseCase: GetFormattedAmountUseCase,
    getAllAccountsUseCase: GetAllAccountsUseCase,
    getTransactionGroupByCategoryUseCase: GetTransactionGroupByCategoryUseCase,
    getBudgetsUseCase: GetBudgetsUseCase,
    appCoroutineDispatchers: AppCoroutineDispatchers,
    getDateRangeUseCase: GetDateRangeUseCase,
    settingsRepository: SettingsRepository,
    private val appComposeNavigator: AppComposeNavigator,
    observeAccountingPeriodUseCase: ObserveAccountingPeriodUseCase? = null,
) : ViewModel() {

    private val _state = MutableStateFlow(
        DashboardState(
            expenseFlowState = ExpenseFlowState(),
            transactions = emptyList(),
            budgets = emptyList(),
            accounts = emptyList(),
            categoryTransactionState = CategoryTransactionState(
                pieChartData = listOf(),
                totalAmount = Amount(0.0),
                categoryTransactions = emptyList(),
                categoryType = CategoryType.EXPENSE,
            ),
            transactionPeriod = ""
        )
    )
    val state = _state.asStateFlow()

    init {
        combine(
            getCurrencyUseCase.invoke(),
            getTransactionWithFilterUseCase.invokeUnfiltered(),
            getAllAccountsUseCase.invoke(),
            getDateRangeUseCase.invoke(),
            observeAccountingPeriodUseCase?.invoke() ?: kotlinx.coroutines.flow.flowOf(null),
        ) { currency, transactions, accounts, _, accountingPeriod ->

            val today = LocalDate.now()
            val periodStart = accountingPeriod?.takeIf { it.hasDefinedStart }?.startDate ?: today.withDayOfMonth(1)
            val periodEnd = accountingPeriod?.takeIf { it.hasDefinedStart }?.endExclusiveDate ?: today.withDayOfMonth(1).plusMonths(1)
            val zone = ZoneId.systemDefault()
            val startMillis = periodStart.atStartOfDay(zone).toInstant().toEpochMilli()
            val endMillis = periodEnd.atStartOfDay(zone).toInstant().toEpochMilli()
            val summaryTransactions = transactions.orEmpty().filter {
                it.createdOn.time >= startMillis && it.createdOn.time < endMillis
            }
            val recentStart = today.minusDays(4).atStartOfDay(zone).toInstant().toEpochMilli()
            val recentEnd = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
            val filteredTransactions = (transactions.orEmpty().filter {
                it.createdOn.time >= recentStart && it.createdOn.time < recentEnd
            }.sortedByDescending { it.createdOn.time }.map {
                it.toTransactionUIModel(
                    amount = getFormattedAmountUseCase.invoke(
                        it.effectiveBaseAmountCents().toAmountDouble(),
                        currency,
                    ),
                    secondaryAmount = it.currencySnapshot
                        ?.takeIf { snapshot -> snapshot.originalCurrency.code != snapshot.baseCurrency.code }
                        ?.let { snapshot ->
                            getFormattedAmountUseCase.invoke(
                                snapshot.originalAmountCents.toAmountDouble(),
                                snapshot.originalCurrency,
                            )
                        },
                ).copy(
                    balanceAfterTransaction = it.balanceAfterTransactionCents?.let { cents ->
                        getFormattedAmountUseCase.invoke(cents.toAmountDouble(), currency)
                    },
                )
            })

            val accountsConverted = accounts.map {
                it.toAccountUiModel(
                    getFormattedAmountUseCase.invoke(
                        it.amount,
                        currency,
                    ),
                    if (it.type == AccountType.CREDIT) {
                        getFormattedAmountUseCase.invoke(
                            it.getAvailableCreditLimit(),
                            currency
                        )
                    } else {
                        null
                    }
                )
            }

            val incomeValue = summaryTransactions.filter { it.type == TransactionType.INCOME }.sumOf {
                it.effectiveBaseAmountCents().toAmountDouble()
            }

            val expenseValue = summaryTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf {
                it.effectiveBaseAmountCents().toAmountDouble()
            }


            _state.update {
                it.copy(
                    expenseFlowState = it.expenseFlowState.copy(
                        income = getFormattedAmountUseCase.invoke(
                            incomeValue,
                            currency,
                        ).amountString.orEmpty(),
                        expense = getFormattedAmountUseCase.invoke(
                            expenseValue,
                            currency,
                        ).amountString.orEmpty(),
                        balance = getFormattedAmountUseCase.invoke(
                            (incomeValue - expenseValue),
                            currency,
                        ).amountString.orEmpty(),
                    ),
                    transactions = filteredTransactions,
                    accounts = accountsConverted,
                    totalAccountBalance = getFormattedAmountUseCase.invoke(
                        accounts.sumOf { it.currentBalanceCents }.toAmountDouble(),
                        currency,
                    ).amountString.orEmpty(),
                    transactionPeriod = "%02d.%02d - %02d.%02d".format(periodStart.monthValue, periodStart.dayOfMonth, periodEnd.minusDays(1).monthValue, periodEnd.minusDays(1).dayOfMonth),
                    isCustomAccountingPeriod = accountingPeriod?.shouldUseCustomLabel() == true,
                )
            }
        }.flowOn(appCoroutineDispatchers.computation)
            .launchIn(viewModelScope)

        getTransactionGroupByCategoryUseCase.invoke(CategoryType.EXPENSE).onEach {
            val categoryTransaction = it.copy(
                pieChartData = it.pieChartData.take(4),
                categoryTransactions = it.categoryTransactions.take(4),
            )
            _state.update { it.copy(categoryTransactionState = categoryTransaction) }
        }.launchIn(viewModelScope)

        combine(
            getBudgetsUseCase.invoke(),
            getDateRangeUseCase.invoke(),
            observeAccountingPeriodUseCase?.invoke() ?: kotlinx.coroutines.flow.flowOf(null),
        ) { allBudgets, _, accountingPeriod ->
            val activeMonth = Date().toMonthAndYearKey()
            val filtered = if (activeMonth != null) {
                // "Active Budgets" means: the monthly budget covering the month being viewed,
                // together with the yearly budget covering the current year — both can be active
                // at once (e.g. a monthly grocery budget alongside a yearly travel budget).
                val currentYear = Date().toYear()
                allBudgets.filter { budget ->
                    when (budget.periodType) {
                        BudgetPeriod.MONTHLY -> if (
                            budget.accountingPeriodStartDate != null && accountingPeriod != null &&
                            accountingPeriod.hasDefinedStart
                        ) {
                            budget.accountingPeriodStartDate == accountingPeriod.startDate &&
                                budget.accountingPeriodEndExclusiveDate == accountingPeriod.endExclusiveDate
                        } else {
                            budget.selectedMonth == activeMonth
                        }
                        BudgetPeriod.YEARLY -> budget.selectedMonth == currentYear
                    }
                }
            } else {
                allBudgets
            }
            // The "create a budget for this month" nudge is about monthly budgets specifically —
            // an active yearly budget shouldn't suppress it.
            val hasMonthlyBudget = filtered.any { it.periodType == BudgetPeriod.MONTHLY }
            val showCreateBudgetForMonth = if (activeMonth != null && !hasMonthlyBudget) {
                if (accountingPeriod?.shouldUseCustomLabel() == true) {
                    "This Period Budget"
                } else {
                    budgetName(activeMonth)
                }
            } else {
                null
            }
            _state.update { it.copy(budgets = filtered, showCreateBudgetForMonth = showCreateBudgetForMonth) }
        }.flowOn(appCoroutineDispatchers.computation)
            .launchIn(viewModelScope)

        settingsRepository.getHomeSummaryCompact().onEach { compact ->
            _state.update { it.copy(isCompactSummary = compact) }
        }.launchIn(viewModelScope)
    }

    private fun openSettings() {
        appComposeNavigator.navigate(ExpenseManagerScreens.Settings)
    }

    private fun openAccountList() {
        appComposeNavigator.navigate(ExpenseManagerScreens.AccountList)
    }

    private fun openAccountCreate(accountId: String?) {
        appComposeNavigator.navigate(ExpenseManagerScreens.AccountCreate(accountId))
    }

    private fun openBudgetList() {
        appComposeNavigator.navigate(ExpenseManagerScreens.BudgetList)
    }

    private fun openBudgetCreate() {
        appComposeNavigator.navigate(ExpenseManagerScreens.BudgetCreate(null))
    }

    private fun openBudgetDetails(budgetId: String?) {
        appComposeNavigator.navigate(ExpenseManagerScreens.BudgetDetails(budgetId))
    }

    private fun openTransactionList() {
        appComposeNavigator.navigate(ExpenseManagerScreens.TransactionList)
    }

    private fun openTransactionCreate(transactionId: String? = null) {
        appComposeNavigator.navigate(ExpenseManagerScreens.TransactionCreate(transactionId))
    }

    fun processAction(action: DashboardAction) {
        when (action) {
            is DashboardAction.OpenAccountEdit -> openAccountCreate(action.account.id)
            DashboardAction.OpenAccountList -> openAccountList()
            is DashboardAction.OpenBudgetDetails -> openBudgetDetails(action.budgetUiModel.id)
            DashboardAction.OpenBudgetList -> openBudgetList()
            DashboardAction.OpenBudgetCreate -> openBudgetCreate()
            DashboardAction.OpenSettings -> openSettings()
            is DashboardAction.OpenTransactionEdit -> openTransactionCreate(action.transaction?.id)
            DashboardAction.OpenTransactionList -> openTransactionList()
        }
    }

}

private fun com.naveenapps.expensemanager.core.model.AccountingPeriod.shouldUseCustomLabel(): Boolean =
    hasDefinedStart && mode != com.naveenapps.expensemanager.core.model.AccountingPeriodMode.CALENDAR_MONTH
