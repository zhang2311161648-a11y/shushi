package com.naveenapps.expensemanager.feature.transaction.create

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.account.GetAllAccountsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.category.GetAllCategoryUseCase
import com.naveenapps.expensemanager.core.domain.usecase.categorysuggestion.CategorySuggestionEngine
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetDefaultCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetExchangeRateUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.SaveManualExchangeRateUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.AddTransactionUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.DeleteTransactionUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.FindTransactionByIdUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.UpdateTransactionUseCase
import com.naveenapps.expensemanager.core.repository.FeedbackRepository
import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.CategorySuggestionMatchType
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.core.model.TextFieldValue
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.model.ExchangeRateSnapshot
import com.naveenapps.expensemanager.core.model.TransactionCurrencySnapshot
import com.naveenapps.expensemanager.core.model.convertCents
import com.naveenapps.expensemanager.core.model.toCents
import com.naveenapps.expensemanager.core.model.toExchangeRateMicros
import com.naveenapps.expensemanager.core.model.getAvailableCreditLimit
import com.naveenapps.expensemanager.core.model.isExpense
import com.naveenapps.expensemanager.core.model.isIncome
import com.naveenapps.expensemanager.core.model.isLikelyStableSuggestionKeyword
import com.naveenapps.expensemanager.core.model.isTransfer
import com.naveenapps.expensemanager.core.model.normalizeSuggestionText
import com.naveenapps.expensemanager.core.model.toAccountUiModel
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerArgsNames
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import com.naveenapps.expensemanager.core.repository.CategorySuggestionRuleRepository
import com.naveenapps.expensemanager.core.repository.CategorySuggestionSettingsRepository
import com.naveenapps.expensemanager.core.repository.ImageStorageRepository
import com.naveenapps.expensemanager.core.repository.SettingsRepository
import com.naveenapps.expensemanager.core.settings.domain.repository.NumberFormatRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.UUID


