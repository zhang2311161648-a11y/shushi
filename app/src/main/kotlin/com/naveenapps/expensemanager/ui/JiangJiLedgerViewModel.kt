package com.naveenapps.expensemanager.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.ledger.ArchiveLedgerUseCase
import com.naveenapps.expensemanager.core.domain.usecase.ledger.CreateLedgerUseCase
import com.naveenapps.expensemanager.core.domain.usecase.ledger.ObserveCurrentLedgerUseCase
import com.naveenapps.expensemanager.core.domain.usecase.ledger.ObserveLedgersUseCase
import com.naveenapps.expensemanager.core.domain.usecase.ledger.RenameLedgerUseCase
import com.naveenapps.expensemanager.core.domain.usecase.ledger.SwitchLedgerUseCase
import com.naveenapps.expensemanager.core.model.Ledger
import com.naveenapps.expensemanager.core.model.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class JiangJiLedgerState(
    val ledgers: List<Ledger> = emptyList(),
    val currentLedgerId: String? = null,
    val showEditor: Boolean = false,
    val editorName: String = "",
    val editingLedgerId: String? = null,
    val archiveLedgerId: String? = null,
    val isSaving: Boolean = false,
    val feedbackMessage: String? = null,
    val errorMessage: String? = null,
)

class JiangJiLedgerViewModel(
    observeLedgers: ObserveLedgersUseCase,
    observeCurrentLedger: ObserveCurrentLedgerUseCase,
    private val createLedger: CreateLedgerUseCase,
    private val renameLedger: RenameLedgerUseCase,
    private val switchLedger: SwitchLedgerUseCase,
    private val archiveLedger: ArchiveLedgerUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(JiangJiLedgerState())
    val state = _state.asStateFlow()

    init {
        observeLedgers().onEach { ledgers ->
            _state.update { it.copy(ledgers = ledgers) }
        }.launchIn(viewModelScope)
        observeCurrentLedger().onEach { ledger ->
            _state.update { it.copy(currentLedgerId = ledger?.id) }
        }.launchIn(viewModelScope)
    }

    fun openCreateEditor() {
        _state.update {
            it.copy(
                editorName = "",
                editingLedgerId = null,
                showEditor = true,
                errorMessage = null,
            )
        }
    }

    fun openRenameEditor(ledger: Ledger) {
        _state.update {
            it.copy(
                editorName = ledger.name,
                editingLedgerId = ledger.id,
                showEditor = true,
                errorMessage = null,
            )
        }
    }

    fun closeEditor() {
        if (!_state.value.isSaving) {
            _state.update { it.copy(editorName = "", editingLedgerId = null, showEditor = false, errorMessage = null) }
        }
    }

    fun updateEditorName(name: String) {
        _state.update { it.copy(editorName = name, errorMessage = null) }
    }

    fun saveEditor() {
        val current = _state.value
        if (current.editorName.trim().isBlank()) {
            _state.update { it.copy(errorMessage = "账本名称不能为空") }
            return
        }
        _state.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            val result = current.editingLedgerId?.let { ledgerId ->
                renameLedger(ledgerId, current.editorName)
            } ?: createLedger(current.editorName)
            when (result) {
                is Resource.Success -> _state.update {
                    it.copy(
                        isSaving = false,
                        editorName = "",
                        editingLedgerId = null,
                        showEditor = false,
                        feedbackMessage = if (current.editingLedgerId == null) "账本已创建" else "账本名称已更新",
                    )
                }
                is Resource.Error -> _state.update {
                    it.copy(isSaving = false, errorMessage = result.exception.message ?: "保存失败，请重试")
                }
            }
        }
    }

    fun switchTo(ledger: Ledger) {
        if (ledger.id == _state.value.currentLedgerId) return
        viewModelScope.launch {
            when (val result = switchLedger(ledger.id)) {
                is Resource.Success -> _state.update { it.copy(feedbackMessage = "已切换到账本：${ledger.name}") }
                is Resource.Error -> _state.update { it.copy(errorMessage = result.exception.message ?: "切换失败，请重试") }
            }
        }
    }

    fun requestArchive(ledger: Ledger) {
        _state.update { it.copy(archiveLedgerId = ledger.id, errorMessage = null) }
    }

    fun dismissArchive() {
        _state.update { it.copy(archiveLedgerId = null) }
    }

    fun confirmArchive() {
        val ledgerId = _state.value.archiveLedgerId ?: return
        viewModelScope.launch {
            when (val result = archiveLedger(ledgerId)) {
                is Resource.Success -> _state.update {
                    it.copy(archiveLedgerId = null, feedbackMessage = "账本已归档")
                }
                is Resource.Error -> _state.update {
                    it.copy(archiveLedgerId = null, errorMessage = result.exception.message ?: "归档失败，请重试")
                }
            }
        }
    }

    fun clearMessage() {
        _state.update { it.copy(feedbackMessage = null, errorMessage = null) }
    }
}
