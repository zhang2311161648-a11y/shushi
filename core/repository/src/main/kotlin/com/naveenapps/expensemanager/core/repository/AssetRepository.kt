package com.naveenapps.expensemanager.core.repository

import com.naveenapps.expensemanager.core.model.Asset
import com.naveenapps.expensemanager.core.model.Resource
import kotlinx.coroutines.flow.Flow

interface AssetRepository {

    fun getAssets(): Flow<List<Asset>>

    suspend fun addAsset(asset: Asset): Resource<Boolean>

    suspend fun updateAsset(asset: Asset): Resource<Boolean>

    suspend fun deleteAsset(asset: Asset): Resource<Boolean>
}
