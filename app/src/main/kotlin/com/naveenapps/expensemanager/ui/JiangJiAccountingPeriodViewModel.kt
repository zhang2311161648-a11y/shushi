package com.naveenapps.expensemanager.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.accountingperiod.ObserveAccountingPeriodUseCase
import com.naveenapps.expensemanager.core.domain.usecase.accountingperiod.AccountingPeriodResolver
import com.naveenapps.expensemanager.core.model.AccountingPeriod
import com.naveenapps.expensemanager.core.model.AccountingPeriodMode
import com.naveenapps.expensemanager.core.model.AccountingPeriodSettings
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.SalaryCycle
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.repository.AccountingPeriodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class JiangJiAccountingPeriodState(
    val settings: AccountingPeriodSettings = AccountingPeriodSettings(),
    val currentPeriod: AccountingPeriod? = null,
    val salaryCycles: List<SalaryCycle> = emptyList(),
    val fixedStartDayText: String = "1",
    val pendingMode: AccountingPeriodMode? = null,
    val showSalaryCycleEditor: Boolean = false,
    val salaryCycleDate: LocalDate = LocalDate.now(),
    val salaryCycleNote: String = "",
    val showDatePicker: Boolean = false,
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
)

class JiangJiAccountingPeriodViewModel(
    private val repository: AccountingPeriodRepository,
    observeAccountingPeriod: ObserveAccountingPeriodUseCase,
    private val navigator: AppComposeNavigator,
) : ViewModel() {

    private val _state = MutableStateFlow(JiangJiAccountingPeriodState())
    val state = _state.asStateFlow()

    init {
        combine(
            repository.observeSettings(),
            repository.observeSalaryCycles(),
            observeAccountingPeriod(),
        ) { settings, salaryCycles, currentPeriod ->
            _state.update {
                it.copy(
                    settings = settings,
                    currentPeriod = currentPeriod,
                    salaryCycles = salaryCycles,
                    fixedStartDayText = settings.fixedStartDay.toString(),
                )
            }
        }.launchIn(viewModelScope)
    }

    fun requestModeChange(mode: AccountingPeriodMode) {
        if (mode == _state.value.settings.mode) return
        _state.update { it.copy(pendingMode = mode, errorMessage = null) }
    }

    fun cancelModeChange() {
        _state.update { it.copy(pendingMode = null) }
    }

    fun confirmModeChange() {
        val pendingMode = _state.value.pendingMode ?: return
        saveSettings(_state.value.settings.copy(mode = pendingMode)) {
            _state.update { it.copy(pendingMode = null) }
        }
    }

    fun updateFixedStartDay(value: String) {
        val filtered = value.filter(Char::isDigit).take(2)
        _state.update { it.copy(fixedStartDayText = filtered, errorMessage = null) }
        val day = filtered.toIntOrNull() ?: return
        if (day in 1..31) {
            saveSettings(_state.value.settings.copy(fixedStartDay = day))
        }
    }

    fun openSalaryCycleEditor() {
        _state.update {
            it.copy(
                showSalaryCycleEditor = true,
                salaryCycleDate = LocalDate.now(),
                salaryCycleNote = "",
                showDatePicker = false,
                errorMessage = null,
            )
        }
    }

    fun closeSalaryCycleEditor() {
        if (!_state.value.isSaving) {
            _state.update { it.copy(showSalaryCycleEditor = false, showDatePicker = false) }
        }
    }

    fun showDatePicker() {
        _state.update { it.copy(showDatePicker = true) }
    }

    fun hideDatePicker() {
        _state.update { it.copy(showDatePicker = false) }
    }

    fun selectSalaryCycleDate(date: LocalDate) {
        _state.update { it.copy(salaryCycleDate = date, showDatePicker = false, errorMessage = null) }
    }

    fun updateSalaryCycleNote(note: String) {
        _state.update { it.copy(salaryCycleNote = note, errorMessage = null) }
    }

    fun saveSalaryCycle() {
        val current = _state.value
        val today = LocalDate.now()
        if (current.salaryCycleDate.isAfter(today)) {
            _state.update { it.copy(errorMessage = "周期开始日期不能晚于今天。") }
            return
        }
        _state.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            val result = repository.addSalaryCycle(
                SalaryCycle(
                    startDate = current.salaryCycleDate,
                    note = current.salaryCycleNote.trim().ifBlank { null },
                    createdAt = System.currentTimeMillis(),
                ),
            )
            when (result) {
                is Resource.Success -> _state.update {
                    it.copy(showSalaryCycleEditor = false, showDatePicker = false, isSaving = false)
                }

                is Resource.Error -> _state.update {
                    it.copy(isSaving = false, errorMessage = result.exception.message ?: "保存失败，请重试")
                }
            }
        }
    }

    fun deleteSalaryCycle(cycle: SalaryCycle) {
        viewModelScope.launch {
            when (val result = repository.deleteSalaryCycle(cycle)) {
                is Resource.Success -> Unit
                is Resource.Error -> _state.update {
                    it.copy(errorMessage = result.exception.message ?: "删除失败，请重试")
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    fun closePage() {
        navigator.popBackStack()
    }

    private fun saveSettings(
        settings: AccountingPeriodSettings,
        onSuccess: () -> Unit = {},
    ) {
        viewModelScope.launch {
            when (val result = repository.saveSettings(settings)) {
                is Resource.Success -> onSuccess()
                is Resource.Error -> _state.update {
                    it.copy(errorMessage = result.exception.message ?: "保存失败，请重试")
                }
            }
        }
    }
}
