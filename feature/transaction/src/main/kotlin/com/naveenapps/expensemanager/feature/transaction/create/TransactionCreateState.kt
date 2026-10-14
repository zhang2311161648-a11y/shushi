package com.naveenapps.expensemanager.feature.transaction.create

import androidx.compose.runtime.Stable
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.CategorySuggestion
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.TextFieldValue
import com.naveenapps.expensemanager.core.model.TransactionType
import java.util.Date

@Stable
data class TransactionCreateState(
    val amount: TextFieldValue<String>,
    val notes: TextFieldValue<String>,
    val dateTime: Date,
    val transactionType: TransactionType,
    val currency: Currency,
    val selectedCurrency: Currency,
    val selectedCategory: Category,
    val selectedFromAccount: AccountUiModel,
    val selectedToAccount: AccountUiModel,
    val accounts: List<AccountUiModel>,
    val categories: List<Category>,
    val accountSelection: AccountSelection,
    val showDeleteDialog: Boolean,
    val showDeleteButton: Boolean,
    val showNumberPad: Boolean,
    val showCategorySelection: Boolean,
    val showAccountSelection: Boolean,
    val showCurrencySelection: Boolean,
    val showDateSelection: Boolean,
    val showTimeSelection: Boolean,
    val attachments: List<String> = emptyList(),
    val showAttachmentPicker: Boolean = false,
    val categorySuggestions: List<CategorySuggestion> = emptyList(),
    val isCategorySuggestionEnabled: Boolean = true,
    val categorySuggestionResolved: Boolean = false,
    val manualExchangeRate: TextFieldValue<String> = TextFieldValue("", onValueChange = null),
    val showExchangeRateDialog: Boolean = false,
    val exchangeRateError: String? = null,
)

enum class AccountSelection {
    FROM_ACCOUNT,
    TO_ACCOUNT
}

data class TransactionCreateInitSetupState(
    val isCategorySyncCompleted: Boolean,
    val isAccountSyncCompleted: Boolean,
)
