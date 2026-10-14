package com.naveenapps.expensemanager.core.domain.usecase.asset

import com.naveenapps.expensemanager.core.model.Asset
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.AssetRepository

class AddAssetUseCase(private val repository: AssetRepository) {

    suspend operator fun invoke(asset: Asset): Resource<Boolean> {
        return if (asset.name.isBlank() || !asset.amount.isFinite() || asset.amount <= 0.0) {
            Resource.Error(IllegalArgumentException("Asset name and a positive amount are required"))
        } else {
            repository.addAsset(asset)
        }
    }
}
