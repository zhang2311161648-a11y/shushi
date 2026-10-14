package com.naveenapps.expensemanager.core.data.repository

import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.datastore.ColorPaletteDataStore
import com.naveenapps.expensemanager.core.model.JiangJiColorPalette
import com.naveenapps.expensemanager.core.repository.ColorPaletteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ColorPaletteRepositoryImpl(
    private val dataStore: ColorPaletteDataStore,
    private val dispatchers: AppCoroutineDispatchers,
) : ColorPaletteRepository {

    override fun getSelectedPalette(): Flow<JiangJiColorPalette> = dataStore.getPalette()

    override suspend fun savePalette(palette: JiangJiColorPalette): Boolean =
        withContext(dispatchers.io) {
            dataStore.setPalette(palette)
            true
        }

    override fun getCustomColorHex(): Flow<String?> = dataStore.getCustomColorHex()

    override suspend fun saveCustomColorHex(hex: String?): Boolean =
        withContext(dispatchers.io) {
            dataStore.setCustomColorHex(hex)
            true
        }

    override fun getCustomBackgroundImageUri(): Flow<String?> = dataStore.getCustomBackgroundImageUri()

    override suspend fun saveCustomBackgroundImageUri(uri: String?): Boolean =
        withContext(dispatchers.io) {
            dataStore.setCustomBackgroundImageUri(uri)
            true
        }
}
