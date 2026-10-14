package com.naveenapps.expensemanager.core.repository

import com.naveenapps.expensemanager.core.model.ExchangeRateSnapshot
import com.naveenapps.expensemanager.core.model.Resource

interface ExchangeRateRepository {

    suspend fun getRate(fromCurrencyCode: String, toCurrencyCode: String): Resource<ExchangeRateSnapshot>

    suspend fun saveManualRate(
        fromCurrencyCode: String,
        toCurrencyCode: String,
        rateMicros: Long,
    ): Resource<ExchangeRateSnapshot>
}
