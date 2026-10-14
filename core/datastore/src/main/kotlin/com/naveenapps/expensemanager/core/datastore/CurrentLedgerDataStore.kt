package com.naveenapps.expensemanager.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.naveenapps.expensemanager.core.model.DEFAULT_LEDGER_ID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CurrentLedgerDataStore(private val dataStore: DataStore<Preferences>) {

    fun getCurrentLedgerId(): Flow<String> = dataStore.data.map { preferences ->
        preferences[KEY_CURRENT_LEDGER_ID] ?: DEFAULT_LEDGER_ID
    }

    suspend fun setCurrentLedgerId(ledgerId: String) {
        dataStore.edit { preferences ->
            preferences[KEY_CURRENT_LEDGER_ID] = ledgerId
        }
    }

    private companion object {
        val KEY_CURRENT_LEDGER_ID = stringPreferencesKey("current_ledger_id")
    }
}
