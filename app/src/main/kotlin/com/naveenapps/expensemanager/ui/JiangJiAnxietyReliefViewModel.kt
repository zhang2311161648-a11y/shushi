package com.naveenapps.expensemanager.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.anxietyrelief.AddAnxietyReliefItemUseCase
import com.naveenapps.expensemanager.core.domain.usecase.anxietyrelief.ArchiveAnxietyReliefItemUseCase
import com.naveenapps.expensemanager.core.domain.usecase.anxietyrelief.DeleteAnxietyReliefItemUseCase
import com.naveenapps.expensemanager.core.domain.usecase.anxietyrelief.GetAnxietyReliefDefaultTargetUseCase
import com.naveenapps.expensemanager.core.domain.usecase.anxietyrelief.GetAnxietyReliefItemsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.anxietyrelief.SetAnxietyReliefDefaultTargetUseCase
import com.naveenapps.expensemanager.core.domain.usecase.anxietyrelief.UpdateAnxietyReliefItemUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.model.AnxietyReliefItem
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID
import kotlin.time.ExperimentalTime
import kotlin.time.Clock

data class JiangJiAnxietyReliefState(
    val items: List<AnxietyReliefItem> = emptyList(),
    val defaultDailyTargetCents: Long = 500L,
    val currency: Currency? = null,
    val editor: JiangJiAnxietyReliefEditorState? = null,
    val defaultTargetDraft: String = "5",
    val defaultTargetError: String? = null,
    val errorMessage: String? = null,
    val isChangingImage: Boolean = false,
    val isDeleting: Boolean = false,
)

data class JiangJiAnxietyReliefEditorState(
    val itemId: String? = null,
    val name: String = "",
    val imageUri: String? = null,
    val price: String = "",
    val purchaseDate: String = todayIsoDate(),
    val targetDailyCost: String = "5",
    val note: String = "",
    val mood: String = "开心 🥰",
    val category: String = "未分类",
    val status: String = "陪伴中",
    val costByUse: Boolean = false,
    val useCount: String = "0",
    val validationError: String? = null,
    val isSaving: Boolean = false,
)