class TransactionCreateViewModel(
    savedStateHandle: SavedStateHandle,
    getCurrencyUseCase: GetCurrencyUseCase,
    getAllAccountsUseCase: GetAllAccountsUseCase,
    getAllCategoryUseCase: GetAllCategoryUseCase,
    getDefaultCurrencyUseCase: GetDefaultCurrencyUseCase,
    private val getFormattedAmountUseCase: GetFormattedAmountUseCase,
    private val findTransactionByIdUseCase: FindTransactionByIdUseCase,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val settingsRepository: SettingsRepository,
    private val imageStorageRepository: ImageStorageRepository,
    private val appComposeNavigator: AppComposeNavigator,
    private val numberFormatRepository: NumberFormatRepository,
    private val feedbackRepository: FeedbackRepository,
    private val categorySuggestionRuleRepository: CategorySuggestionRuleRepository? = null,
    private val categorySuggestionSettingsRepository: CategorySuggestionSettingsRepository? = null,
    private val categorySuggestionEngine: CategorySuggestionEngine = CategorySuggestionEngine(),
    private val getExchangeRateUseCase: GetExchangeRateUseCase? = null,
    private val saveManualExchangeRateUseCase: SaveManualExchangeRateUseCase? = null,
) : ViewModel() {

    private val sessionCreatedAttachmentPaths = mutableListOf<String>()
    private var suggestionJob: Job? = null
    private var categoryWasManuallySelected = false
    private var currencyWasManuallySelected = false
    private var categorySuggestionEnabled = true

    private val _event = Channel<TransactionCreateEvent>()
    val event = _event.receiveAsFlow()

    private val transactionType = MutableStateFlow(TransactionType.EXPENSE)

    // Tracks whether both the accounts and categories flows have emitted their first value,
    // so we only populate an editing transaction once both are ready.
    private val syncState = MutableStateFlow(
        TransactionCreateInitSetupState(
            isCategorySyncCompleted = false,
            isAccountSyncCompleted = false,
        )
    )

    private val _state = MutableStateFlow(
        TransactionCreateState(
            amount = TextFieldValue(
                value = numberFormatRepository.formatForEditing(0.0),
                valueError = false,
                onValueChange = this::setAmountOnChange
            ),
            notes = TextFieldValue(
                value = "",
                valueError = false,
                onValueChange = this::setNotes
            ),
            dateTime = Date(),
            transactionType = TransactionType.EXPENSE,
            currency = getDefaultCurrencyUseCase.invoke(),
            selectedCurrency = getDefaultCurrencyUseCase.invoke(),
            selectedCategory = defaultCategory,
            selectedFromAccount = defaultAccount,
            selectedToAccount = defaultAccount,
            accounts = emptyList(),
            categories = emptyList(),
            showDeleteButton = false,
            showDeleteDialog = false,
            showCategorySelection = false,
            showAccountSelection = false,
            showCurrencySelection = false,
            showNumberPad = false,
            showTimeSelection = false,
            showDateSelection = false,
            accountSelection = AccountSelection.FROM_ACCOUNT
        )
    )
    var state = _state.asStateFlow()

    // Non-null only when editing an existing transaction.
    private var editingTransaction: Transaction? = null

    init {
        observeAccountsAndCurrency(getCurrencyUseCase, getAllAccountsUseCase)
        observeCategories(getAllCategoryUseCase)
        observeCategorySuggestionSettings()
        observeUserCategorySuggestionRules()
        loadTransactionWhenReady(savedStateHandle)
    }

    // region Observers

    private fun observeAccountsAndCurrency(
        getCurrencyUseCase: GetCurrencyUseCase,
        getAllAccountsUseCase: GetAllAccountsUseCase,
    ) {
        combine(
            getCurrencyUseCase.invoke(),
            getAllAccountsUseCase.invoke(),
        ) { currency, accounts ->
            val mappedAccounts = mapAccountsToUiModels(accounts, currency)
            val defaultAccountId = settingsRepository.getDefaultAccount().firstOrNull()
            val selectedAccount = mappedAccounts.find { it.id == defaultAccountId }
                ?: mappedAccounts.firstOrNull()
                ?: defaultAccount
            _state.update {
                it.copy(
                    currency = currency,
                    selectedCurrency = if (currencyWasManuallySelected) {
                        it.selectedCurrency
                    } else {
                        currency
                    },
                    accounts = mappedAccounts,
                    selectedFromAccount = selectedAccount,
                    selectedToAccount = selectedAccount,
                )
            }
            syncState.update { it.copy(isAccountSyncCompleted = true) }
        }.launchIn(viewModelScope)
    }

    private fun observeCategories(getAllCategoryUseCase: GetAllCategoryUseCase) {
        combine(
            transactionType,
            getAllCategoryUseCase.invoke(),
            settingsRepository.getDefaultIncomeCategory(),
            settingsRepository.getDefaultExpenseCategory()
        ) { type, categories, defaultIncomeCategory, defaultExpenseCategory ->
            val filteredCategories = categories.filter { category ->
                if (type.isIncome()) category.type.isIncome() else category.type.isExpense()
            }
            val preferredCategoryId = when (type) {
                TransactionType.INCOME -> defaultIncomeCategory
                TransactionType.EXPENSE -> defaultExpenseCategory
                TransactionType.TRANSFER -> null
            }
            val matchedCategory = filteredCategories.find { it.id == preferredCategoryId }

            _state.update {
                it.copy(
                    transactionType = type,
                    categories = filteredCategories,
                    selectedCategory = matchedCategory ?: filteredCategories.firstOrNull()
                    ?: defaultCategory,
                )
            }
            categorySuggestionEngine.updateActiveCategories(filteredCategories)
            scheduleCategorySuggestion(_state.value.notes.value, filteredCategories)
            syncState.update { it.copy(isCategorySyncCompleted = true) }
        }.launchIn(viewModelScope)
    }

    private fun observeCategorySuggestionSettings() {
        categorySuggestionSettingsRepository?.isEnabled()?.onEach { enabled ->
            categorySuggestionEnabled = enabled
            _state.update { it.copy(isCategorySuggestionEnabled = enabled) }
            if (!enabled) {
                suggestionJob?.cancel()
                _state.update {
                    it.copy(categorySuggestions = emptyList(), categorySuggestionResolved = false)
                }
            } else {
                scheduleCategorySuggestion(_state.value.notes.value, _state.value.categories)
            }
        }?.launchIn(viewModelScope)
    }

    private fun observeUserCategorySuggestionRules() {
        categorySuggestionRuleRepository?.observeUserRules()?.onEach { rules ->
            categorySuggestionEngine.updateUserRules(rules)
            scheduleCategorySuggestion(_state.value.notes.value, _state.value.categories)
        }?.launchIn(viewModelScope)
    }

    private fun scheduleCategorySuggestion(
        text: String,
        categories: List<Category>,
    ) {
        suggestionJob?.cancel()
        if (!categorySuggestionEnabled || text.isBlank() || categories.isEmpty()) {
            _state.update {
                it.copy(categorySuggestions = emptyList(), categorySuggestionResolved = false)
            }
            return
        }

        suggestionJob = viewModelScope.launch {
            delay(SUGGESTION_DEBOUNCE_MS)
            val transactionType = _state.value.transactionType
            val suggestions = categorySuggestionEngine.suggest(text, categories)
                .filter { suggestion ->
                    categories.firstOrNull { it.id == suggestion.categoryId }
                        ?.type == transactionType.toCategoryType()
                }
            _state.update { current ->
                val selectedCategory = if (!categoryWasManuallySelected) {
                    suggestions.firstOrNull { it.confidence >= AUTO_SELECT_CONFIDENCE }
                        ?.categoryId
                        ?.let { id -> categories.firstOrNull { it.id == id } }
                        ?: current.selectedCategory
                } else {
                    current.selectedCategory
                }
                current.copy(
                    categorySuggestions = suggestions.take(MAX_SUGGESTIONS),
                    selectedCategory = selectedCategory,
                    categorySuggestionResolved = true,
                )
            }
        }
    }

    private fun loadTransactionWhenReady(savedStateHandle: SavedStateHandle) {
        val transactionId = savedStateHandle.get<String>(ExpenseManagerArgsNames.ID) ?: return
        viewModelScope.launch {
            syncState.first { it.isAccountSyncCompleted && it.isCategorySyncCompleted }
            loadEditingTransaction(transactionId)
        }
    }

    // endregion

    fun createImageCaptureUri(): Uri = imageStorageRepository.createImageCaptureUri()

    // region Transaction load (edit mode)

    private suspend fun loadEditingTransaction(transactionId: String) {
        when (val response = findTransactionByIdUseCase.invoke(transactionId)) {
            is Resource.Error -> Unit
            is Resource.Success -> {
                val transaction = response.data
                editingTransaction = transaction
                categoryWasManuallySelected = true
                transactionType.value = transaction.type
                _state.update { current ->
                    current.copy(
                        amount = current.amount.copy(
                            value = numberFormatRepository.formatForEditing(transaction.amount.amount)
                        ),
                        selectedCurrency = transaction.currencySnapshot?.originalCurrency
                            ?: transaction.amount.currency
                            ?: current.currency,
                        transactionType = transaction.type,
                        dateTime = transaction.createdOn,
                        notes = current.notes.copy(value = transaction.notes),
                        selectedCategory = transaction.category,
                        selectedFromAccount = transaction.fromAccount.toAccountUiModel(
                            getFormattedAmountUseCase.invoke(
                                transaction.fromAccount.amount,
                                current.currency
                            ),
                        ),
                        selectedToAccount = transaction.toAccount?.let { toAccount ->
                            toAccount.toAccountUiModel(
                                getFormattedAmountUseCase.invoke(
                                    toAccount.amount,
                                    current.currency
                                ),
                            )
                        } ?: defaultAccount,
                        showDeleteButton = true,
                        attachments = transaction.attachments,
                    )
                }
            }
        }
    }

    // endregion

    // region Save / Delete

    private fun save() {
        val currentState = _state.value
        val amountText = currentState.amount.value
        val amountValue = numberFormatRepository.parseToDouble(amountText)

        if (amountText.isBlank() || amountValue == null || amountValue <= 0.0) {
            _state.update { it.copy(amount = it.amount.copy(valueError = true)) }
            return
        }

        if (currentState.transactionType.isTransfer() &&
            currentState.selectedFromAccount.id == currentState.selectedToAccount.id
        ) return

        val amountCents = amountValue.toCents()
        viewModelScope.launch {
            when (val rate = resolveRate(currentState, amountCents)) {
                is Resource.Success -> persistTransaction(
                    buildTransactionFromState(currentState, amountValue, rate.data),
                )
                is Resource.Error -> _state.update {
                    it.copy(
                        showExchangeRateDialog = true,
                        exchangeRateError = rate.exception.message,
                    )
                }
            }
        }
    }

    internal fun buildTransactionFromState(
        state: TransactionCreateState,
        amountValue: Double,
        exchangeRate: ExchangeRateSnapshot? = null,
    ): Transaction {
        val originalCurrency = state.selectedCurrency
        val baseCurrency = state.currency
        val originalAmountCents = amountValue.toCents()
        val sameCurrency = originalCurrency.code.isBlank() ||
            baseCurrency.code.isBlank() ||
            originalCurrency.code.equals(baseCurrency.code, ignoreCase = true)
        val rateMicros = exchangeRate?.rateMicros ?: if (sameCurrency) 1_000_000L else 1L
        val baseAmountCents = if (sameCurrency) originalAmountCents
        else convertCents(originalAmountCents, rateMicros)
        return Transaction(
            id = editingTransaction?.id ?: UUID.randomUUID().toString(),
            notes = state.notes.value,
            categoryId = state.selectedCategory.id,
            fromAccountId = state.selectedFromAccount.id,
            toAccountId = if (state.transactionType.isTransfer()) state.selectedToAccount.id else null,
            type = state.transactionType,
            amount = Amount(amountValue, currency = originalCurrency),
            imagePath = "",
            createdOn = state.dateTime,
            updatedOn = Calendar.getInstance().time,
            attachments = state.attachments,
            currencySnapshot = TransactionCurrencySnapshot(
                originalAmountCents = originalAmountCents,
                originalCurrency = originalCurrency,
                baseAmountCents = baseAmountCents,
                baseCurrency = baseCurrency,
                exchangeRateMicros = if (sameCurrency) 1_000_000L else rateMicros,
                exchangeRateSource = exchangeRate?.source ?: "same_currency",
                exchangeRateUpdatedAt = exchangeRate?.updatedAtMillis,
            ),
        )
    }

    private suspend fun resolveRate(
        state: TransactionCreateState,
        amountCents: Long,
    ): Resource<ExchangeRateSnapshot> {
        val originalCurrency = state.selectedCurrency
        val baseCurrency = state.currency
        val sameCurrency = originalCurrency.code.isBlank() ||
            baseCurrency.code.isBlank() ||
            originalCurrency.code.equals(baseCurrency.code, ignoreCase = true)
        if (sameCurrency) {
            return Resource.Success(
                ExchangeRateSnapshot(
                    fromCurrencyCode = originalCurrency.code.ifBlank { baseCurrency.code },
                    toCurrencyCode = baseCurrency.code,
                    rateMicros = 1_000_000L,
                    source = "same_currency",
                    updatedAtMillis = System.currentTimeMillis(),
                ),
            )
        }

        val existingSnapshot = editingTransaction?.currencySnapshot
        if (existingSnapshot != null &&
            existingSnapshot.originalAmountCents == amountCents &&
            existingSnapshot.originalCurrency.code.equals(originalCurrency.code, ignoreCase = true) &&
            existingSnapshot.baseCurrency.code.equals(baseCurrency.code, ignoreCase = true)
        ) {
            return Resource.Success(
                ExchangeRateSnapshot(
                    fromCurrencyCode = originalCurrency.code,
                    toCurrencyCode = baseCurrency.code,
                    rateMicros = existingSnapshot.exchangeRateMicros,
                    source = existingSnapshot.exchangeRateSource,
                    updatedAtMillis = existingSnapshot.exchangeRateUpdatedAt ?: System.currentTimeMillis(),
                ),
            )
        }
        return getExchangeRateUseCase?.invoke(originalCurrency.code, baseCurrency.code)
            ?: Resource.Error(IllegalStateException("Exchange-rate service is not configured"))
    }

    private fun confirmManualExchangeRate() {
        val state = _state.value
        val amountValue = numberFormatRepository.parseToDouble(state.amount.value) ?: return
        val rateMicros = runCatching { state.manualExchangeRate.value.toExchangeRateMicros() }
            .getOrNull()
        if (rateMicros == null) {
            _state.update { it.copy(exchangeRateError = "请输入大于 0 的有效汇率") }
            return
        }
        viewModelScope.launch {
            val result = saveManualExchangeRateUseCase?.invoke(
                state.selectedCurrency.code,
                state.currency.code,
                rateMicros,
            ) ?: Resource.Error(IllegalStateException("Manual exchange-rate service is not configured"))
            when (result) {
                is Resource.Success -> {
                    _state.update { it.copy(showExchangeRateDialog = false, exchangeRateError = null) }
                    persistTransaction(buildTransactionFromState(state, amountValue, result.data))
                }
                is Resource.Error -> _state.update { it.copy(exchangeRateError = result.exception.message) }
            }
        }
    }

    private fun persistTransaction(transaction: Transaction) {
        val isNewTransaction = editingTransaction == null
        val previouslyPersistedAttachments = editingTransaction?.attachments ?: emptyList()
        val stateAtSave = _state.value
        viewModelScope.launch {
            val response = if (editingTransaction != null) {
                updateTransactionUseCase.invoke(transaction)
            } else {
                addTransactionUseCase.invoke(transaction)
            }
            if (response is Resource.Success) {
                (previouslyPersistedAttachments - transaction.attachments.toSet()).forEach {
                    imageStorageRepository.deleteTransactionAttachment(it)
                }
                sessionCreatedAttachmentPaths.removeAll(transaction.attachments)
                discardSessionAttachments()
                learnCategoryFromSuccessfulSave(stateAtSave, transaction)
                if (isNewTransaction) {
                    onNewTransactionCreated()
                }
                closePage()
            }
        }
    }

    // Only ever called after a brand-new transaction (not an edit) is saved successfully.
    // Bumps the running "transactions created" count, and — once someone has logged enough
    // transactions and enough days have passed since install — requests Google Play's in-app
    // review popup exactly once for the life of the install.
    private suspend fun onNewTransactionCreated() {
        feedbackRepository.setTransactionCreated(true)
        if (feedbackRepository.shouldShowFeedbackDialog().first()) {
            // Marked immediately, before we know whether Play actually showed anything —
            // Play never reports that back, so "requested" is the only signal we get.
            feedbackRepository.setFeedbackDialogShown(true)
            _event.send(TransactionCreateEvent.RequestReview)
        }
    }

    private fun deleteTransaction() {
        val transaction = editingTransaction ?: return
        viewModelScope.launch {
            if (deleteTransactionUseCase.invoke(transaction) is Resource.Success) {
                transaction.attachments.forEach {
                    imageStorageRepository.deleteTransactionAttachment(it)
                }
                discardSessionAttachments()
                closePage()
            }
        }
    }

    // endregion

    // region Attachments

    private fun onAttachmentPicked(uri: Uri) {
        viewModelScope.launch {
            val path = imageStorageRepository.saveTransactionAttachment(uri) ?: return@launch
            sessionCreatedAttachmentPaths.add(path)
            _state.update { it.copy(attachments = it.attachments + path) }
        }
    }

    private fun removeAttachment(path: String) {
        _state.update { it.copy(attachments = it.attachments - path) }
    }

    private fun cancelEditing() {
        discardSessionAttachments()
        closePage()
    }

    private fun discardSessionAttachments() {
        sessionCreatedAttachmentPaths.forEach { imageStorageRepository.deleteTransactionAttachment(it) }
        sessionCreatedAttachmentPaths.clear()
    }

    // endregion

    // region State helpers

    private fun mapAccountsToUiModels(
        accounts: List<Account>,
        currency: Currency
    ): List<AccountUiModel> {
        return accounts.map { account ->
            account.toAccountUiModel(
                getFormattedAmountUseCase.invoke(account.amount, currency),
                if (account.type == AccountType.CREDIT) {
                    getFormattedAmountUseCase.invoke(account.getAvailableCreditLimit(), currency)
                } else {
                    null
                }
            )
        }
    }

    private fun setAmountOnChange(amount: String) {
        val amountValue = numberFormatRepository.parseToDouble(amount)
        _state.update {
            it.copy(
                amount = it.amount.copy(
                    value = amount,
                    valueError = amount.isBlank() || amountValue == null || amountValue <= 0.0
                ),
                showNumberPad = false
            )
        }
    }

    private fun setNotes(notes: String) {
        categoryWasManuallySelected = false
        _state.update { it.copy(notes = it.notes.copy(value = notes)) }
        scheduleCategorySuggestion(notes, _state.value.categories)
    }

    private suspend fun learnCategoryFromSuccessfulSave(
        state: TransactionCreateState,
        transaction: Transaction,
    ) {
        val repository = categorySuggestionRuleRepository ?: return
        if (transaction.type.isTransfer() || state.notes.value.isBlank()) return

        val topSuggestion = state.categorySuggestions.firstOrNull()
        if (topSuggestion?.categoryId == transaction.categoryId) {
            // A correct system/user suggestion does not need to create a new learned rule.
            return
        }

        val selectedSuggestion = state.categorySuggestions.firstOrNull {
            it.categoryId == transaction.categoryId
        }
        val pattern = if (topSuggestion?.source == com.naveenapps.expensemanager.core.model.CategorySuggestionRuleSource.SYSTEM) {
            topSuggestion.matchedPattern
        } else {
            selectedSuggestion?.matchedPattern ?: state.notes.value
        }
        val matchType = if (isLikelyStableSuggestionKeyword(pattern)) {
            CategorySuggestionMatchType.CONTAINS
        } else {
            CategorySuggestionMatchType.EXACT
        }

        // A short brand-like input gets both exact and contains rules, while a sentence is
        // exact-only. This is what lets "泡泡玛特盲盒" reuse a learned "泡泡玛特" safely.
        repository.saveUserRule(pattern, transaction.categoryId, matchType)
        if (matchType == CategorySuggestionMatchType.CONTAINS) {
            repository.saveUserRule(
                pattern,
                transaction.categoryId,
                CategorySuggestionMatchType.EXACT,
            )
        }
    }

    private fun changeTransactionType(type: TransactionType) {
        transactionType.update { type }
        _state.update { it.copy(transactionType = type) }
    }

    private fun closePage() {
        appComposeNavigator.popBackStack()
    }

    private fun openCategoryCreate() {
        appComposeNavigator.navigate(ExpenseManagerScreens.CategoryCreate(null))
    }

    private fun openAccountCreate() {
        appComposeNavigator.navigate(ExpenseManagerScreens.AccountCreate(null))
    }

    private fun dismissDeleteDialog() {
        _state.update { it.copy(showDeleteDialog = false) }
    }

    private fun showDeleteDialog() {
        _state.update { it.copy(showDeleteDialog = true) }
    }

    // endregion

    fun processAction(action: TransactionCreateAction) {
        when (action) {
            TransactionCreateAction.ClosePage -> cancelEditing()
            TransactionCreateAction.ShowDeleteDialog -> showDeleteDialog()
            TransactionCreateAction.DismissDeleteDialog -> dismissDeleteDialog()
            TransactionCreateAction.Delete -> deleteTransaction()
            TransactionCreateAction.Save -> save()

            is TransactionCreateAction.OpenAccountCreate -> openAccountCreate()
            is TransactionCreateAction.OpenCategoryCreate -> openCategoryCreate()

            is TransactionCreateAction.ChangeTransactionType -> changeTransactionType(action.type)

            is TransactionCreateAction.SetNumberPadValue -> action.amount?.let {
                setAmountOnChange(
                    it
                )
            }

            TransactionCreateAction.ShowCategorySelection -> _state.update {
                it.copy(
                    showCategorySelection = true
                )
            }

            TransactionCreateAction.DismissCategorySelection -> _state.update {
                it.copy(
                    showCategorySelection = false
                )
            }

            is TransactionCreateAction.SelectCategory -> _state.update {
                categoryWasManuallySelected = true
                it.copy(selectedCategory = action.category, showCategorySelection = false)
            }

            is TransactionCreateAction.ShowAccountSelection -> _state.update {
                it.copy(showAccountSelection = true, accountSelection = action.type)
            }

            TransactionCreateAction.DismissAccountSelection -> _state.update {
                it.copy(
                    showAccountSelection = false
                )
            }

            TransactionCreateAction.ShowCurrencySelection -> _state.update {
                it.copy(showCurrencySelection = true)
            }

            TransactionCreateAction.DismissCurrencySelection -> _state.update {
                it.copy(showCurrencySelection = false)
            }

            is TransactionCreateAction.SelectCurrency -> {
                currencyWasManuallySelected = true
                _state.update {
                    it.copy(
                        selectedCurrency = action.currency,
                        showCurrencySelection = false,
                    )
                }
            }

            is TransactionCreateAction.SetManualExchangeRate -> _state.update {
                it.copy(
                    manualExchangeRate = it.manualExchangeRate.copy(
                        value = action.rate,
                        valueError = false,
                    ),
                    exchangeRateError = null,
                )
            }

            TransactionCreateAction.ConfirmManualExchangeRate -> confirmManualExchangeRate()

            TransactionCreateAction.DismissExchangeRateDialog -> _state.update {
                it.copy(showExchangeRateDialog = false, exchangeRateError = null)
            }

            is TransactionCreateAction.SelectAccount -> _state.update {
                when (it.accountSelection) {
                    AccountSelection.FROM_ACCOUNT -> it.copy(
                        selectedFromAccount = action.account,
                        showAccountSelection = false
                    )

                    AccountSelection.TO_ACCOUNT -> it.copy(
                        selectedToAccount = action.account,
                        showAccountSelection = false
                    )
                }
            }

            TransactionCreateAction.ShowNumberPad -> _state.update { it.copy(showNumberPad = true) }
            TransactionCreateAction.DismissNumberPad -> _state.update { it.copy(showNumberPad = false) }

            TransactionCreateAction.ShowDateSelection -> _state.update { it.copy(showDateSelection = true) }
            TransactionCreateAction.ShowTimeSelection -> _state.update { it.copy(showTimeSelection = true) }
            TransactionCreateAction.DismissDateSelection -> _state.update {
                it.copy(showDateSelection = false, showTimeSelection = false)
            }

            is TransactionCreateAction.SelectDate -> _state.update {
                it.copy(
                    dateTime = action.date,
                    showDateSelection = false,
                    showTimeSelection = false
                )
            }

            TransactionCreateAction.ShowAttachmentPicker -> _state.update {
                it.copy(showAttachmentPicker = true)
            }

            TransactionCreateAction.DismissAttachmentPicker -> _state.update {
                it.copy(showAttachmentPicker = false)
            }

            is TransactionCreateAction.AttachmentPicked -> {
                _state.update { it.copy(showAttachmentPicker = false) }
                onAttachmentPicked(action.uri)
            }

            is TransactionCreateAction.RemoveAttachment -> removeAttachment(action.path)
        }
    }

    override fun onCleared() {
        super.onCleared()
        discardSessionAttachments()
    }

    private fun TransactionType.toCategoryType(): CategoryType =
        if (isIncome()) CategoryType.INCOME else CategoryType.EXPENSE

    companion object {
        private val defaultCategory = Category(
            id = "1",
            name = "Shopping",
            type = CategoryType.EXPENSE,
            storedIcon = StoredIcon(
                name = "ic_calendar",
                backgroundColor = "#000000",
            ),
            createdOn = Date(),
            updatedOn = Date(),
        )

        private val defaultAccount = AccountUiModel(
            id = "1",
            name = "Shopping",
            storedIcon = StoredIcon(
                name = "ic_calendar",
                backgroundColor = "#000000",
            ),
            amount = Amount(0.0, "$ 0.00"),
            amountTextColor = com.naveenapps.expensemanager.core.common.R.color.green_500,
        )

        private const val SUGGESTION_DEBOUNCE_MS = 200L
        private const val AUTO_SELECT_CONFIDENCE = 0.75
        private const val MAX_SUGGESTIONS = 3
    }
}
