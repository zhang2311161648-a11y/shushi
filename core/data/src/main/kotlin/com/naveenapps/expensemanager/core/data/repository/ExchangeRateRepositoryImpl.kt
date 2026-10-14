package com.naveenapps.expensemanager.core.data.repository

import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.datastore.ExchangeRateCache
import com.naveenapps.expensemanager.core.datastore.ExchangeRateDataStore
import com.naveenapps.expensemanager.core.model.EXCHANGE_RATE_SCALE
import com.naveenapps.expensemanager.core.model.ExchangeRateSnapshot
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.ExchangeRateRepository
import kotlinx.coroutines.withContext

private const val MANUAL_SOURCE = "manual"

class ExchangeRateRepositoryImpl(
    private val exchangeRateDataStore: ExchangeRateDataStore,
    private val dispatchers: AppCoroutineDispatchers,
) : ExchangeRateRepository {

    override suspend fun getRate(
        fromCurrencyCode: String,
        toCurrencyCode: String,
    ): Resource<ExchangeRateSnapshot> = withContext(dispatchers.io) {
        val from = fromCurrencyCode.trim().uppercase()
        val to = toCurrencyCode.trim().uppercase()
        if (from.isBlank() || to.isBlank()) {
            return@withContext Resource.Error(IllegalArgumentException("Currency code is required"))
        }
        if (from == to) {
            return@withContext Resource.Success(
                ExchangeRateSnapshot(from, to, EXCHANGE_RATE_SCALE, "same_currency", System.currentTimeMillis()),
            )
        }

        // Single-resource builds never access the network. A user-provided rate is persisted
        // locally and can be reused; without one, the caller can ask for a manual rate.
        exchangeRateDataStore.getRate(from, to)?.let { cached ->
            return@withContext Resource.Success(cached.toSnapshot(from, to))
        }
        Resource.Error(IllegalStateException("No local exchange rate is available; enter a manual rate"))
    }

    override suspend fun saveManualRate(
        fromCurrencyCode: String,
        toCurrencyCode: String,
        rateMicros: Long,
    ): Resource<ExchangeRateSnapshot> = withContext(dispatchers.io) {
        val from = fromCurrencyCode.trim().uppercase()
        val to = toCurrencyCode.trim().uppercase()
        if (from.isBlank() || to.isBlank() || rateMicros <= 0L) {
            return@withContext Resource.Error(IllegalArgumentException("A valid currency pair and rate are required"))
        }
        val now = System.currentTimeMillis()
        exchangeRateDataStore.saveRate(from, to, rateMicros, MANUAL_SOURCE, now)
        Resource.Success(ExchangeRateSnapshot(from, to, rateMicros, MANUAL_SOURCE, now))
    }

    private fun ExchangeRateCache.toSnapshot(
        from: String,
        to: String,
        source: String = this.source,
    ): ExchangeRateSnapshot = ExchangeRateSnapshot(from, to, rateMicros, source, updatedAtMillis)
}
