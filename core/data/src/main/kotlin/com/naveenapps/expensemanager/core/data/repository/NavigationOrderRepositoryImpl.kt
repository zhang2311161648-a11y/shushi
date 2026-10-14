package com.naveenapps.expensemanager.core.data.repository

import com.naveenapps.expensemanager.core.datastore.SettingsDataStore
import com.naveenapps.expensemanager.core.repository.NavigationOrderRepository
import kotlinx.coroutines.flow.Flow

class NavigationOrderRepositoryImpl(
    private val settingsDataStore: SettingsDataStore,
) : NavigationOrderRepository {

    override fun observeBottomBarOrder(): Flow<List<String>> =
        settingsDataStore.getBottomBarOrder()

    override suspend fun saveBottomBarOrder(itemIds: List<String>) {
        settingsDataStore.setBottomBarOrder(itemIds)
    }

    override suspend fun resetBottomBarOrder() {
        settingsDataStore.setBottomBarOrder(emptyList())
    }
}
