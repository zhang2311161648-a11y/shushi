package com.naveenapps.expensemanager.core.repository

import com.naveenapps.expensemanager.core.model.JiangJiColorPalette
import kotlinx.coroutines.flow.Flow

/** Stores and observes the curated ShuShi color palette selection. */
interface ColorPaletteRepository {
    fun getSelectedPalette(): Flow<JiangJiColorPalette>

    suspend fun savePalette(palette: JiangJiColorPalette): Boolean

    fun getCustomColorHex(): Flow<String?>

    suspend fun saveCustomColorHex(hex: String?): Boolean

    fun getCustomBackgroundImageUri(): Flow<String?>

    suspend fun saveCustomBackgroundImageUri(uri: String?): Boolean
}
