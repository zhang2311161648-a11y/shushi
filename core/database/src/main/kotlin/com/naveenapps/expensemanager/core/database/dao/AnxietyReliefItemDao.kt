package com.naveenapps.expensemanager.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.naveenapps.expensemanager.core.database.entity.AnxietyReliefItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnxietyReliefItemDao : BaseDao<AnxietyReliefItemEntity> {

    @Query("SELECT * FROM anxiety_relief_item WHERE is_archived = 0 ORDER BY updated_at DESC, created_at DESC")
    fun getActiveItems(): Flow<List<AnxietyReliefItemEntity>>

    @Query("SELECT * FROM anxiety_relief_item ORDER BY updated_at DESC, created_at DESC")
    fun getAllItems(): Flow<List<AnxietyReliefItemEntity>>

    @Query("SELECT * FROM anxiety_relief_item WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): AnxietyReliefItemEntity?
}
