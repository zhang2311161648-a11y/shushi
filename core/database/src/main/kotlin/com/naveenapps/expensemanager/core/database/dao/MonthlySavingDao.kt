package com.naveenapps.expensemanager.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.naveenapps.expensemanager.core.database.entity.MonthlySavingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlySavingDao {

    @Query("SELECT * FROM monthly_saving ORDER BY year DESC, month DESC")
    fun observeAllSavings(): Flow<List<MonthlySavingEntity>>

    @Query("SELECT * FROM monthly_saving WHERE year = :year AND month = :month LIMIT 1")
    suspend fun getSaving(year: Int, month: Int): MonthlySavingEntity?

    @Query("SELECT * FROM monthly_saving WHERE year = :year AND month = :month LIMIT 1")
    fun observeSaving(year: Int, month: Int): Flow<MonthlySavingEntity?>

    @Query("SELECT * FROM monthly_saving WHERE period_key = :periodKey LIMIT 1")
    suspend fun getSavingByPeriodKey(periodKey: String): MonthlySavingEntity?

    @Query("SELECT * FROM monthly_saving WHERE period_key = :periodKey LIMIT 1")
    fun observeSavingByPeriodKey(periodKey: String): Flow<MonthlySavingEntity?>

    @Query("SELECT * FROM monthly_saving WHERE year = :year ORDER BY COALESCE(period_start_date, printf('%04d-%02d', year, month)) DESC")
    fun observeSavingsByYear(year: Int): Flow<List<MonthlySavingEntity>>

    @Query("SELECT COALESCE(SUM(amount_cents), 0) FROM monthly_saving WHERE year = :year")
    fun getYearTotal(year: Int): Flow<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMonthlySaving(saving: MonthlySavingEntity): Long

    @Query("DELETE FROM monthly_saving WHERE id = :id")
    suspend fun deleteMonthlySaving(id: Long): Int
}
