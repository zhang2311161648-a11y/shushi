package com.naveenapps.expensemanager.feature.account.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.account.DeleteAccountUseCase
import com.naveenapps.expensemanager.core.domain.usecase.account.GetAllAccountsUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetFormattedAmountUseCase
import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.getAvailableCreditLimit
import com.naveenapps.expensemanager.core.model.toAccountUiModel
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import com.naveenapps.expensemanager.core.repository.ImageStorageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class AccountListViewModel(
    getAllAccountsUseCase: GetAllAccountsUseCase,
    getCurrencyUseCase: GetCurrencyUseCase,
    private val getFormattedAmountUseCase: GetFormattedAmountUseCase,
    private val appComposeNavigator: AppComposeNavigator,
    private val deleteAccountUseCase: DeleteAccountUseCase? = null,
    private val imageStorageRepository: ImageStorageRepository? = null,
) : ViewModel() {

    private val _state = MutableStateFlow(
        AccountListState(
            accounts = emptyList(),
            showReOrder = false
        )
    )
    val state = _state.asStateFlow()

    private var accountsById: Map<String, Account> = emptyMap()

    init {
        combine(
            getCurrencyUseCase.invoke(),
            getAllAccountsUseCase.invoke(),
        ) { currency, accounts ->
            accountsById = accounts.associateBy(Account::id)
            val list = accounts.map {
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

            _state.update {
                it.copy(
                    accounts = list,
                    showReOrder = accounts.isNotEmpty() && accounts.size > 1
                )
            }
        }.launchIn(viewModelScope)
    }

    private fun openCreateScreen(account: AccountUiModel?) {
        appComposeNavigator.navigate(
            ExpenseManagerScreens.AccountCreate(account?.id),
        )
    }

    private fun closePage() {
        appComposeNavigator.popBackStack()
    }

    private fun openAccountReOrderScreen() {
        appComposeNavigator.navigate(ExpenseManagerScreens.AccountReOrderScreen)
    }

    private fun showDeleteDialog(account: AccountUiModel) {
        _state.update {
            it.copy(
                accountToDelete = account,
                deleteFailed = false,
            )
        }
    }

    private fun dismissDeleteDialog() {
        _state.update { it.copy(accountToDelete = null) }
    }

    private fun deleteAccount() {
        val accountUiModel = _state.value.accountToDelete ?: return
        val account = accountsById[accountUiModel.id]
        val useCase = deleteAccountUseCase
        if (account == null || useCase == null) {
            _state.update {
                it.copy(accountToDelete = null, deleteFailed = true)
            }
            return
        }

        viewModelScope.launch {
            when (useCase.invoke(account)) {
                is Resource.Error -> {
                    _state.update {
                        it.copy(accountToDelete = null, deleteFailed = true)
                    }
                }

                is Resource.Success -> {
                    account.storedIcon.customImagePath?.let {
                        imageStorageRepository?.deleteAccountImage(it)
                    }
                    _state.update { it.copy(accountToDelete = null) }
                }
            }
        }
    }

    private fun clearDeleteError() {
        _state.update { it.copy(deleteFailed = false) }
    }

    fun processAction(action: AccountListAction) {
        when (action) {
            AccountListAction.ClosePage -> closePage()
            AccountListAction.CreateAccount -> openCreateScreen(null)
            is AccountListAction.EditAccount -> openCreateScreen(action.accountUiModel)
            AccountListAction.OpenReOrder -> openAccountReOrderScreen()
            is AccountListAction.ShowDeleteDialog -> showDeleteDialog(action.accountUiModel)
            AccountListAction.DismissDeleteDialog -> dismissDeleteDialog()
            AccountListAction.DeleteAccount -> deleteAccount()
            AccountListAction.ClearDeleteError -> clearDeleteError()
        }
    }
}
