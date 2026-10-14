package com.naveenapps.expensemanager.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.naveenapps.expensemanager.core.database.entity.CategoryEntity
import com.naveenapps.expensemanager.core.model.DEFAULT_LEDGER_ID
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao : BaseDao<CategoryEntity> {

    @Query("SELECT * FROM category WHERE ledger_id = :ledgerId ORDER BY created_on DESC")
    fun getCategories(ledgerId: String): Flow<List<CategoryEntity>?>

    fun getCategories(): Flow<List<CategoryEntity>?> = getCategories(DEFAULT_LEDGER_ID)

    @Query("SELECT * FROM category WHERE id = :aModelId AND ledger_id = :ledgerId")
    fun findByIdFlow(aModelId: String, ledgerId: String): Flow<CategoryEntity?>

    fun findByIdFlow(aModelId: String): Flow<CategoryEntity?> = findByIdFlow(aModelId, DEFAULT_LEDGER_ID)

    @Query("SELECT * FROM category WHERE ledger_id = :ledgerId ORDER BY created_on DESC")
    suspend fun getAllValues(ledgerId: String): List<CategoryEntity>?

    suspend fun getAllValues(): List<CategoryEntity>? = getAllValues(DEFAULT_LEDGER_ID)

    @Query("SELECT * FROM category WHERE id = :aModelId AND ledger_id = :ledgerId")
    suspend fun findById(aModelId: String, ledgerId: String): CategoryEntity?

    suspend fun findById(aModelId: String): CategoryEntity? = findById(aModelId, DEFAULT_LEDGER_ID)
}
