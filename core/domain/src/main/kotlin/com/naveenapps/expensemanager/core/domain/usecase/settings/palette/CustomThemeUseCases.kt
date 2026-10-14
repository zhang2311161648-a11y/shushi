package com.naveenapps.expensemanager.core.domain.usecase.settings.palette

import com.naveenapps.expensemanager.core.repository.ColorPaletteRepository
import kotlinx.coroutines.flow.Flow

class GetCustomColorHexUseCase(private val repository: ColorPaletteRepository) {
    operator fun invoke(): Flow<String?> = repository.getCustomColorHex()
}

class SaveCustomColorHexUseCase(private val repository: ColorPaletteRepository) {
    suspend operator fun invoke(hex: String?): Boolean = repository.saveCustomColorHex(hex)
}

class GetCustomBackgroundImageUriUseCase(private val repository: ColorPaletteRepository) {
    operator fun invoke(): Flow<String?> = repository.getCustomBackgroundImageUri()
}

class SaveCustomBackgroundImageUriUseCase(private val repository: ColorPaletteRepository) {
    suspend operator fun invoke(uri: String?): Boolean = repository.saveCustomBackgroundImageUri(uri)
}
