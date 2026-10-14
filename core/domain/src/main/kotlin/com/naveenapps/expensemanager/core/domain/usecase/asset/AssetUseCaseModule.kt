package com.naveenapps.expensemanager.core.domain.usecase.asset

import org.koin.dsl.module

val AssetUseCaseModule = module {
    single { AddAssetUseCase(get()) }
    single { UpdateAssetUseCase(get()) }
    single { DeleteAssetUseCase(get()) }
}
