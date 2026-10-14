package com.naveenapps.expensemanager.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.naveenapps.expensemanager.core.database.entity.AssetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetDao : BaseDao<AssetEntity> {

    @Query("SELECT * FROM asset ORDER BY updated_on DESC, created_on DESC")
    fun getAssets(): Flow<List<AssetEntity>?>

    @Query("SELECT * FROM asset WHERE id = :id")
    suspend fun findById(id: String): AssetEntity?
}
