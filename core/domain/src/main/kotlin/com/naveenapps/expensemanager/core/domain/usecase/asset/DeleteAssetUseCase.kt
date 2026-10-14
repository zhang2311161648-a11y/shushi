package com.naveenapps.expensemanager.core.domain.usecase.asset

import com.naveenapps.expensemanager.core.model.Asset
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.AssetRepository

class DeleteAssetUseCase(private val repository: AssetRepository) {

    suspend operator fun invoke(asset: Asset): Resource<Boolean> {
        return repository.deleteAsset(asset)
    }
}
