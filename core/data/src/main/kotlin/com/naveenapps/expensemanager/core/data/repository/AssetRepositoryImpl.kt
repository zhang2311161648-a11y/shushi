package com.naveenapps.expensemanager.core.data.repository

import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.data.mappers.toDomainModel
import com.naveenapps.expensemanager.core.data.mappers.toEntityModel
import com.naveenapps.expensemanager.core.database.dao.AssetDao
import com.naveenapps.expensemanager.core.model.Asset
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.AssetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AssetRepositoryImpl(
    private val assetDao: AssetDao,
    private val dispatchers: AppCoroutineDispatchers,
) : AssetRepository {

    override fun getAssets(): Flow<List<Asset>> {
        return assetDao.getAssets().map { assets ->
            assets?.map { it.toDomainModel() } ?: emptyList()
        }
    }

    override suspend fun addAsset(asset: Asset): Resource<Boolean> =
        withContext(dispatchers.io) {
            return@withContext try {
                Resource.Success(assetDao.insert(asset.toEntityModel()) != -1L)
            } catch (exception: Exception) {
                Resource.Error(exception)
            }
        }

    override suspend fun updateAsset(asset: Asset): Resource<Boolean> =
        withContext(dispatchers.io) {
            return@withContext try {
                assetDao.update(asset.toEntityModel())
                Resource.Success(true)
            } catch (exception: Exception) {
                Resource.Error(exception)
            }
        }

    override suspend fun deleteAsset(asset: Asset): Resource<Boolean> =
        withContext(dispatchers.io) {
            return@withContext try {
                Resource.Success(assetDao.delete(asset.toEntityModel()) > 0)
            } catch (exception: Exception) {
                Resource.Error(exception)
            }
        }
}
