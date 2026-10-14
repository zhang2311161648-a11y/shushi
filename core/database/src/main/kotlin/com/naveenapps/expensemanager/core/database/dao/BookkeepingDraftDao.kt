package com.naveenapps.expensemanager.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.naveenapps.expensemanager.core.database.entity.BookkeepingDraftBatchEntity

@Dao
interface BookkeepingDraftDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(batch: BookkeepingDraftBatchEntity)
    @Update suspend fun update(batch: BookkeepingDraftBatchEntity)
    @Query("SELECT * FROM bookkeeping_draft_batch WHERE id=:id AND ledgerId=:ledgerId")
    suspend fun find(id: String, ledgerId: String): BookkeepingDraftBatchEntity?
    @Query("SELECT * FROM bookkeeping_draft_batch WHERE ledgerId=:ledgerId AND state IN ('DRAFT', 'PARTIAL') ORDER BY createdAt DESC")
    suspend fun pending(ledgerId: String): List<BookkeepingDraftBatchEntity>
    @Query("SELECT * FROM bookkeeping_draft_batch WHERE ledgerId=:ledgerId AND state IN ('DRAFT', 'PARTIAL', 'COMMITTED') ORDER BY createdAt DESC LIMIT 20")
    suspend fun recoverable(ledgerId: String): List<BookkeepingDraftBatchEntity>
}