class JiangJiAnxietyReliefViewModel(
    getItemsUseCase: GetAnxietyReliefItemsUseCase,
    getDefaultTargetUseCase: GetAnxietyReliefDefaultTargetUseCase,
    getCurrencyUseCase: GetCurrencyUseCase,
    private val addItemUseCase: AddAnxietyReliefItemUseCase,
    private val updateItemUseCase: UpdateAnxietyReliefItemUseCase,
    private val archiveItemUseCase: ArchiveAnxietyReliefItemUseCase,
    private val deleteItemUseCase: DeleteAnxietyReliefItemUseCase,
    private val setDefaultTargetUseCase: SetAnxietyReliefDefaultTargetUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(JiangJiAnxietyReliefState())
    val state = _state.asStateFlow()
    private val usageMutex = Mutex()

    init {
        combine(
            getItemsUseCase(),
            getDefaultTargetUseCase(),
            getCurrencyUseCase.invoke(),
        ) { items, target, currency -> Triple(items, target, currency) }
            .onEach { (items, target, currency) ->
                _state.update {
                    it.copy(
                        items = items,
                        defaultDailyTargetCents = target,
                        currency = currency,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun openNew() {
        val target = _state.value.defaultDailyTargetCents
        _state.update {
            it.copy(
                editor = JiangJiAnxietyReliefEditorState(
                    targetDailyCost = centsToInput(target),
                ),
                errorMessage = null,
            )
        }
    }

    fun openEdit(item: AnxietyReliefItem) {
        _state.update {
            it.copy(
                editor = JiangJiAnxietyReliefEditorState(
                    itemId = item.id,
                    name = item.name,
                    imageUri = item.imageUri,
                    price = centsToInput(item.priceCents),
                    purchaseDate = item.purchaseDate,
                    targetDailyCost = centsToInput(item.targetDailyCostCents),
                    note = item.note,
                    mood = item.mood,
                    category = item.category,
                    status = if (item.isArchived || item.isSold) "已归档" else "陪伴中",
                    costByUse = item.costByUse,
                    useCount = item.useCount.toString(),
                ),
                errorMessage = null,
            )
        }
    }

    fun closeEditor() {
        if (_state.value.editor?.isSaving == true) return
        _state.update { it.copy(editor = null) }
    }

    fun updateName(value: String) = updateEditor { copy(name = value, validationError = null) }
    fun updateImageUri(value: String?) = updateEditor { copy(imageUri = value) }
    fun updatePrice(value: String) = updateEditor { copy(price = value, validationError = null) }
    fun updatePurchaseDate(value: String) = updateEditor { copy(purchaseDate = value, validationError = null) }
    fun updateTargetDailyCost(value: String) = updateEditor { copy(targetDailyCost = value, validationError = null) }
    fun updateMood(value: String) = updateEditor { copy(mood = value) }
    fun updateNote(value: String) = updateEditor { copy(note = value) }
    fun updateCategory(value: String) = updateEditor { copy(category = value) }
    fun updateStatus(value: String) = updateEditor { copy(status = value) }
    fun updateCostByUse(value: Boolean) = updateEditor { copy(costByUse = value) }
    fun updateUseCount(value: String) = updateEditor { copy(useCount = value, validationError = null) }

    fun saveEditor() {
        val editor = _state.value.editor ?: return
        if (editor.isSaving) return
        val priceCents = parseCents(editor.price)
        val dateIsValid = runCatching { kotlinx.datetime.LocalDate.parse(editor.purchaseDate) }.isSuccess
        val target = resolveTargetDailyCostCents(editor.targetDailyCost, _state.value.defaultDailyTargetCents)
        val uses = editor.useCount.toLongOrNull()
        val validationError = when {
            editor.name.trim().isBlank() -> "请填写物品名称"
            priceCents == null || priceCents <= 0L -> "请输入大于 0 的价格"
            !dateIsValid -> "请选择有效的购买日期"
            target == null || target <= 0L -> "请输入大于 0 的目标成本"
            uses == null || uses < 0L -> "使用次数须为非负整数"
            else -> null
        }
        if (validationError != null || priceCents == null || target == null || uses == null) {
            _state.update { it.copy(editor = editor.copy(validationError = validationError)) }
            return
        }

        val oldItem = editor.itemId?.let { id -> _state.value.items.firstOrNull { it.id == id } }
        val now = System.currentTimeMillis()
        val moodValue = editor.mood.ifBlank { "开心 🥰" }
        val targetCents = target
        val item = oldItem?.copy(
            name = editor.name.trim(),
            imageUri = editor.imageUri,
            priceCents = priceCents,
            purchaseDate = editor.purchaseDate,
            targetDailyCostCents = targetCents,
            note = editor.note.trim(),
            mood = moodValue,
            updatedAt = now,
        ) ?: AnxietyReliefItem(
            id = UUID.randomUUID().toString(),
            name = editor.name.trim(),
            imageUri = editor.imageUri,
            priceCents = priceCents,
            purchaseDate = editor.purchaseDate,
            targetDailyCostCents = targetCents,
            note = editor.note.trim(),
            mood = moodValue,
            createdAt = now,
            updatedAt = now,
        )

        val savedItem = item.copy(
            category = editor.category.trim().ifBlank { "未分类" },
            isSold = false,
            isArchived = editor.status != "陪伴中",
            costByUse = editor.costByUse,
            useCount = uses,
        )
        _state.update { it.copy(editor = editor.copy(isSaving = true, validationError = null)) }
        viewModelScope.launch {
            val result = if (oldItem == null) addItemUseCase(savedItem) else updateItemUseCase(savedItem)
            when (result) {
                is Resource.Success -> _state.update { it.copy(editor = null, errorMessage = null) }
                is Resource.Error -> _state.update {
                    it.copy(editor = it.editor?.copy(isSaving = false, validationError = "保存失败，请重试"))
                }
            }
        }
    }

    fun archive(item: AnxietyReliefItem, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            when (archiveItemUseCase(item)) {
                is Resource.Success -> {
                    _state.update { it.copy(errorMessage = null) }
                    onComplete()
                }
                is Resource.Error -> _state.update { it.copy(errorMessage = "归档失败，请重试") }
            }
        }
    }

    fun delete(item: AnxietyReliefItem, onComplete: () -> Unit = {}) {
        if (_state.value.isDeleting) return
        _state.update { it.copy(isDeleting = true, errorMessage = null) }
        viewModelScope.launch {
            usageMutex.withLock {
                val deleted = when (val result = deleteItemUseCase(item)) {
                    is Resource.Success -> result.data
                    is Resource.Error -> false
                }
                _state.update { current ->
                    if (deleted) current.copy(
                        items = current.items.filterNot { it.id == item.id },
                        isDeleting = false,
                        errorMessage = null,
                    ) else current.copy(isDeleting = false, errorMessage = "删除失败，请重试")
                }
                if (deleted) onComplete()
            }
        }
    }

    fun openDefaultTargetEditor() {
        _state.update {
            it.copy(
                defaultTargetDraft = centsToInput(it.defaultDailyTargetCents),
                defaultTargetError = null,
            )
        }
    }

    fun updateDefaultTargetDraft(value: String) {
        _state.update { it.copy(defaultTargetDraft = value, defaultTargetError = null) }
    }

    fun saveDefaultTarget(onComplete: () -> Unit = {}) {
        val cents = parseCents(_state.value.defaultTargetDraft)
        if (cents == null || cents <= 0L) {
            _state.update { it.copy(defaultTargetError = "请输入大于 0 的金额") }
            return
        }
        viewModelScope.launch {
            when (setDefaultTargetUseCase(cents)) {
                is Resource.Success -> {
                    _state.update { it.copy(defaultTargetError = null) }
                    onComplete()
                }
                is Resource.Error -> _state.update { it.copy(defaultTargetError = "保存失败，请重试") }
            }
        }
    }

    fun clearError() = _state.update { it.copy(errorMessage = null) }

    fun changeItemImage(itemId: String, imageUri: String) {
        if (_state.value.isChangingImage) return
        _state.update { it.copy(isChangingImage = true, errorMessage = null) }
        viewModelScope.launch {
            usageMutex.withLock {
                val current = _state.value.items.firstOrNull { it.id == itemId }
                if (current == null) {
                    _state.update { it.copy(isChangingImage = false, errorMessage = "未找到该物品") }
                    return@withLock
                }
                val updated = current.copy(imageUri = imageUri, updatedAt = System.currentTimeMillis())
                when (val result = updateItemUseCase(updated)) {
                    is Resource.Success -> _state.update { s ->
                        if (result.data) s.copy(isChangingImage = false, items = s.items.map { if (it.id == itemId) updated else it })
                        else s.copy(isChangingImage = false, errorMessage = "图片保存失败，请重试")
                    }
                    is Resource.Error -> _state.update { it.copy(isChangingImage = false, errorMessage = "图片保存失败，请重试") }
                }
            }
        }
    }

    fun recordUse(item: AnxietyReliefItem) {
        if (!item.costByUse || item.isArchived || item.isSold || item.useCount == Long.MAX_VALUE) return
        viewModelScope.launch {
            usageMutex.withLock {
                val current = _state.value.items.firstOrNull { it.id == item.id } ?: return@withLock
                if (!current.costByUse || current.isArchived || current.isSold || current.useCount == Long.MAX_VALUE) return@withLock
                val updated = current.copy(useCount = current.useCount + 1, updatedAt = System.currentTimeMillis())
                when (updateItemUseCase(updated)) {
                    is Resource.Success -> _state.update { s -> s.copy(items = s.items.map { if (it.id == updated.id) updated else it }) }
                    is Resource.Error -> _state.update { it.copy(errorMessage = "记录使用失败，请重试") }
                }
            }
        }
    }

    private fun updateEditor(update: JiangJiAnxietyReliefEditorState.() -> JiangJiAnxietyReliefEditorState) {
        _state.update { current -> current.copy(editor = current.editor?.let(update)) }
    }

    companion object {
        fun parseCents(value: String): Long? = runCatching {
            BigDecimal(value.trim().replace(',', '.'))
                .movePointRight(2)
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact()
        }.getOrNull()

        fun resolveTargetDailyCostCents(value: String, defaultTargetCents: Long): Long? =
            if (value.isBlank()) {
                defaultTargetCents.takeIf { it > 0L }
            } else {
                parseCents(value)
            }

        fun centsToInput(value: Long): String =
            BigDecimal.valueOf(value, 2).setScale(2, RoundingMode.UNNECESSARY).toPlainString()
    }
}

@OptIn(ExperimentalTime::class)
private fun todayIsoDate(): String = Clock.System.now()
    .toLocalDateTime(TimeZone.currentSystemDefault())
    .date
    .toString()
