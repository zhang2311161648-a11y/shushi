package com.naveenapps.expensemanager.core.repository

import com.naveenapps.expensemanager.core.model.UserProfile
import kotlinx.coroutines.flow.Flow

/** Stores the user's local display name and avatar for the My space header. */
interface ProfileRepository {
    fun observeProfile(): Flow<UserProfile>

    suspend fun saveProfile(profile: UserProfile): Boolean
}
