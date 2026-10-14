package com.naveenapps.expensemanager.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.naveenapps.expensemanager.core.model.JiangJiColorPalette
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ColorPaletteDataStore(private val dataStore: DataStore<Preferences>) {

    suspend fun setPalette(palette: JiangJiColorPalette) = dataStore.edit { preferences ->
        preferences[KEY_PALETTE] = palette.id
    }

    fun getPalette(): Flow<JiangJiColorPalette> = dataStore.data.map { preferences ->
        JiangJiColorPalette.fromId(preferences[KEY_PALETTE])
    }

    suspend fun setCustomColorHex(hex: String?) = dataStore.edit { preferences ->
        if (hex == null) preferences.remove(KEY_CUSTOM_COLOR_HEX) else {
            require(hex.matches(Regex("#[0-9A-Fa-f]{6}")))
            preferences[KEY_CUSTOM_COLOR_HEX] = hex.uppercase()
            preferences[KEY_PALETTE] = JiangJiColorPalette.CUSTOM.id
        }
    }

    fun getCustomColorHex(): Flow<String?> = dataStore.data.map { preferences ->
        preferences[KEY_CUSTOM_COLOR_HEX]
    }

    suspend fun setCustomBackgroundImageUri(uri: String?) = dataStore.edit { preferences ->
        if (uri == null) preferences.remove(KEY_CUSTOM_BG_URI) else preferences[KEY_CUSTOM_BG_URI] = uri
    }

    fun getCustomBackgroundImageUri(): Flow<String?> = dataStore.data.map { preferences ->
        preferences[KEY_CUSTOM_BG_URI]
    }

    private companion object {
        val KEY_PALETTE = stringPreferencesKey("jiangji_color_palette")
        val KEY_CUSTOM_COLOR_HEX = stringPreferencesKey("jiangji_custom_color_hex")
        val KEY_CUSTOM_BG_URI = stringPreferencesKey("jiangji_custom_bg_uri")
    }
}
