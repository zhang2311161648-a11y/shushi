package com.naveenapps.expensemanager.core.settings.domain.repository

import com.naveenapps.expensemanager.core.settings.domain.model.NumberFormatType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface NumberFormatSettingRepository {

    fun getNumberFormatType(): Flow<NumberFormatType>

    suspend fun saveNumberFormatType(numberFormatType: NumberFormatType)

    /** Number of fractional digits used by amount display. Defaults to the legacy one digit. */
    fun getDecimalDigits(): Flow<Int> = flowOf(DEFAULT_DECIMAL_DIGITS)

    suspend fun saveDecimalDigits(decimalDigits: Int) = Unit

    companion object {
        const val DEFAULT_DECIMAL_DIGITS = 1
    }
}
