package com.naveenapps.expensemanager.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.naveenapps.expensemanager.core.database.entity.SalaryCycleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SalaryCycleDao {

    @Query("SELECT * FROM salary_cycle ORDER BY start_date ASC")
    fun observeAll(): Flow<List<SalaryCycleEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(cycle: SalaryCycleEntity): Long

    @Query("DELETE FROM salary_cycle WHERE id = :id")
    suspend fun deleteById(id: Long): Int
}
