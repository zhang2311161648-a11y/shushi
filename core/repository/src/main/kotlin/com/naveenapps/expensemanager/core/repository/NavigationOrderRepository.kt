package com.naveenapps.expensemanager.core.repository

import kotlinx.coroutines.flow.Flow

/** Persists the user's preferred order for the four primary navigation destinations. */
interface NavigationOrderRepository {

    fun observeBottomBarOrder(): Flow<List<String>>

    suspend fun saveBottomBarOrder(itemIds: List<String>)

    suspend fun resetBottomBarOrder()
}
