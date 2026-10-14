package com.naveenapps.expensemanager.core.data.mappers

import com.naveenapps.expensemanager.core.database.entity.AssetEntity
import com.naveenapps.expensemanager.core.model.Asset

fun Asset.toEntityModel(): AssetEntity {
    return AssetEntity(
        id = id,
        name = name,
        amount = amount,
        note = note,
        createdOn = createdOn,
        updatedOn = updatedOn,
    )
}

fun AssetEntity.toDomainModel(): Asset {
    return Asset(
        id = id,
        name = name,
        amount = amount,
        note = note,
        createdOn = createdOn,
        updatedOn = updatedOn,
    )
}
