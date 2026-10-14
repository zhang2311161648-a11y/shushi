package com.naveenapps.expensemanager.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AnxietyReliefDataStore(private val dataStore: DataStore<Preferences>) {
    val defaultDailyTargetCents: Flow<Long> = dataStore.data.map { preferences ->
        preferences[DEFAULT_DAILY_TARGET_CENTS] ?: DEFAULT_TARGET_CENTS
    }

    suspend fun setDefaultDailyTargetCents(value: Long) {
        dataStore.edit { preferences ->
            preferences[DEFAULT_DAILY_TARGET_CENTS] = value
        }
    }

    companion object {
        const val DEFAULT_TARGET_CENTS = 500L
        private val DEFAULT_DAILY_TARGET_CENTS = longPreferencesKey(
            "anxiety_relief_default_daily_target_cents",
        )
    }
}
