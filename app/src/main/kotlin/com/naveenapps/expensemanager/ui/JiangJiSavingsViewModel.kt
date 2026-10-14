package com.naveenapps.expensemanager.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.datastore.SavingsPlanDataStore
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.SavingsPlan
import com.naveenapps.expensemanager.core.model.SavingsPlanType
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

data class JiangJiSavingsState(
    val legacySavings: List<com.naveenapps.expensemanager.core.database.entity.MonthlySavingEntity> = emptyList(),
    val plans: List<SavingsPlan> = emptyList(),
    val calendarYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val calendarMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1, // 1..12
    val selectedDay: Int = Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
    val allTransactions: List<Transaction> = emptyList(),
    val currency: Currency? = null,
)

class JiangJiSavingsViewModel(
    private val savingsPlanDataStore: SavingsPlanDataStore,
    private val transactionRepository: TransactionRepository,
    getCurrencyUseCase: GetCurrencyUseCase,
    monthlySavingDao: com.naveenapps.expensemanager.core.database.dao.MonthlySavingDao,
) : ViewModel() {

    private val _state = MutableStateFlow(JiangJiSavingsState())
    val state = _state.asStateFlow()

    init {
        monthlySavingDao.observeAllSavings().onEach { savings ->
            _state.update { it.copy(legacySavings = savings) }
        }.launchIn(viewModelScope)
        combine(
            savingsPlanDataStore.plans,
            transactionRepository.getAllTransaction(),
            getCurrencyUseCase.invoke(),
        ) { plans, txs, currency ->
            Triple(plans, txs.orEmpty(), currency)
        }.onEach { (plans, txs, currency) ->
            _state.update {
                it.copy(
                    plans = plans,
                    allTransactions = txs,
                    currency = currency,
                )
            }
        }.launchIn(viewModelScope)
    }

    fun createPlan(
        name: String,
        type: SavingsPlanType,
        targetAmountCents: Long,
        startDate: String,
        endDate: String,
        frequency: String,
        note: String,
    ) {
        viewModelScope.launch {
            val newPlan = SavingsPlan(
                name = name.ifBlank { type.title },
                type = type,
                targetAmountCents = targetAmountCents,
                savedAmountCents = 0L,
                startDate = startDate,
                endDate = endDate,
                frequency = frequency,
                note = note,
            )
            savingsPlanDataStore.savePlan(newPlan)
        }
    }

    fun deposit(planId: String, amountCents: Long, date: String) {
        viewModelScope.launch {
            savingsPlanDataStore.deposit(planId, amountCents, date)
        }
    }

    fun deletePlan(planId: String) {
        viewModelScope.launch {
            savingsPlanDataStore.deletePlan(planId)
        }
    }

    fun previousMonth() {
        _state.update {
            var y = it.calendarYear
            var m = it.calendarMonth - 1
            if (m < 1) {
                m = 12
                y -= 1
            }
            it.copy(calendarYear = y, calendarMonth = m, selectedDay = 1)
        }
    }

    fun nextMonth() {
        _state.update {
            var y = it.calendarYear
            var m = it.calendarMonth + 1
            if (m > 12) {
                m = 1
                y += 1
            }
            it.copy(calendarYear = y, calendarMonth = m, selectedDay = 1)
        }
    }

    fun selectDay(day: Int) {
        _state.update { it.copy(selectedDay = day) }
    }
}
