package com.naveenapps.expensemanager.core.repository

import com.naveenapps.expensemanager.core.model.CategorySuggestionMatchType
import com.naveenapps.expensemanager.core.model.CategorySuggestionRule
import com.naveenapps.expensemanager.core.model.Resource
import kotlinx.coroutines.flow.Flow

interface CategorySuggestionRuleRepository {

    fun observeUserRules(): Flow<List<CategorySuggestionRule>>

    suspend fun saveUserRule(
        pattern: String,
        categoryId: String,
        matchType: CategorySuggestionMatchType,
    ): Resource<Boolean>

    suspend fun deleteUserRule(id: Long): Resource<Boolean>

    suspend fun clearUserRules(): Resource<Boolean>
}
