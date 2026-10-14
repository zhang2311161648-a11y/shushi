package com.naveenapps.expensemanager.core.domain.usecase.settings.currency

import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.CurrencyRepository
import com.naveenapps.expensemanager.core.repository.TransactionRepository
import kotlinx.coroutines.flow.firstOrNull

class SaveCurrencyUseCase(
    private val repository: CurrencyRepository,
    private val transactionRepository: TransactionRepository? = null,
) {
    suspend operator fun invoke(currency: Currency): Resource<Boolean> {
        if (currency.symbol.isBlank()) {
            return Resource.Error(Exception("Please provide valid currency symbol"))
        }

        if (currency.name.isBlank()) {
            return Resource.Error(Exception("Please provide valid currency name"))
        }

        val requestedBaseCode = currency.code.trim().uppercase().ifBlank { currency.symbol.trim() }
        val existingBaseCodes = transactionRepository
            ?.getAllTransaction()
            ?.firstOrNull()
            .orEmpty()
            .mapNotNull { transaction ->
                transaction.currencySnapshot?.baseCurrency?.code?.trim()?.uppercase()
            }
            .filter { it.isNotBlank() }
            .toSet()
        if (existingBaseCodes.isNotEmpty() && existingBaseCodes.any { it != requestedBaseCode }) {
            return Resource.Error(
                IllegalStateException("The base currency cannot be changed after transactions exist"),
            )
        }

        return Resource.Success(repository.saveCurrency(currency))
    }
}
