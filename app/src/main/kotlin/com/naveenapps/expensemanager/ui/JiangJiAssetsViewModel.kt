package com.naveenapps.expensemanager.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.asset.AddAssetUseCase
import com.naveenapps.expensemanager.core.domain.usecase.asset.DeleteAssetUseCase
import com.naveenapps.expensemanager.core.domain.usecase.asset.UpdateAssetUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.model.Asset
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.AssetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

data class JiangJiAssetsState(
    val assets: List<Asset> = emptyList(),
    val currency: Currency? = null,
    val formattedTotal: String = "0.00",
    val formattedAmounts: Map<String, String> = emptyMap(),
    val editor: JiangJiAssetEditorState? = null,
    val errorMessage: String? = null,
)

data class JiangJiAssetEditorState(
    val assetId: String? = null,
    val name: String = "",
    val amount: String = "",
    val note: String = "",
    val validationError: String? = null,
    val isSaving: Boolean = false,
)

class JiangJiAssetsViewModel(
    private val assetRepository: AssetRepository,
    getCurrencyUseCase: GetCurrencyUseCase,
    private val getFormattedAmountUseCase: GetFormattedAmountUseCase,
    private val addAssetUseCase: AddAssetUseCase,
    private val updateAssetUseCase: UpdateAssetUseCase,
    private val deleteAssetUseCase: DeleteAssetUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(JiangJiAssetsState())
    val state = _state.asStateFlow()

    init {
        combine(
            assetRepository.getAssets(),
            getCurrencyUseCase.invoke(),
        ) { assets, currency ->
            assets to currency
        }.onEach { (assets, currency) ->
            val formattedAmounts = currency?.let { selectedCurrency ->
                assets.associate { asset ->
                    asset.id to getFormattedAmountUseCase(asset.amount, selectedCurrency).amountString.orEmpty()
                }
            } ?: emptyMap()
            val total = assets.sumOf { it.amount }
            _state.update { current ->
                current.copy(
                    assets = assets,
                    currency = currency,
                    formattedTotal = currency?.let { selectedCurrency ->
                        getFormattedAmountUseCase(total, selectedCurrency).amountString.orEmpty()
                    } ?: "0.00",
                    formattedAmounts = formattedAmounts,
                )
            }
        }.launchIn(viewModelScope)
    }

    fun openNewAsset() {
        _state.update {
            it.copy(
                editor = JiangJiAssetEditorState(),
                errorMessage = null,
            )
        }
    }

    fun openEditAsset(asset: Asset) {
        _state.update {
            it.copy(
                editor = JiangJiAssetEditorState(
                    assetId = asset.id,
                    name = asset.name,
                    amount = asset.amount.toString(),
                    note = asset.note,
                ),
                errorMessage = null,
            )
        }
    }

    fun closeEditor() {
        if (_state.value.editor?.isSaving == true) return
        _state.update { it.copy(editor = null) }
    }

    fun updateEditorName(value: String) {
        updateEditor { copy(name = value, validationError = null) }
    }

    fun updateEditorAmount(value: String) {
        updateEditor { copy(amount = value, validationError = null) }
    }

    fun updateEditorNote(value: String) {
        updateEditor { copy(note = value, validationError = null) }
    }

    fun saveEditor() {
        val editor = _state.value.editor ?: return
        val name = editor.name.trim()
        val amount = editor.amount.trim().replace(',', '.').toDoubleOrNull()
        val validationError = when {
            name.isBlank() -> "请填写资产名称"
            amount == null || !amount.isFinite() || amount <= 0.0 -> "请输入大于 0 的金额"
            else -> null
        }

        if (validationError != null || amount == null) {
            _state.update {
                it.copy(editor = editor.copy(validationError = validationError))
            }
            return
        }

        val now = Date()
        val existingAsset = editor.assetId?.let { assetId ->
            _state.value.assets.firstOrNull { it.id == assetId }
        }
        val asset = existingAsset?.copy(
            name = name,
            amount = amount,
            note = editor.note.trim(),
            updatedOn = now,
        ) ?: Asset(
            id = UUID.randomUUID().toString(),
            name = name,
            amount = amount,
            note = editor.note.trim(),
            createdOn = now,
            updatedOn = now,
        )

        _state.update { it.copy(editor = editor.copy(isSaving = true, validationError = null)) }
        viewModelScope.launch {
            val result = if (existingAsset == null) {
                addAssetUseCase(asset)
            } else {
                updateAssetUseCase(asset)
            }
            when (result) {
                is Resource.Success -> {
                    _state.update { it.copy(editor = null, errorMessage = null) }
                }

                is Resource.Error -> {
                    _state.update {
                        it.copy(
                            editor = it.editor?.copy(
                                isSaving = false,
                                validationError = "保存失败，请重试",
                            ),
                        )
                    }
                }
            }
        }
    }

    fun deleteAsset(asset: Asset) {
        viewModelScope.launch {
            when (val result = deleteAssetUseCase(asset)) {
                is Resource.Success -> _state.update { it.copy(errorMessage = null) }
                is Resource.Error -> _state.update { it.copy(errorMessage = "删除失败，请重试") }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    private fun updateEditor(update: JiangJiAssetEditorState.() -> JiangJiAssetEditorState) {
        _state.update { current ->
            current.copy(editor = current.editor?.let(update))
        }
    }
}
