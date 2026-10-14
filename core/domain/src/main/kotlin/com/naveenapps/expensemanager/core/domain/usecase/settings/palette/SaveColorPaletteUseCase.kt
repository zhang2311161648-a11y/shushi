package com.naveenapps.expensemanager.core.domain.usecase.settings.palette

import com.naveenapps.expensemanager.core.model.JiangJiColorPalette
import com.naveenapps.expensemanager.core.repository.ColorPaletteRepository

class SaveColorPaletteUseCase(
    private val repository: ColorPaletteRepository,
) {
    suspend operator fun invoke(palette: JiangJiColorPalette): Boolean = repository.savePalette(palette)
}
