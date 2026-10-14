package com.naveenapps.expensemanager.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.naveenapps.expensemanager.core.model.DEFAULT_PROFILE_NAME
import com.naveenapps.expensemanager.core.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Persists the local profile shown in the app's personal space. */
class ProfileDataStore(private val dataStore: DataStore<Preferences>) {

    fun observeProfile(): Flow<UserProfile> = dataStore.data.map { preferences ->
        UserProfile(
            displayName = preferences[DISPLAY_NAME]
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?: DEFAULT_PROFILE_NAME,
            avatarPath = preferences[AVATAR_PATH]
                ?.trim()
                ?.takeIf { it.isNotEmpty() },
        )
    }

    suspend fun setProfile(displayName: String, avatarPath: String?) {
        dataStore.edit { preferences ->
            preferences[DISPLAY_NAME] = displayName
            if (avatarPath.isNullOrBlank()) {
                preferences.remove(AVATAR_PATH)
            } else {
                preferences[AVATAR_PATH] = avatarPath
            }
        }
    }

    private companion object {
        val DISPLAY_NAME = stringPreferencesKey("profile_display_name")
        val AVATAR_PATH = stringPreferencesKey("profile_avatar_path")
    }
}
