package com.naveenapps.expensemanager.core.domain.usecase.profile

import org.koin.dsl.module

val ProfileUseCaseModule = module {
    single { ObserveUserProfileUseCase(get()) }
    single { SaveUserProfileUseCase(get()) }
}
