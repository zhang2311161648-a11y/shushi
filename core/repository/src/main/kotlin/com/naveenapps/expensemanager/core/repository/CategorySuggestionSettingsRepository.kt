package com.naveenapps.expensemanager.core.repository

import com.naveenapps.expensemanager.core.model.Resource
import kotlinx.coroutines.flow.Flow

interface CategorySuggestionSettingsRepository {

    fun isEnabled(): Flow<Boolean>

    suspend fun setEnabled(enabled: Boolean): Resource<Boolean>
}
