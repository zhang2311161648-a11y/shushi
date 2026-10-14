package com.naveenapps.expensemanager.core.repository

import com.naveenapps.expensemanager.core.model.AnxietyReliefItem
import com.naveenapps.expensemanager.core.model.Resource
import kotlinx.coroutines.flow.Flow

interface AnxietyReliefRepository {
    fun getItems(): Flow<List<AnxietyReliefItem>>

    suspend fun add(item: AnxietyReliefItem): Resource<Boolean>

    suspend fun update(item: AnxietyReliefItem): Resource<Boolean>

    suspend fun archive(item: AnxietyReliefItem): Resource<Boolean>

    suspend fun delete(item: AnxietyReliefItem): Resource<Boolean>
}

interface AnxietyReliefSettingsRepository {
    fun getDefaultDailyTargetCents(): Flow<Long>

    suspend fun setDefaultDailyTargetCents(value: Long): Resource<Boolean>
}
