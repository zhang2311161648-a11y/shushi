package com.naveenapps.expensemanager.core.domain.usecase.settings.currency

import com.naveenapps.expensemanager.core.model.ExchangeRateSnapshot
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.ExchangeRateRepository

class GetExchangeRateUseCase(private val repository: ExchangeRateRepository) {
    suspend operator fun invoke(fromCurrencyCode: String, toCurrencyCode: String): Resource<ExchangeRateSnapshot> =
        repository.getRate(fromCurrencyCode, toCurrencyCode)
}
