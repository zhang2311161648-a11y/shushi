package com.naveenapps.expensemanager.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.naveenapps.expensemanager.core.database.entity.LedgerEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface LedgerDao : BaseDao<LedgerEntity> {

    @Query("SELECT * FROM ledger WHERE is_archived = 0 ORDER BY created_on ASC")
    fun observeActive(): Flow<List<LedgerEntity>>

    @Query("SELECT * FROM ledger WHERE id = :id AND is_archived = 0")
    fun observeActiveById(id: String): Flow<LedgerEntity?>

    @Query("SELECT * FROM ledger WHERE id = :id")
    suspend fun findById(id: String): LedgerEntity?

    @Query("SELECT * FROM ledger WHERE is_archived = 0 ORDER BY created_on ASC")
    suspend fun getActive(): List<LedgerEntity>

    @Query("SELECT COUNT(*) FROM ledger WHERE is_archived = 0")
    suspend fun countActive(): Int

    @Query("UPDATE ledger SET name = :name, updated_on = :updatedOn WHERE id = :id AND is_archived = 0")
    suspend fun rename(id: String, name: String, updatedOn: Date): Int

    @Query("UPDATE ledger SET is_archived = 1, updated_on = :updatedOn WHERE id = :id AND is_archived = 0")
    suspend fun archive(id: String, updatedOn: Date): Int
}
