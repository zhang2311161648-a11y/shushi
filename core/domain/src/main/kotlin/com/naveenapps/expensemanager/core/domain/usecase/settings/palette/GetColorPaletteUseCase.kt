package com.naveenapps.expensemanager.core.domain.usecase.settings.palette

import com.naveenapps.expensemanager.core.model.JiangJiColorPalette
import com.naveenapps.expensemanager.core.repository.ColorPaletteRepository
import kotlinx.coroutines.flow.Flow

class GetColorPaletteUseCase(
    private val repository: ColorPaletteRepository,
) {
    operator fun invoke(): Flow<JiangJiColorPalette> = repository.getSelectedPalette()
}
