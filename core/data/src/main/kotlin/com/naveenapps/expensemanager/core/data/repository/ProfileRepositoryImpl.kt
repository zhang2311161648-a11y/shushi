package com.naveenapps.expensemanager.core.data.repository

import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.datastore.ProfileDataStore
import com.naveenapps.expensemanager.core.model.UserProfile
import com.naveenapps.expensemanager.core.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ProfileRepositoryImpl(
    private val dataStore: ProfileDataStore,
    private val dispatchers: AppCoroutineDispatchers,
) : ProfileRepository {

    override fun observeProfile(): Flow<UserProfile> = dataStore.observeProfile()

    override suspend fun saveProfile(profile: UserProfile): Boolean =
        withContext(dispatchers.io) {
            val displayName = profile.displayName.trim().take(MAX_DISPLAY_NAME_LENGTH)
            require(displayName.isNotEmpty()) { "Display name must not be blank" }
            dataStore.setProfile(displayName, profile.avatarPath)
            true
        }

    private companion object {
        const val MAX_DISPLAY_NAME_LENGTH = 32
    }
}
