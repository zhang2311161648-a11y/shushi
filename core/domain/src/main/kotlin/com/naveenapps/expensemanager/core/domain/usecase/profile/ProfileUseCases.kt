package com.naveenapps.expensemanager.core.domain.usecase.profile

import com.naveenapps.expensemanager.core.model.UserProfile
import com.naveenapps.expensemanager.core.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow

class ObserveUserProfileUseCase(private val repository: ProfileRepository) {
    operator fun invoke(): Flow<UserProfile> = repository.observeProfile()
}

class SaveUserProfileUseCase(private val repository: ProfileRepository) {
    suspend operator fun invoke(profile: UserProfile): Boolean = repository.saveProfile(profile)
}
