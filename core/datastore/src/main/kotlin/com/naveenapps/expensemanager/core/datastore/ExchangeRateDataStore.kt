package com.naveenapps.expensemanager.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first

data class ExchangeRateCache(
    val rateMicros: Long,
    val source: String,
    val updatedAtMillis: Long,
)

class ExchangeRateDataStore(private val dataStore: DataStore<Preferences>) {

    suspend fun getRate(fromCode: String, toCode: String): ExchangeRateCache? {
        val key = pairKey(fromCode, toCode)
        val preferences = dataStore.data.first()
        val rate = preferences[longPreferencesKey("exchange_rate_${key}_micros")] ?: return null
        if (rate <= 0L) return null
        return ExchangeRateCache(
            rateMicros = rate,
            source = preferences[stringPreferencesKey("exchange_rate_${key}_source")] ?: "cached",
            updatedAtMillis = preferences[longPreferencesKey("exchange_rate_${key}_updated_at")] ?: 0L,
        )
    }

    suspend fun saveRate(
        fromCode: String,
        toCode: String,
        rateMicros: Long,
        source: String,
        updatedAtMillis: Long,
    ) {
        require(rateMicros > 0L)
        val key = pairKey(fromCode, toCode)
        dataStore.edit { preferences ->
            preferences[longPreferencesKey("exchange_rate_${key}_micros")] = rateMicros
            preferences[stringPreferencesKey("exchange_rate_${key}_source")] = source
            preferences[longPreferencesKey("exchange_rate_${key}_updated_at")] = updatedAtMillis
        }
    }

    private fun pairKey(fromCode: String, toCode: String): String =
        "${fromCode.trim().uppercase()}_${toCode.trim().uppercase()}"
}
