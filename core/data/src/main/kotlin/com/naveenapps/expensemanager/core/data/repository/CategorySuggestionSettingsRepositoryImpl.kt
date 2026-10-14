package com.naveenapps.expensemanager.core.data.repository

import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.datastore.SettingsDataStore
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.CategorySuggestionSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class CategorySuggestionSettingsRepositoryImpl(
    private val dataStore: SettingsDataStore,
    private val dispatchers: AppCoroutineDispatchers,
) : CategorySuggestionSettingsRepository {

    override fun isEnabled(): Flow<Boolean> = dataStore.isSmartCategoryEnabled()

    override suspend fun setEnabled(enabled: Boolean): Resource<Boolean> = withContext(dispatchers.io) {
        return@withContext try {
            dataStore.setSmartCategoryEnabled(enabled)
            Resource.Success(true)
        } catch (exception: Exception) {
            Resource.Error(exception)
        }
    }
}
